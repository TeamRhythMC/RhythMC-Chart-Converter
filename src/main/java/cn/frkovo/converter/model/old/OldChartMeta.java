package cn.frkovo.converter.model.old;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * Old format chart JSON meta section
 */
@Data
public class OldChartMeta {
    private String charter;
    private String charter_alias;
    private String uuid;
    private String initial_arena;
    private float flow_speed;
    private int offset;  // in ticks
    private float level;
    private List<String> comments;
    private List<String> coop_charters;
    
    public static OldChartMeta fromJson(JSONObject json) {
        OldChartMeta meta = new OldChartMeta();
        meta.setCharter(json.getString("charter"));
        meta.setCharter_alias(json.getString("charter-alias"));
        meta.setUuid(json.getString("uuid"));
        meta.setInitial_arena(json.getString("initial-arena"));
        meta.setFlow_speed(json.getFloatValue("flow-speed"));
        meta.setOffset(json.getIntValue("offset"));
        meta.setLevel(json.getFloatValue("level"));
        meta.setComments(json.getList("comments", String.class));
        meta.setCoop_charters(json.getList("coop-charters", String.class));
        return meta;
    }
}
