package cn.frkovo.converter.model.old;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Old format arena YAML model
 */
@Data
public class OldArena {
    private String arenaDisplayname;
    private String arenaName;
    private int price;
    private boolean canBuy;
    private String arenaIcon;
    private String schematicFile;
    private Map<String, String> effects;
    
    public static OldArena fromFile(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(filePath)) {
            Map<String, Object> data = yaml.load(reader);
            
            OldArena arena = new OldArena();
            arena.setArenaDisplayname((String) data.get("arena-displayname"));
            arena.setArenaName((String) data.get("arena-name"));
            arena.setPrice(data.get("price") != null ? ((Number) data.get("price")).intValue() : 0);
            arena.setCanBuy(data.get("can-buy") != null && (Boolean) data.get("can-buy"));
            arena.setArenaIcon((String) data.get("arena-icon"));
            arena.setSchematicFile((String) data.get("schematic-file"));
            
            @SuppressWarnings("unchecked")
            Map<String, String> effects = (Map<String, String>) data.get("effects");
            arena.setEffects(effects);
            
            return arena;
        }
    }
    
    public String getNormalJudgeBoxMaterial() {
        return effects != null ? effects.get("normal-judge-box-material") : null;
    }
    
    public String getReversedJudgeBoxMaterial() {
        return effects != null ? effects.get("reversed-judge-box-material") : null;
    }
}
