package cn.frkovo.converter.model.new_;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;

/**
 * New format note model
 */
@Data
public class NewNote {
    private int noteType;  // 0=TAP, 1=LOOK, 2=HOLD, 3=DODGE
    private double beat;
    private double posX;
    private double posY;
    private double posZ;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float scaleZ = 1.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    private int holdGroup = -1;  // -1 = not part of any hold, >=0 = group ID for HOLD notes
    
    public JSONArray toPosArray() {
        JSONArray arr = new JSONArray();
        arr.add(posX);
        arr.add(posY);
        arr.add(posZ);
        return arr;
    }
    
    public JSONArray toScaleArray() {
        JSONArray arr = new JSONArray();
        arr.add(scaleX);
        arr.add(scaleY);
        arr.add(scaleZ);
        return arr;
    }
    
    public JSONArray toRotationArray() {
        JSONArray arr = new JSONArray();
        arr.add(rotationX);
        arr.add(rotationY);
        arr.add(rotationZ);
        return arr;
    }
}
