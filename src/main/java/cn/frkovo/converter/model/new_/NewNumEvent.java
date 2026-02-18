package cn.frkovo.converter.model.new_;

import lombok.Data;

/**
 * New format numeric event model (for track transformations)
 */
@Data
public class NewNumEvent {
    private double startBeat;
    private double endBeat;
    private double startValue;
    private double endValue;
    private int easing = 0;  // 0 = LINEAR
    
    public NewNumEvent() {}
    
    public NewNumEvent(double beat, double value) {
        this.startBeat = beat;
        this.endBeat = beat;
        this.startValue = value;
        this.endValue = value;
    }
    public NewNumEvent(double startBeat, double endBeat, double startValue, double endValue, int easing) {
        this.startBeat = startBeat;
        this.endBeat = endBeat;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easing = easing;
    }
}
