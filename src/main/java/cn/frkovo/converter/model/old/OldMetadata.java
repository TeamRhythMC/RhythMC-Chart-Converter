package cn.frkovo.converter.model.old;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Old format metadata.yml model
 */
@Data
public class OldMetadata {
    private String name;
    private String respack_sha1;
    private String composer;
    private int length;  // in ticks
    private String icon;
    private String version;
    private String alias;
    
    public static OldMetadata fromFile(String filePath) throws IOException {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(options);
        try (FileReader reader = new FileReader(filePath)) {
            // 使用Map来加载，避免未知属性报错
            Map<String, Object> data = yaml.load(reader);
            OldMetadata metadata = new OldMetadata();
            metadata.setName((String) data.get("name"));
            metadata.setRespack_sha1((String) data.get("respack_sha1"));
            metadata.setComposer((String) data.get("composer"));
            metadata.setLength(data.get("length") != null ? ((Number) data.get("length")).intValue() : 0);
            metadata.setIcon((String) data.get("icon"));
            metadata.setAlias((String) data.get("alias"));
            metadata.setVersion((String) data.get("version"));
            return metadata;
        }
    }
}
