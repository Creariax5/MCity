package elc.florian.mcity.state;

import elc.florian.mcity.structure.PlacedStructure;
import elc.florian.mcity.tools.SubType;
import net.minecraft.util.math.BlockPos;

/** État de sélection des outils et de l'édition (sélection, déplacement, etc.) */
public class Tools {
    public enum ToolType { ROAD, AREA, WATER, ELECTRICITY }
    public enum AreaType { HABITATION, COMMERCE, INDUSTRIE, DEZONNAGE }

    public static final int ROAD_WIDTH = 8;
    public static final int ROAD_MIN_LENGTH = 16;

    // Sélection courante
    public static ToolType selectedTool = null;
    public static SubType selectedSubType = null;

    public static boolean panelOpen = false;
    public static BlockPos lineFirstPoint = null;

    // Édition de structures existantes
    public static PlacedStructure selectedStructure = null;
    public static boolean moveMode = false;

    // Mode placement de zone
    public static boolean zoneFillMode = false;
}
