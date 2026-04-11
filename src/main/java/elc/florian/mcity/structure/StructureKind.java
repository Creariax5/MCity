package elc.florian.mcity.structure;

public enum StructureKind {
    PUITS("Puits", 3, 3, 4, false),
    RESERVOIR("Reservoir", 4, 4, 8, false),
    GENERATEUR("Generateur", 3, 3, 4, false),
    TOUR_RELAIS("Tour relais", 3, 3, 10, false),
    ROAD_ROAD("Route", 0, 0, 0, true),
    CANALISATION("Canalisation", 0, 0, 0, true),
    CABLE("Cable", 0, 0, 0, true);

    public final String displayName;
    public final int width;
    public final int depth;
    public final int height;
    public final boolean isLine;

    StructureKind(String displayName, int width, int depth, int height, boolean isLine) {
        this.displayName = displayName;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.isLine = isLine;
    }

    public boolean isRoad() {
        return this == ROAD_ROAD;
    }
}
