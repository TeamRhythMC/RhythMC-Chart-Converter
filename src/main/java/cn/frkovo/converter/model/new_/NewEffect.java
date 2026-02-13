package cn.frkovo.converter.model.new_;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * New format effect model
 */
@Data
public class NewEffect {
    private String eventType;
    private double beat;
    private JSONObject properties;
    
    public NewEffect() {}
    
    public NewEffect(String eventType, double beat, JSONObject properties) {
        this.eventType = eventType;
        this.beat = beat;
        this.properties = properties;
    }
}
