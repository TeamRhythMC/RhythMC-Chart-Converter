package cn.frkovo.converter.mapper;

/**
 * Maps old note types to new note types
 * 
 * Old types:
 * - NOTE_CLICK, NOTE_LEFT_CLICK, NOTE_RIGHT_CLICK -> TAP (0)
 * - NOTE_LOOK -> LOOK (1)
 * - NOTE_HOLD -> HOLD (2)
 * - NOTE_DO_NOT_CLICK -> DODGE (3)
 */
public class NoteTypeMapper {
    
    public static final int TAP = 0;
    public static final int LOOK = 1;
    public static final int HOLD = 2;
    public static final int DODGE = 3;
    
    /**
     * Maps old note type string to new note type integer
     * 
     * @param oldType Old note type string (e.g., "NOTE_CLICK", "NOTE_LOOK")
     * @return New note type integer (0=TAP, 1=LOOK, 2=HOLD, 3=DODGE)
     */
    public static int map(String oldType) {
        if (oldType == null) {
            return TAP;  // Default to TAP
        }
        
        return switch (oldType.toUpperCase()) {
            case "NOTE_CLICK", "NOTE_LEFT_CLICK", "NOTE_RIGHT_CLICK" -> TAP;
            case "NOTE_LOOK" -> LOOK;
            case "NOTE_HOLD" -> HOLD;
            case "NOTE_DO_NOT_CLICK" -> DODGE;
            default -> TAP;  // Default to TAP for unknown types
        };
    }
    
    /**
     * Gets the new note type name from the integer value
     * 
     * @param type New note type integer
     * @return New note type name (TAP, LOOK, HOLD, DODGE)
     */
    public static String getName(int type) {
        return switch (type) {
            case TAP -> "TAP";
            case LOOK -> "LOOK";
            case HOLD -> "HOLD";
            case DODGE -> "DODGE";
            default -> "TAP";
        };
    }
}
