package cn.frkovo.converter.converter;

import cn.frkovo.converter.Main;
import cn.frkovo.converter.mapper.EffectTypeMapper;
import cn.frkovo.converter.mapper.NoteTypeMapper;
import cn.frkovo.converter.model.new_.NewEffect;
import cn.frkovo.converter.model.new_.NewNote;
import cn.frkovo.converter.model.new_.NewNumEvent;
import cn.frkovo.converter.model.new_.NewTrack;
import cn.frkovo.converter.model.old.OldChart;
import cn.frkovo.converter.model.old.OldChartMeta;
import cn.frkovo.converter.model.old.OldEffect;
import cn.frkovo.converter.model.old.OldFrame;
import cn.frkovo.converter.model.old.OldMetadata;
import cn.frkovo.converter.model.old.OldNote;
import cn.frkovo.converter.util.UUIDResolver;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Converts old chart format to new chart format
 */
public class ChartConverter {
    private static final Logger logger = LoggerFactory.getLogger(ChartConverter.class);
    
    // Difficulty file names
    private static final String[] DIFFICULTY_FILES = {"world.json", "nether.json", "end.json", "void.json"};
    
    // BPM setting
    private static final double BPM = 1200.0;
    
    public void convert(File chartFolder, int songId) throws IOException {
        // Read old metadata
        File metadataFile = new File(chartFolder, "metadata.yml");
        if (!metadataFile.exists()) {
            logger.warn("metadata.yml not found in {}", chartFolder.getName());
            return;
        }
        
        OldMetadata oldMetadata = OldMetadata.fromFile(metadataFile.getAbsolutePath());
        
        // Create output folder
        String folderName = String.valueOf(songId);
        Path outputFolder = Path.of(Main.getOutputDir(), "Charts", folderName);
        Files.createDirectories(outputFolder);
        
        // Convert and write new metadata
        convertMetadata(oldMetadata, songId, outputFolder);
        
        // Convert each difficulty
        for (String difficultyFile : DIFFICULTY_FILES) {
            File chartFile = new File(chartFolder, difficultyFile);
            if (chartFile.exists()) {
                OldChart oldChart = OldChart.fromFile(chartFile);
                convertChart(oldMetadata, oldChart, difficultyFile.replace(".json", ""), outputFolder);
            }
        }
        
         logger.info("Converted chart: {} -> {}", oldMetadata.getName(), folderName);
    }
    
    private void convertMetadata(OldMetadata old, int songId, Path outputFolder) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", old.getName() != null ? old.getName() : "Unknown");
        metadata.put("composer", old.getComposer() != null ? old.getComposer() : "Unknown");
        metadata.put("icon", old.getIcon() != null ? old.getIcon() : "NOTE_BLOCK");
        metadata.put("alias", old.getAlias() != null ? old.getAlias() : "");
        metadata.put("length", old.getLength() * 50);  // Convert ticks to milliseconds
        metadata.put("base_bpm", BPM);
        metadata.put("respack_sha1", old.getRespack_sha1());
        metadata.put("description", "");
        metadata.put("song_id", songId);
        metadata.put("version", old.getVersion() != null ? old.getVersion() : "1.0");
        metadata.put("comments", new ArrayList<>());
        metadata.put("player-alias", new ArrayList<>());
        metadata.put("tags", new ArrayList<>());
        metadata.put("unlockSong", new ArrayList<>());
        metadata.put("unlockWorld", new ArrayList<>());
        metadata.put("unlockNether", new ArrayList<>());
        metadata.put("unlockVoid", new ArrayList<>());
        
        // Write metadata.yml
        try (FileWriter writer = new FileWriter(outputFolder.resolve("manifest.yml").toFile())) {
            yaml.dump(metadata, writer);
        }
    }
    
    private void convertChart(OldMetadata om, OldChart oldChart, String difficulty, Path outputFolder) throws IOException {
        JSONObject newChart = new JSONObject();
        
        // Convert meta
        JSONObject meta = convertChartMeta(oldChart.getMeta(), difficulty);
        newChart.put("meta", meta);
        
        // Convert frames to tracks
        List<NewTrack> tracks = convertFramesToTracks(oldChart.getFrames());
        JSONArray tracksArray = new JSONArray();
        for (NewTrack track : tracks) {
            tracksArray.add(convertTrackToJson(om.getLength(), track));
        }
        newChart.put("tracks", tracksArray);
        
        // Convert effects
        JSONArray eventsArray = new JSONArray();
        if (oldChart.getEffects() != null) {
            for (OldEffect oldEffect : oldChart.getEffects()) {
                NewEffect newEffect = EffectTypeMapper.map(oldEffect);
                if (newEffect != null) {
                    eventsArray.add(convertEffectToJson(newEffect));
                }
            }
        }
        newChart.put("effects", eventsArray);
        
        // Write chart JSON
        String fileName = difficulty + ".rmcc";
        Files.writeString(outputFolder.resolve(fileName), newChart.toJSONString(JSONWriter.Feature.PrettyFormat));
    }
    
    private JSONObject convertChartMeta(OldChartMeta old, String difficulty) {
        JSONObject meta = new JSONObject();
        
        // Charters - resolve UUIDs to names
        JSONArray charters = new JSONArray();
        UUIDResolver resolver = Main.getUuidResolver();
        
        if (old != null) {
            if (old.getCharter_alias() != null) {
                charters.add(old.getCharter_alias());
            } else if (old.getCharter() != null) {
                String charter = old.getCharter();
                // Try to resolve UUID to name
                if (resolver != null && resolver.isUUID(charter)) {
                    String resolved = resolver.resolve(charter);
                    charters.add(resolved != null ? resolved : charter);
                } else {
                    charters.add(charter);
                }
            }
            if (old.getCoop_charters() != null) {
                for (Object coopCharter : old.getCoop_charters()) {
                    if (coopCharter instanceof String) {
                        String coopStr = (String) coopCharter;
                        if (resolver != null && resolver.isUUID(coopStr)) {
                            String resolved = resolver.resolve(coopStr);
                            charters.add(resolved != null ? resolved : coopStr);
                        } else {
                            charters.add(coopStr);
                        }
                    } else {
                        charters.add(coopCharter);
                    }
                }
            }
        }
        if (charters.isEmpty()) {
            charters.add("Unknown");
        }
        meta.put("charters", charters);
        
        // Level
        meta.put("level", old != null && old.getLevel() > 0 ? Math.round(old.getLevel() * 10.0) / 10.0 : 1.0); // round to 1 decimal place
        
        // Offset (convert ticks to milliseconds)
        meta.put("offset", old != null ? old.getOffset() * 50L : 0L);
        
        // Flow speed
        meta.put("flow-speed", old != null && old.getFlow_speed() > 0 ? old.getFlow_speed() : 1.0f);
        
        // UID - generate based on difficulty
        // For levels, uid = 1xxxxx + n (world = 1, nether = 2, end = 3, void = 4)
        int diffIndex = getDifficultyIndex(difficulty);
        meta.put("uid", 100000 + diffIndex);
        
        // Comments
        JSONArray comments = new JSONArray();
        if (old != null && old.getComments() != null) {
            comments.addAll(old.getComments());
        }
        meta.put("comments", comments);
        
        // BPMs
        JSONArray bpms = new JSONArray();
        JSONObject bpmEntry = new JSONObject();
        bpmEntry.put("beat", 0);
        bpmEntry.put("bpm", BPM);
        bpms.add(bpmEntry);
        meta.put("bpms", bpms);
        
        // Initial arena
        if (old != null && old.getInitial_arena() != null) {
            meta.put("initialArena", old.getInitial_arena());
        }
        meta.remove("flow-speed"); // Remove flow-speed as it's deprecated
        return meta;
    }
    
    private int getDifficultyIndex(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "world" -> 1;
            case "nether" -> 2;
            case "end" -> 3;
            case "void" -> 4;
            default -> 1;
        };
    }
    
    private List<NewTrack> convertFramesToTracks(List<OldFrame> frames) {
        // Group notes by position to create tracks
        // For simplicity, we'll create a single track with all notes
        // In a more sophisticated implementation, we could create multiple tracks
        
        NewTrack mainTrack = new NewTrack();
        mainTrack.setId(0);
        
        if (frames != null) {
            for (OldFrame frame : frames) {
                if (frame.getNotes() != null) {
                    for (OldNote oldNote : frame.getNotes()) {
                        List<NewNote> newNotes = convertNote(oldNote, frame.getJudgeTick());
                        if (newNotes != null) {
                            mainTrack.getNotes().addAll(newNotes);
                        }
                    }
                }
            }
        }
        
        // Sort notes by beat
        mainTrack.getNotes().sort(Comparator.comparingDouble(NewNote::getBeat));
        
        List<NewTrack> tracks = new ArrayList<>();
        tracks.add(mainTrack);
        return tracks;
    }
    
    /**
     * Convert old note to new note(s).
     * HOLD notes are expanded to multiple notes from beat to beat+length (one per beat).
     * Other note types return a single note.
     */
    private List<NewNote> convertNote(OldNote old, long judgeTick) {
        if (old == null) {
            return null;
        }
        
        List<NewNote> result = new ArrayList<>();
        int noteType = NoteTypeMapper.map(old.getType());
        
        // For HOLD notes, expand to multiple notes with default position (0, -1, 0)
        if (noteType == NoteTypeMapper.HOLD && old.getLength() != null && old.getLength() > 0) {
            int length = old.getLength();
            for (int i = 0; i <= length; i++) {
                NewNote note = new NewNote();
                note.setNoteType(noteType);
                note.setBeat(judgeTick + i);  // tick = beat when BPM = 1200
                note.setPosX(0);
                note.setPosY(-1);
                note.setPosZ(0);
                result.add(note);
            }
        } else {
            // For non-HOLD notes, return single note
            NewNote note = new NewNote();
            note.setNoteType(noteType);
            note.setBeat(judgeTick);  // tick = beat when BPM = 1200
            note.setPosX(old.getPosX());
            note.setPosY(old.getPosY());
            note.setPosZ(0);
            result.add(note);
        }
        
        return result;
    }
    
    private JSONObject convertTrackToJson(int l ,NewTrack track) {
        JSONObject json = new JSONObject();
        json.put("id", track.getId());
        
        // Speed events - default: speed 1.0 from start to end
        JSONArray speedEvents = new JSONArray();
        speedEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 1.0f, 1.0f, 0)));
        json.put("speedEvents", speedEvents);
        
        // Transform events - default: position 0 from start to end
        JSONArray xTransformEvents = new JSONArray();
        xTransformEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("xTransformEvents", xTransformEvents);
        
        JSONArray yTransformEvents = new JSONArray();
        yTransformEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("yTransformEvents", yTransformEvents);
        
        JSONArray zTransformEvents = new JSONArray();
        zTransformEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("zTransformEvents", zTransformEvents);
        
        // Rotate events - default: rotation 0 from start to end
        JSONArray xRotateEvents = new JSONArray();
        xRotateEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("xRotateEvents", xRotateEvents);
        
        JSONArray yRotateEvents = new JSONArray();
        yRotateEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("yRotateEvents", yRotateEvents);
        
        JSONArray zRotateEvents = new JSONArray();
        zRotateEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 0.0f, 0.0f, 0)));
        json.put("zRotateEvents", zRotateEvents);
        
        // Scale events - default: scale 1.0 from start to end
        JSONArray xScaleEvents = new JSONArray();
        xScaleEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 1.0f, 1.0f, 0)));
        json.put("xScaleEvents", xScaleEvents);
        
        JSONArray yScaleEvents = new JSONArray();
        yScaleEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 1.0f, 1.0f, 0)));
        json.put("yScaleEvents", yScaleEvents);
        
        JSONArray zScaleEvents = new JSONArray();
        zScaleEvents.add(convertNumEventToJson(new NewNumEvent(0, l, 1.0f, 1.0f, 0)));
        json.put("zScaleEvents", zScaleEvents);
        
        // Notes
        JSONArray notes = new JSONArray();
        for (NewNote note : track.getNotes()) {
            notes.add(convertNoteToJson(note));
        }
        json.put("notes", notes);
        
        return json;
    }
    
    private JSONArray convertNumEventsToJson(List<NewNumEvent> events) {
        JSONArray array = new JSONArray();
        for (NewNumEvent event : events) {
            array.add(convertNumEventToJson(event));
        }
        return array;
    }
    
    private JSONObject convertNumEventToJson(NewNumEvent event) {
        JSONObject json = new JSONObject();
        json.put("startBeat", event.getStartBeat());
        json.put("endBeat", event.getEndBeat());
        json.put("startValue", event.getStartValue());
        json.put("endValue", event.getEndValue());
        json.put("easing", event.getEasing());
        return json;
    }
    
    private JSONObject convertNoteToJson(NewNote note) {
        JSONObject json = new JSONObject();
        json.put("noteType", note.getNoteType());
        json.put("beat", note.getBeat());
        json.put("pos", note.toPosArray());
        json.put("scale", note.toScaleArray());
        json.put("rotation", note.toRotationArray());
        return json;
    }
    
    private JSONObject convertEffectToJson(NewEffect effect) {
        JSONObject json = new JSONObject();
        json.put("effectType", effect.getEventType());
        json.put("beat", effect.getBeat());
        json.put("properties", effect.getProperties());
        return json;
    }
}
