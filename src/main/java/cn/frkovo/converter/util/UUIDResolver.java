package cn.frkovo.converter.util;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves UUID to username by reading playerdata NBT files
 */
public class UUIDResolver {
    private static final Logger logger = LoggerFactory.getLogger(UUIDResolver.class);
    
    private final Path playerdataDir;
    private final Map<String, String> uuidToNameCache;
    
    public UUIDResolver(Path playerdataDir) {
        this.playerdataDir = playerdataDir;
        this.uuidToNameCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Load all playerdata files and build UUID->Name mapping
     */
    public void loadAll() {
        if (playerdataDir == null || !playerdataDir.toFile().exists()) {
            logger.warn("Playerdata directory not found: {}", playerdataDir);
            return;
        }
        
        File[] datFiles = playerdataDir.toFile().listFiles();
        
        if (datFiles == null) {
            logger.warn("No playerdata files found in: {}", playerdataDir);
            return;
        }
        
        int loaded = 0;
        for (File datFile : datFiles) {
            try {
                String uuid = datFile.getName().replace(".dat", "");
                String name = readLastKnownName(datFile);
                if (name != null && !name.isEmpty()) {
                    uuidToNameCache.put(uuid, name);
                    loaded++;
                }
            } catch (Exception e) {
                logger.debug("Failed to read playerdata file: {}", datFile.getName());
            }
        }
        
        logger.info("Loaded {} UUID->Name mappings from playerdata", loaded);
    }
    
    /**
     * Read lastKnownName from a playerdata NBT file
     */
    private String readLastKnownName(File datFile) throws IOException {
        NamedTag namedTag = NBTUtil.read(datFile);

        // Try to find bukkit.lastKnownName
        CompoundTag t = (CompoundTag) namedTag.getTag();
        CompoundTag t2 = (CompoundTag) t.get("bukkit");
        if( t2 != null) {
            Tag<?> nameTag = t2.get("lastKnownName");
            if (nameTag instanceof StringTag) {
                return ((StringTag) nameTag).getValue();
            }
        }
        return null;
    }
    
    /**
     * Resolve UUID to username
     * @param uuid UUID string (with or without dashes)
     * @return Username if found, otherwise returns the original UUID
     */
    public String resolve(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return "Unknown";
        }
        
        // Normalize UUID format (remove dashes)
        String normalizedUuid = uuid.replace("-", "");
        
        // Check cache
        String name = uuidToNameCache.get(normalizedUuid);
        if (name != null) {
            return name;
        }
        
        // Try with dashes format
        String dashedUuid = toDashedFormat(normalizedUuid);
        name = uuidToNameCache.get(dashedUuid);
        if (name != null) {
            return name;
        }
        
        // Not found, return original UUID
        logger.debug("UUID not found in playerdata: {}", uuid);
        return uuid;
    }
    
    /**
     * Convert UUID without dashes to standard format with dashes
     */
    private String toDashedFormat(String uuid) {
        if (uuid.length() != 32) {
            return uuid;
        }
        return uuid.substring(0, 8) + "-" +
               uuid.substring(8, 12) + "-" +
               uuid.substring(12, 16) + "-" +
               uuid.substring(16, 20) + "-" +
               uuid.substring(20);
    }
    
    /**
     * Check if a string looks like a UUID
     */
    public static boolean isUUID(String str) {
        if (str == null) return false;
        String normalized = str.replace("-", "");
        return normalized.length() == 32 && normalized.matches("[0-9a-fA-F]+");
    }
    
    /**
     * Get the cache size
     */
    public int getCacheSize() {
        return uuidToNameCache.size();
    }
}
