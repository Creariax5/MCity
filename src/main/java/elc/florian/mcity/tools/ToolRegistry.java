package elc.florian.mcity.tools;

import elc.florian.mcity.state.Tools;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Registre central de tous les outils du mod, indexés par ToolType. */
public class ToolRegistry {
    private static final Map<Tools.ToolType, Tool> tools = new EnumMap<>(Tools.ToolType.class);
    private static final List<Tool> ordered = new ArrayList<>();

    public static void register(Tools.ToolType type, Tool tool) {
        tools.put(type, tool);
        ordered.add(tool);
    }

    public static Tool get(Tools.ToolType type) {
        return tools.get(type);
    }

    public static List<Tool> all() {
        return ordered;
    }
}
