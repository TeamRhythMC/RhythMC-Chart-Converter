package cn.frkovo.converter.converter;

import cn.frkovo.converter.Main;
import cn.frkovo.converter.model.old.OldArena;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts old arena format to new arena format
 * New format: arena-id/{manifest.yml} + {arena.schem}
 */
public class ArenaConverter {
    private static final Logger logger = LoggerFactory.getLogger(ArenaConverter.class);
    
    private final File schematicsFolder;
    private int arenaId;
    
    public ArenaConverter(File schematicsFolder, int startArenaId) {
        this.schematicsFolder = schematicsFolder;
        this.arenaId = startArenaId;
    }
    
    public void convert(File arenaFile) throws IOException {
        // Read old arena
        OldArena oldArena = OldArena.fromFile(arenaFile.getAbsolutePath());
        
        // Clean arena name - remove .yml suffix if present and convert to lowercase
        String arenaName = oldArena.getArenaName();
        if (arenaName == null) {
            arenaName = "unknown";
        }
        // Remove .yml suffix if present
        if (arenaName.toLowerCase().endsWith(".yml")) {
            arenaName = arenaName.substring(0, arenaName.length() - 4);
        }
        arenaName = arenaName.toLowerCase();
        
        // Create output folder: arena-id
        String folderName = arenaName;
        Path outputFolder = Path.of(Main.getOutputDir(), "Arenas", folderName);
        Files.createDirectories(outputFolder);
        
        // Create manifest.yml
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        
        Map<String, Object> manifest = new HashMap<>();
        manifest.put("name", arenaName);
        manifest.put("display-name", oldArena.getArenaDisplayname() != null ? oldArena.getArenaDisplayname() : oldArena.getArenaName());
        manifest.put("author", "Unknown");
        manifest.put("description", "");
        manifest.put("icon", oldArena.getArenaIcon() != null ? oldArena.getArenaIcon() : "GRASS_BLOCK");
        
        // Border - use normal-judge-box-material or default to CYAN_CONCRETE
        String border = oldArena.getNormalJudgeBoxMaterial();
        manifest.put("border", border != null ? border : "CYAN_CONCRETE");
        
        // Schematic file name (without extension)
        manifest.put("schematic", arenaName + ".schem");
        
        manifest.put("hide", false);
        
        // Unlock method
        List<Map<String, Object>> unlockMethod = createUnlockMethod(oldArena, arenaName);
        manifest.put("unlock-method", unlockMethod);
        //read schematic
        int[] xyz = readXYZaOffset(new File(schematicsFolder, oldArena.getSchematicFile()));
        Map<String, Object> schematicInfo = new HashMap<>();
        schematicInfo.put("dimensions", List.of(xyz[0], xyz[1], xyz[2]));
        schematicInfo.put("offset", List.of(xyz[3], xyz[4], xyz[5]));
        manifest.put("schematic-info", schematicInfo);

        // Write manifest.yml
        Path manifestPath = outputFolder.resolve("metadata.yml");
        try (FileWriter writer = new FileWriter(manifestPath.toFile())) {
            yaml.dump(manifest, writer);
        }
        
        // Copy schematic file to output folder
        copySchematicFile(oldArena.getSchematicFile(), outputFolder, arenaName);
        
        logger.info("Converted arena: {} -> {} ({})", arenaFile.getName(), folderName, arenaName);
        arenaId++;
    }
    
    private void copySchematicFile(String schematicFileName, Path outputFolder, String arenaName) throws IOException {
        if (schematicFileName == null || schematicFileName.isEmpty()) {
            logger.warn("No schematic file specified for arena: {}", arenaName);
            return;
        }
        
        File schematicFile = new File(schematicsFolder, schematicFileName);
        if (!schematicFile.exists()) {
            logger.warn("Schematic file not found: {}", schematicFileName);
            return;
        }

        // Copy schematic file to output folder with arena name
        Path destPath = outputFolder.resolve(arenaName + ".schem");
        Files.copy(schematicFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
    }
    private int[] readXYZaOffset(File datFile) throws IOException {
        NamedTag namedTag = NBTUtil.read(datFile);
        int[] expected = new int[]{0, 0, 0, 0, 0, 0};
        CompoundTag t = (CompoundTag) namedTag.getTag();
        CompoundTag t2 = (CompoundTag) t.get("Schematic");
        if( t2 != null) {
            Tag<?> nameTag = t2.get("Width");
            if (nameTag instanceof ShortTag) {
                 expected[0] = ((ShortTag) nameTag).asShort();
            }
            Tag<?> nameTag2 = t2.get("Height");
            if (nameTag instanceof ShortTag) {
                 expected[1] = ((ShortTag) nameTag2).asShort();
            }
            Tag<?> nameTag3 = t2.get("Length");
            if (nameTag instanceof ShortTag) {
                 expected[2] = ((ShortTag) nameTag3).asShort();
            }
            CompoundTag t3 = (CompoundTag) t2.get("Metadata");
            CompoundTag t4 = (CompoundTag) t3.get("WorldEdit");
            Tag<?> nameTag1 = t4.get("Offset");
            if(nameTag1 instanceof ListTag listTag) {
                if (listTag.size() == 3) {
                    expected[3] = ((IntTag) listTag.get(0)).asInt();
                    expected[4] = ((IntTag) listTag.get(1)).asInt();
                    expected[5] = ((IntTag) listTag.get(2)).asInt();
                }
            }
        }

        return expected;
    }
    private short[] readOFFSET(File datFile) throws IOException {
        NamedTag namedTag = NBTUtil.read(datFile);
        short[] expected = new short[]{0, 0, 0};
        CompoundTag t = (CompoundTag) namedTag.getTag();
        CompoundTag t1 = (CompoundTag) t.get("Schematic");


        return expected;
    }
    private List<Map<String, Object>> createUnlockMethod(OldArena oldArena, String arenaName) {
        List<Map<String, Object>> unlockMethods = new ArrayList<>();
        
        if (oldArena.isCanBuy() && oldArena.getPrice() > 0) {
            Map<String, Object> method = new HashMap<>();
            method.put("type", "money");
            method.put("value", (double) oldArena.getPrice());
            unlockMethods.add(method);
        } else if (oldArena.isCanBuy()) {
            // Can buy but price is 0, use permission
            Map<String, Object> method = new HashMap<>();
            method.put("type", "permission");
            method.put("value", "default");
            unlockMethods.add(method);
        } else {
            // Cannot buy, use permission
            Map<String, Object> method = new HashMap<>();
            method.put("type", "permission");
            method.put("value", "rhythmc.arena." + arenaName);
            unlockMethods.add(method);
        }
        
        return unlockMethods;
    }
    
    public int getNextArenaId() {
        return arenaId;
    }
}
