package cn.frkovo.converter;

import cn.frkovo.converter.converter.ChartConverter;
import cn.frkovo.converter.converter.ArenaConverter;
import cn.frkovo.converter.converter.ResourcePackConverter;
import cn.frkovo.converter.util.UUIDResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * RhythMC Chart Converter - Main Entry Point
 * Converts RhythMC v1 chart format to RhythMC-Reborn v2 format
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Configuration
    private static final String INPUT_DIR = "ToConvert";
    private static final String OUTPUT_DIR = "Converted";
    private static final String PLAYERDATA_DIR = "playerdata";
    private static final int START_SONG_ID = 10001;
    private static final int START_ARENA_ID = 10000;
    
    // Singleton state
    private static int currentSongId = START_SONG_ID;
    private static UUIDResolver uuidResolver;
    
    // SHA to songId mapping for resource pack conversion
    private static final Map<String, Integer> shaToSongIdMap = new HashMap<>();
    
    public static void main(String[] args) {
        logger.info("=== RhythMC Chart Converter v1.0 ===");
        logger.info("Converting charts from {} to {}", INPUT_DIR, OUTPUT_DIR);
        
        try {
            // Initialize UUID resolver
            initUUIDResolver();
            
            // Create output directories
            createOutputDirectories();
            
            // Convert charts
            convertCharts();
            
            // Convert arenas
            convertArenas();
            
            // Convert resource packs
            convertResourcePacks();
            
            logger.info("=== Conversion Complete ===");
        } catch (Exception e) {
            logger.error("Conversion failed", e);
            System.exit(1);
        }
    }
    
    private static void initUUIDResolver() {
        Path playerdataPath = Paths.get(PLAYERDATA_DIR);
        uuidResolver = new UUIDResolver(playerdataPath);
        uuidResolver.loadAll();
    }
    
    public static UUIDResolver getUuidResolver() {
        return uuidResolver;
    }
    
    private static void createOutputDirectories() throws IOException {
        Path chartsOutput = Paths.get(OUTPUT_DIR, "Charts");
        Path arenasOutput = Paths.get(OUTPUT_DIR, "Arenas");
        
        Files.createDirectories(chartsOutput);
        Files.createDirectories(arenasOutput);
        
        logger.info("Created output directories");
    }
    
    private static void convertCharts() throws IOException {
        Path chartsInput = Paths.get(INPUT_DIR, "Charts");
        
        if (!Files.exists(chartsInput)) {
            logger.warn("Charts input directory not found: {}", chartsInput);
            return;
        }
        
        ChartConverter chartConverter = new ChartConverter();
        
        try (Stream<Path> chartFolders = Files.list(chartsInput)) {
            chartFolders
                .filter(Files::isDirectory)
                .forEach(folder -> {
                    try {
                        String sha = folder.getFileName().toString();
                        logger.info("Converting chart: {}", sha);
                        boolean success = chartConverter.convert(folder.toFile(), currentSongId);
                        
                        if (success) {
                            // Record SHA to songId mapping for resource pack conversion
                            shaToSongIdMap.put(sha, currentSongId);
                            currentSongId++;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to convert chart: {}", folder.getFileName(), e);
                    }
                });
        }
        
        logger.info("Converted {} charts", currentSongId - START_SONG_ID);
    }
    
    private static void convertArenas() throws IOException {
        Path arenasInput = Paths.get(INPUT_DIR, "Arenas");
        Path schematicsInput = Paths.get(INPUT_DIR, "ArenaSchematics");
        
        if (!Files.exists(arenasInput)) {
            logger.warn("Arenas input directory not found: {}", arenasInput);
            return;
        }
        
        ArenaConverter arenaConverter = new ArenaConverter(schematicsInput.toFile(), START_ARENA_ID);
        
        try (Stream<Path> arenaFiles = Files.list(arenasInput)) {
            arenaFiles
                .filter(path -> path.toString().endsWith(".yml"))
                .forEach(arenaFile -> {
                    try {
                        logger.info("Converting arena: {}", arenaFile.getFileName());
                        arenaConverter.convert(arenaFile.toFile());
                    } catch (Exception e) {
                        logger.error("Failed to convert arena: {}", arenaFile.getFileName(), e);
                    }
                });
        }
        
        logger.info("Converted {} arenas", arenaConverter.getNextArenaId() - START_ARENA_ID);
    }
    
    private static void convertResourcePacks() throws IOException {
        Path rmcBDir = Paths.get(INPUT_DIR, "rmc-b");
        
        if (!Files.exists(rmcBDir)) {
            logger.warn("Resource pack input directory not found: {}", rmcBDir);
            return;
        }
        
        if (shaToSongIdMap.isEmpty()) {
            logger.warn("No charts converted, skipping resource pack conversion");
            return;
        }
        
        // Create output directory for resource pack
        Path resourcePackOutput = Paths.get(OUTPUT_DIR);
        Files.createDirectories(resourcePackOutput);

        ResourcePackConverter resourcePackConverter = new ResourcePackConverter(shaToSongIdMap);
        
        try {
            Path outputZip = resourcePackConverter.convert(rmcBDir, resourcePackOutput);
            logger.info("Created combined resource pack: {}", outputZip);
        } catch (Exception e) {
            logger.error("Failed to convert resource packs", e);
        }
    }
    
    public static String getOutputDir() {
        return OUTPUT_DIR;
    }
}
