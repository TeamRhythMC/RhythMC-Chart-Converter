package cn.frkovo.converter.model.old;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Old format chart JSON model
 */
@Data
public class OldChart {
    private OldChartMeta meta;
    private List<OldFrame> frames;
    private List<OldEffect> effects;
    
    public static OldChart fromFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        JSONObject json = JSONObject.parseObject(content);
        
        OldChart chart = new OldChart();
        
        // Parse meta
        JSONObject metaJson = json.getJSONObject("meta");
        if (metaJson != null) {
            chart.setMeta(OldChartMeta.fromJson(metaJson));
        }
        
        // Parse frames
        List<OldFrame> frames = new ArrayList<>();
        JSONArray framesArray = json.getJSONArray("frames");
        if (framesArray != null) {
            for (int i = 0; i < framesArray.size(); i++) {
                frames.add(OldFrame.fromJson(framesArray.getJSONObject(i)));
            }
        }
        chart.setFrames(frames);
        
        // Parse effects
        List<OldEffect> effects = new ArrayList<>();
        JSONArray effectsArray = json.getJSONArray("effects");
        if (effectsArray != null) {
            for (int i = 0; i < effectsArray.size(); i++) {
                effects.add(OldEffect.fromJson(effectsArray.getJSONObject(i)));
            }
        }
        chart.setEffects(effects);
        
        return chart;
    }
}
