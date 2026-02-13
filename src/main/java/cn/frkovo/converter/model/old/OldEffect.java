package cn.frkovo.converter.model.old;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Old format effect model
 */
@Data
public class OldEffect {
    private long startTick;
    private String effectType;
    private JSONObject rawData;  // Store raw data for property extraction
    
    // Common properties
    private Integer duration;
    
    // HOLOGRAM properties
    private List<String> hologramContents;
    private JSONArray hologramLoc;
    private String id;
    
    // MESSAGE properties
    private List<String> contents;
    
    // EFFECT properties
    private String effectId;
    private Integer amplifier;
    
    // TIME properties
    private Long time;
    
    // WEATHER properties
    private String weather;
    
    // COLOR properties
    private String color;
    
    // ARENA properties
    private String arena;
    
    // SPEED properties
    private Float speed;
    
    // VISIBLE properties
    private Boolean visible;
    private List<String> noteTypes;
    
    // TEXT properties
    private JSONArray loc;
    private String content;
    
    // TRANSFORMATIONS properties
    private String type;
    private JSONArray to;
    private Float scale;
    private Double rotate;
    private Boolean shadow;
    private Boolean glowing;
    private JSONArray backgroundColor;
    
    public static OldEffect fromJson(JSONObject json) {
        OldEffect effect = new OldEffect();
        effect.setStartTick(json.getLongValue("start-tick"));
        effect.setEffectType(json.getString("effect-type"));
        effect.setRawData(json);
        
        // Common properties
        effect.setDuration(json.getInteger("duration"));
        
        // HOLOGRAM properties
        effect.setHologramContents(json.getList("hologram-contents", String.class));
        effect.setHologramLoc(json.getJSONArray("hologram-loc"));
        effect.setId(json.getString("id"));
        
        // MESSAGE properties
        effect.setContents(json.getList("contents", String.class));
        
        // EFFECT properties
        effect.setEffectId(json.getString("effect-id"));
        effect.setAmplifier(json.getInteger("amplifier"));
        
        // TIME properties
        effect.setTime(json.getLong("time"));
        
        // WEATHER properties
        effect.setWeather(json.getString("weather"));
        
        // COLOR properties
        effect.setColor(json.getString("color"));
        
        // ARENA properties
        effect.setArena(json.getString("arena"));
        
        // SPEED properties
        effect.setSpeed(json.getFloat("speed"));
        
        // VISIBLE properties
        effect.setVisible(json.getBoolean("visible"));
        effect.setNoteTypes(json.getList("note-types", String.class));
        
        // TEXT properties
        effect.setLoc(json.getJSONArray("loc"));
        effect.setContent(json.getString("content"));
        
        // TRANSFORMATIONS properties
        effect.setType(json.getString("type"));
        effect.setTo(json.getJSONArray("to"));
        effect.setScale(json.getFloat("scale"));
        effect.setRotate(json.getDouble("rotate"));
        effect.setShadow(json.getBoolean("shadow"));
        effect.setGlowing(json.getBoolean("glowing"));
        // For TRANSFORMATIONS type, color is an array (background color)
        // For COLOR type, color is a string (already set above)
        if ("TRANSFORMATIONS".equals(effect.getEffectType())) {
            effect.setBackgroundColor(json.getJSONArray("color"));
        }
        
        return effect;
    }
}
