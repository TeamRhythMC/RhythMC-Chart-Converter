package cn.frkovo.converter.model.new_;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * New format track model
 */
@Data
public class NewTrack {
    private int id;
    private List<NewNumEvent> speedEvents = new ArrayList<>();
    private List<NewNumEvent> xTransformEvents = new ArrayList<>();
    private List<NewNumEvent> yTransformEvents = new ArrayList<>();
    private List<NewNumEvent> zTransformEvents = new ArrayList<>();
    private List<NewNumEvent> xRotateEvents = new ArrayList<>();
    private List<NewNumEvent> yRotateEvents = new ArrayList<>();
    private List<NewNumEvent> zRotateEvents = new ArrayList<>();
    private List<NewNumEvent> xScaleEvents = new ArrayList<>();
    private List<NewNumEvent> yScaleEvents = new ArrayList<>();
    private List<NewNumEvent> zScaleEvents = new ArrayList<>();
    private List<NewNote> notes = new ArrayList<>();
}
