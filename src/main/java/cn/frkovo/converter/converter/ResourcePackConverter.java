package cn.frkovo.converter.converter;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Converts resource packs from old format to new format.
 * Extracts music from rmc-b zips and creates a combined resource pack.
 */
public class ResourcePackConverter {
    private static final Logger logger = LoggerFactory.getLogger(ResourcePackConverter.class);
    
    // Path to the music file in old resource packs
    private static final String OLD_MUSIC_PATH = "assets/minecraft/sounds/mob/horse/death.ogg";
    
    // Output path for new music files
    private static final String NEW_MUSIC_PATH_PREFIX = "assets/rhythmc/sounds/";
    
    // sounds.json path
    private static final String SOUNDS_JSON_PATH = "assets/rhythmc/sounds.json";
    
    // Map of SHA -> songId
    private final Map<String, Integer> shaToSongIdMap;
    
    public ResourcePackConverter(Map<String, Integer> shaToSongIdMap) {
        this.shaToSongIdMap = shaToSongIdMap;
    }
    
    /**
     * Convert all resource packs in the rmc-b directory.
     * 
     * @param rmcBDir Directory containing SHA-named folders with player.zip files
     * @param outputDir Output directory for the combined resource pack
     * @return Path to the created resource pack zip file
     */
    public Path convert(Path rmcBDir, Path outputDir) throws IOException {
        // Create temp directory for extracted files
        Path tempDir = Files.createTempDirectory("rhythmc_resource_pack");
        
        // Create output directories
        Path soundsDir = tempDir.resolve(NEW_MUSIC_PATH_PREFIX);
        Files.createDirectories(soundsDir);
        
        // Map to store sound event definitions
        JSONObject soundsJson = new JSONObject();
        
        // Process each SHA folder
        try (var stream = Files.list(rmcBDir)) {
            stream.filter(Files::isDirectory)
                .forEach(shaFolder -> {
                    String sha = shaFolder.getFileName().toString();
                    Integer songId = shaToSongIdMap.get(sha);
                    
                    if (songId == null) {
                        logger.warn("No songId mapping found for SHA: {}", sha);
                        return;
                    }
                    
                    // Find zip file in the folder
                    try (var zipStream = Files.list(shaFolder)) {
                        Optional<Path> zipFile = zipStream
                            .filter(p -> p.toString().endsWith(".zip"))
                            .findFirst();
                        
                        if (zipFile.isPresent()) {
                            extractMusic(zipFile.get(), soundsDir, songId, soundsJson);
                        } else {
                            logger.warn("No zip file found in SHA folder: {}", sha);
                        }
                    } catch (IOException e) {
                        logger.error("Error processing SHA folder: {}", sha, e);
                    }
                });
        }
        
        // Write sounds.json
        Path soundsJsonPath = tempDir.resolve(SOUNDS_JSON_PATH);
        Files.createDirectories(soundsJsonPath.getParent());
        Files.writeString(soundsJsonPath, soundsJson.toJSONString(JSONWriter.Feature.PrettyFormat));
        
        // Create pack.mcmeta
        createPackMcmeta(tempDir);
        
        // Create the final zip file
        Path outputZip = outputDir.resolve("RhythMC_Resource_Pack.zip");
        createZip(tempDir, outputZip);
        
        // Clean up temp directory
        deleteDirectory(tempDir);
        
        logger.info("Created resource pack: {}", outputZip);
        return outputZip;
    }
    
    /**
     * Extract music from a zip file and add to sounds.json.
     */
    private void extractMusic(Path zipFile, Path soundsDir, int songId, JSONObject soundsJson) {
        String newFileName = "s" + songId + ".ogg";
        Path outputPath = soundsDir.resolve(newFileName);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(OLD_MUSIC_PATH)) {
                    // Copy the music file
                    Files.copy(zis, outputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Extracted music for song {}: {}", songId, newFileName);
                    
                    // Add sound event definition
                    addSoundEvent(soundsJson, songId);
                    return;
                }
                zis.closeEntry();
            }
            logger.warn("Music file not found in zip: {}", zipFile);
        } catch (IOException e) {
            logger.error("Error extracting music from zip: {}", zipFile, e);
        }
    }
    
    /**
     * Add a sound event definition to sounds.json.
     */
    private void addSoundEvent(JSONObject soundsJson, int songId) {
        String soundEventName = "sounds.music.s" + songId;
        
        JSONObject soundEvent = new JSONObject();
        soundEvent.put("replace", true);
        
        JSONArray sounds = new JSONArray();
        JSONObject sound = new JSONObject();
        sound.put("name", "rhythmc:s" + songId);
        sound.put("type", "file");
        sound.put("volume", 1.0f);
        sound.put("pitch", 1.0f);
        sound.put("weight", 1);
        sound.put("stream", true);  // Stream for better performance with long audio
        sound.put("attenuation_distance", 16);
        sound.put("preload", false);
        sounds.add(sound);
        
        soundEvent.put("sounds", sounds);
        soundEvent.put("subtitle", "RhythMC Song " + songId);
        
        soundsJson.put(soundEventName, soundEvent);
    }
    
    /**
     * Create pack.mcmeta file.
     */
    private void createPackMcmeta(Path dir) throws IOException {
        JSONObject packMcmeta = new JSONObject();
        JSONObject pack = JSONObject.parseObject("""
                {
                        "description": "RhythMC-Test",
                		"pack_format": 9999,
                        "supported_formats": [0, 9999],
                		"min_format": 0,
                        "max_format": 9999
                   }
                """);

        packMcmeta.put("pack", pack);
        
        Path mcmetaPath = dir.resolve("pack.mcmeta");
        Files.writeString(mcmetaPath, packMcmeta.toJSONString(JSONWriter.Feature.PrettyFormat));
    }
    
    /**
     * Create a zip file from a directory with maximum compression.
     */
    private void createZip(Path sourceDir, Path outputZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZip))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);  // Use highest compression level
            Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\", "/"));
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        logger.error("Error adding file to zip: {}", path, e);
                    }
                });
        }
    }
    
    /**
     * Delete a directory recursively.
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
}