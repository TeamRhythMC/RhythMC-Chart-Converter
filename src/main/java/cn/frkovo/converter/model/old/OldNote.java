package cn.frkovo.converter.model.old;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.Objects;

/**
 * Old format note model
 */
@Data
public class OldNote {
    private String type;  // NOTE_CLICK, NOTE_LEFT_CLICK, NOTE_RIGHT_CLICK, NOTE_LOOK, NOTE_HOLD, NOTE_DO_NOT_CLICK
    private float posX;
    private float posY;
    private Integer length;  // Only for NOTE_HOLD
    
    public static OldNote fromJson(JSONObject json) {
        OldNote note = new OldNote();
        note.setType(json.getString("type"));
        
        if(Objects.equals(note.getType(), "NOTE_HOLD")){
            note.setPosX(json.getFloat("pos"));
            note.setPosY(-1);
        }else{
            JSONArray pos = json.getJSONArray("pos");
            if (pos != null && pos.size() >= 2) {
                note.setPosX(pos.getFloatValue(0));
                note.setPosY(pos.getFloatValue(1));
            }
        }
        note.setLength(json.getInteger("length"));
        return note;
    }
}
