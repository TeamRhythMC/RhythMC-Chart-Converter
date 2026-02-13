package cn.frkovo.converter.mapper;

import cn.frkovo.converter.model.new_.NewEffect;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.frkovo.converter.model.old.OldEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps old effect types to new effect types
 * 
 * Mappings:
 * - HOLOGRAM -> HOLOGRAM
 * - REMOVEHOLOGRAM -> REMOVE_HOLOGRAM
 * - INVERT -> EFFECT (BLINDNESS)
 * - MESSAGE -> MESSAGE
 * - EFFECT -> EFFECT
 * - CLREFFECT -> CLEAR_EFFECT
 * - TIME -> TIME
 * - WEATHER -> WEATHER
 * - COLOR -> GLOW_COLOR
 * - ARENA -> ARENA
 * - VISIBLE -> HIDE_NOTES (logic inverted)
 * - SPEED -> SKIP
 * - JUDGEDOT -> SKIP
 * - TEXT -> TEXT_DISPLAY
 * - TRANSFORMATIONS -> TEXT_DISPLAY_EFFECT
 */
public class EffectTypeMapper {
    
    // Potion effect IDs for Minecraft
    // BLINDNESS = 15
    private static final int BLINDNESS_EFFECT_ID = 15;
    
    /**
     * Converts an old effect to a new effect
     * 
     * @param oldEffect The old effect to convert
     * @return New effect, or null if the effect should be skipped
     */
    public static NewEffect map(OldEffect oldEffect) {
        if (oldEffect == null || oldEffect.getEffectType() == null) {
            return null;
        }
        
        String oldType = oldEffect.getEffectType().toUpperCase();
        double beat = oldEffect.getStartTick();  // tick = beat when BPM = 1200
        
        return switch (oldType) {
            case "HOLOGRAM" -> mapHologram(oldEffect, beat);
            case "REMOVEHOLOGRAM" -> mapRemoveHologram(oldEffect, beat);
            case "INVERT" -> mapInvert(oldEffect, beat);
            case "MESSAGE" -> mapMessage(oldEffect, beat);
            case "EFFECT" -> mapEffect(oldEffect, beat);
            case "CLREFFECT" -> mapClearEffect(oldEffect, beat);
            case "TIME" -> mapTime(oldEffect, beat);
            case "WEATHER" -> mapWeather(oldEffect, beat);
            case "COLOR" -> mapColor(oldEffect, beat);
            case "ARENA" -> mapArena(oldEffect, beat);
            case "VISIBLE" -> mapVisible(oldEffect, beat);
            case "TEXT" -> mapText(oldEffect, beat);
            case "TRANSFORMATIONS" -> mapTransformations(oldEffect, beat);
            case "SPEED", "JUDGEDOT" -> null;  // Skip these effects
            default -> {
                // Log unknown effect type but don't skip
                System.err.println("Unknown effect type: " + oldType);
                yield null;
            }
        };
    }
    
    private static NewEffect mapHologram(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        
        // Location
        JSONArray location = new JSONArray();
        if (old.getHologramLoc() != null) {
            location = old.getHologramLoc();
        } else {
            location.add(0.0);
            location.add(1.5);
            location.add(0.0);
        }
        props.put("location", location);
        
        // ID
        String id = old.getId() != null ? old.getId() : "RhyMCGameHologram_" + System.currentTimeMillis();
        props.put("id", id);
        
        // Contents
        JSONArray contents = new JSONArray();
        if (old.getHologramContents() != null) {
            contents.addAll(old.getHologramContents());
        }
        props.put("contents", contents);
        
        // Duration (convert ticks to milliseconds)
        long duration = old.getDuration() != null ? old.getDuration() * 50L : 31536000000L;
        props.put("duration", duration);
        
        return new NewEffect("HOLOGRAM", beat, props);
    }
    
    private static NewEffect mapRemoveHologram(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("id", old.getId());
        return new NewEffect("REMOVE_HOLOGRAM", beat, props);
    }
    
    private static NewEffect mapInvert(OldEffect old, double beat) {
        // INVERT was a blindness effect, convert to EFFECT with BLINDNESS
        JSONObject props = new JSONObject();
        props.put("effectId", BLINDNESS_EFFECT_ID);
        props.put("amplifier", 1);
        
        // Duration (convert ticks to milliseconds)
        long duration = old.getDuration() != null ? old.getDuration() * 50L : 31536000000L;
        props.put("duration", duration);
        
        return new NewEffect("EFFECT", beat, props);
    }
    
    private static NewEffect mapMessage(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        JSONArray contents = new JSONArray();
        if (old.getContents() != null) {
            contents.addAll(old.getContents());
        }
        props.put("contents", contents);
        return new NewEffect("MESSAGE", beat, props);
    }
    
    private static NewEffect mapEffect(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        
        // Map effect ID string to integer if needed
        String effectIdStr = old.getEffectId();
        int effectId = mapPotionEffectId(effectIdStr);
        props.put("effectId", effectId);
        props.put("amplifier", old.getAmplifier() != null ? old.getAmplifier() : 0);
        
        // Duration (convert ticks to milliseconds)
        long duration = old.getDuration() != null ? old.getDuration() * 50L : 31536000000L;
        props.put("duration", duration);
        
        return new NewEffect("EFFECT", beat, props);
    }
    
    private static NewEffect mapClearEffect(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        if (old.getEffectId() != null) {
            props.put("effectId", mapPotionEffectId(old.getEffectId()));
        }
        return new NewEffect("CLEAR_EFFECT", beat, props);
    }
    
    private static NewEffect mapTime(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("time", old.getTime() != null ? old.getTime() : 6000L);
        
        // Duration (convert ticks to milliseconds)
        long duration = old.getDuration() != null ? old.getDuration() * 50L : 31536000000L;
        props.put("duration", duration);
        
        return new NewEffect("TIME", beat, props);
    }
    
    private static NewEffect mapWeather(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("weather", old.getWeather() != null ? old.getWeather() : "CLEAR");
        return new NewEffect("WEATHER", beat, props);
    }
    
    private static NewEffect mapColor(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("color", old.getColor() != null ? old.getColor() : "WHITE");
        return new NewEffect("GLOW_COLOR", beat, props);
    }
    
    private static NewEffect mapArena(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("arena", old.getArena());
        return new NewEffect("ARENA", beat, props);
    }
    
    private static NewEffect mapVisible(OldEffect old, double beat) {
        // VISIBLE in old format: true = show notes, false = hide notes
        // HIDE_NOTES in new format: specifies which notes to hide
        // Logic is inverted!
        JSONObject props = new JSONObject();
        
        if (old.getVisible() != null && !old.getVisible()) {
            // Old: visible=false, so hide notes
            // New: specify which note types to hide
            JSONArray noteTypes = new JSONArray();
            if (old.getNoteTypes() != null) {
                for (String type : old.getNoteTypes()) {
                    noteTypes.add(NoteTypeMapper.getName(NoteTypeMapper.map(type)));
                }
            } else {
                // Hide all note types by default
                noteTypes.add("TAP");
                noteTypes.add("LOOK");
                noteTypes.add("HOLD");
                noteTypes.add("DODGE");
            }
            props.put("noteTypes", noteTypes);
            props.put("tracks", new JSONArray());
        } else {
            // Old: visible=true or null, so show all notes
            // New: empty arrays mean show all
            props.put("noteTypes", new JSONArray());
            props.put("tracks", new JSONArray());
        }
        
        return new NewEffect("HIDE_NOTES", beat, props);
    }
    
    private static NewEffect mapText(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("id", old.getId() != null ? old.getId() : "text_" + System.currentTimeMillis());
        props.put("text", old.getContent());
        
        // Position
        JSONArray position = new JSONArray();
        if (old.getLoc() != null) {
            position = old.getLoc();
        } else {
            position.add(0.0);
            position.add(1.5);
            position.add(0.0);
        }
        props.put("position", position);
        
        // Default rotation and scale
        JSONArray rotation = new JSONArray();
        rotation.add(0.0);
        rotation.add(0.0);
        rotation.add(0.0);
        props.put("rotation", rotation);
        
        JSONArray scale = new JSONArray();
        scale.add(1.0);
        scale.add(1.0);
        scale.add(1.0);
        props.put("scale", scale);
        
        return new NewEffect("TEXT_DISPLAY", beat, props);
    }
    
    private static NewEffect mapTransformations(OldEffect old, double beat) {
        JSONObject props = new JSONObject();
        props.put("id", old.getId());
        props.put("type", old.getType());
        
        if (old.getTo() != null) {
            props.put("to", old.getTo());
        }
        if (old.getRotate() != null) {
            props.put("rotate", old.getRotate());
        }
        if (old.getScale() != null) {
            props.put("scale", old.getScale());
        }
        if (old.getShadow() != null) {
            props.put("shadow", old.getShadow());
        }
        if (old.getGlowing() != null) {
            props.put("glowing", old.getGlowing());
        }
        if (old.getBackgroundColor() != null) {
            props.put("color", old.getBackgroundColor());
        }
        if (old.getContent() != null) {
            props.put("content", old.getContent());
        }
        if (old.getDuration() != null) {
            props.put("duration", old.getDuration());
        }
        
        return new NewEffect("TEXT_DISPLAY_EFFECT", beat, props);
    }
    
    /**
     * Maps potion effect ID string to integer
     * Based on Bukkit PotionEffectType constants
     */
    private static int mapPotionEffectId(String effectId) {
        if (effectId == null) {
            return 1;  // Default to SPEED
        }
        
        return switch (effectId.toUpperCase()) {
            case "SPEED" -> 1;
            case "SLOWNESS", "SLOW" -> 2;
            case "HASTE" -> 3;
            case "MINING_FATIGUE" -> 4;
            case "STRENGTH" -> 5;
            case "INSTANT_HEALTH" -> 6;
            case "INSTANT_DAMAGE" -> 7;
            case "JUMP_BOOST" -> 8;
            case "NAUSEA" -> 9;
            case "REGENERATION" -> 10;
            case "RESISTANCE" -> 11;
            case "FIRE_RESISTANCE" -> 12;
            case "WATER_BREATHING" -> 13;
            case "INVISIBILITY" -> 14;
            case "BLINDNESS" -> 15;
            case "NIGHT_VISION" -> 16;
            case "HUNGER" -> 17;
            case "WEAKNESS" -> 18;
            case "POISON" -> 19;
            case "WITHER" -> 20;
            case "HEALTH_BOOST" -> 21;
            case "ABSORPTION" -> 22;
            case "SATURATION" -> 23;
            case "GLOWING" -> 24;
            case "LEVITATION" -> 25;
            case "LUCK" -> 26;
            case "UNLUCK", "BAD_LUCK" -> 27;
            case "SLOW_FALLING" -> 28;
            case "CONDUIT_POWER" -> 29;
            case "DOLPHINS_GRACE" -> 30;
            case "BAD_OMEN" -> 31;
            case "HERO_OF_THE_VILLAGE" -> 32;
            case "DARKNESS" -> 33;
            case "TRIAL_OMEN" -> 34;
            case "RAID_OMEN" -> 35;
            case "WIND_CHARGED" -> 36;
            case "WEAVING" -> 37;
            case "OOZING" -> 38;
            case "INFESTED" -> 39;
            default -> {
                // Try parsing as integer
                try {
                    yield Integer.parseInt(effectId);
                } catch (NumberFormatException e) {
                    yield 1;  // Default to SPEED
                }
            }
        };
    }
}
