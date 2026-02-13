package cn.frkovo.converter.model.old;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Old format frame model - contains notes at a specific tick
 */
@Data
public class OldFrame {
    private long judgeTick;
    private List<OldNote> notes;
    
    public static OldFrame fromJson(JSONObject json) {
        OldFrame frame = new OldFrame();
        frame.setJudgeTick(json.getLongValue("judge-tick"));
        
        List<OldNote> notes = new ArrayList<>();
        JSONArray notesArray = json.getJSONArray("notes");
        if (notesArray != null) {
            for (int i = 0; i < notesArray.size(); i++) {
                notes.add(OldNote.fromJson(notesArray.getJSONObject(i)));
            }
        }
        frame.setNotes(notes);
        return frame;
    }
}
