package elc.florian.mcity.structure;
import elc.florian.mcity.state.Tools;


import com.google.gson.*;
import elc.florian.mcity.MCity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ZoneRegistry {
    public static final int TILE_SIZE = 8;
    public static final int MAX_TILE_DISTANCE = 4; // tiles autour d'une route

    // tileKey → AreaType
    private static final Map<Long, Tools.AreaType> zonedTiles = new HashMap<>();

    // Cache des tiles plaçables (recalculé à la demande)
    private static Set<Long> placeableCache = null;
    private static boolean cacheDirty = true;

    public static void markDirty() {
        cacheDirty = true;
    }

    public static long tileKey(int tileX, int tileZ) {
        return ((long) tileX << 32) | (tileZ & 0xFFFFFFFFL);
    }

    public static int tileX(long key) { return (int) (key >> 32); }
    public static int tileZ(long key) { return (int) key; }

    public static int blockToTile(int blockCoord) {
        return Math.floorDiv(blockCoord, TILE_SIZE);
    }

    public static Map<Long, Tools.AreaType> allZones() {
        return zonedTiles;
    }

    public static Tools.AreaType getZone(int tileX, int tileZ) {
        return zonedTiles.get(tileKey(tileX, tileZ));
    }

    public static void setZone(int tileX, int tileZ, Tools.AreaType type) {
        if (!isPlaceable(tileX, tileZ)) return;
        zonedTiles.put(tileKey(tileX, tileZ), type);
    }

    public static void removeZone(int tileX, int tileZ) {
        zonedTiles.remove(tileKey(tileX, tileZ));
    }

    public static boolean isPlaceable(int tileX, int tileZ) {
        return getPlaceableTiles().contains(tileKey(tileX, tileZ));
    }

    /** Retourne toutes les tiles dans 4 de distance d'au moins une route. */
    public static Set<Long> getPlaceableTiles() {
        if (cacheDirty || placeableCache == null) {
            placeableCache = computePlaceableTiles();
            cacheDirty = false;
        }
        return placeableCache;
    }

    private static Set<Long> computePlaceableTiles() {
        Set<Long> result = new HashSet<>();
        Set<Long> allRoadTiles = new HashSet<>();

        for (PlacedStructure s : StructureRegistry.all()) {
            if (!s.kind.isRoad()) continue;
            for (BlockPos p : s.blocks) {
                allRoadTiles.add(tileKey(blockToTile(p.getX()), blockToTile(p.getZ())));
            }
        }

        // Pour chaque route, étendre ses road tiles de MAX_TILE_DISTANCE en Chebyshev
        // et filtrer avec la projection scalaire sur le segment
        for (PlacedStructure s : StructureRegistry.all()) {
            if (!s.kind.isRoad() || s.lineFrom == null || s.lineTo == null) continue;

            double fx = s.lineFrom.getX();
            double fz = s.lineFrom.getZ();
            double tx = s.lineTo.getX();
            double tz = s.lineTo.getZ();
            double dx = tx - fx;
            double dz = tz - fz;
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length < 0.0001) continue;
            double dirX = dx / length;
            double dirZ = dz / length;

            Set<Long> thisRoadTiles = new HashSet<>();
            for (BlockPos p : s.blocks) {
                thisRoadTiles.add(tileKey(blockToTile(p.getX()), blockToTile(p.getZ())));
            }

            for (long rtKey : thisRoadTiles) {
                int rtx = tileX(rtKey);
                int rtz = tileZ(rtKey);
                for (int ddx = -MAX_TILE_DISTANCE; ddx <= MAX_TILE_DISTANCE; ddx++) {
                    for (int ddz = -MAX_TILE_DISTANCE; ddz <= MAX_TILE_DISTANCE; ddz++) {
                        int ntx = rtx + ddx;
                        int ntz = rtz + ddz;
                        long nk = tileKey(ntx, ntz);
                        if (allRoadTiles.contains(nk)) continue;
                        if (result.contains(nk)) continue;

                        // Projection sur le segment : centre de la tile
                        double cx = ntx * TILE_SIZE + TILE_SIZE / 2.0;
                        double cz = ntz * TILE_SIZE + TILE_SIZE / 2.0;
                        double proj = (cx - fx) * dirX + (cz - fz) * dirZ;
                        if (proj < 0 || proj > length) continue;

                        result.add(nk);
                    }
                }
            }
        }

        return result;
    }

    /** Supprime les zones qui ne sont plus sur des tiles plaçables (ex: route supprimée). */
    public static void cleanupOrphaned() {
        markDirty();
        Set<Long> placeable = getPlaceableTiles();
        zonedTiles.keySet().removeIf(key -> !placeable.contains(key));
    }

    /** Flood-fill pour supprimer toutes les zones connectées au point cliqué. */
    public static void floodClear(int startTileX, int startTileZ) {
        long start = tileKey(startTileX, startTileZ);
        if (!zonedTiles.containsKey(start)) return;

        Set<Long> visited = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            long k = queue.poll();
            zonedTiles.remove(k);

            int tx = tileX(k);
            int tz = tileZ(k);
            int[][] neighbors = {{tx - 1, tz}, {tx + 1, tz}, {tx, tz - 1}, {tx, tz + 1}};
            for (int[] n : neighbors) {
                long nk = tileKey(n[0], n[1]);
                if (!visited.contains(nk) && zonedTiles.containsKey(nk)) {
                    visited.add(nk);
                    queue.add(nk);
                }
            }
        }
    }

    /** Flood-fill depuis une tile sur toutes les tiles plaçables connectées. */
    public static void floodFill(int startTileX, int startTileZ, Tools.AreaType type) {
        Set<Long> placeable = getPlaceableTiles();
        long start = tileKey(startTileX, startTileZ);
        if (!placeable.contains(start)) return;

        Set<Long> visited = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            long k = queue.poll();
            zonedTiles.put(k, type);

            int tx = tileX(k);
            int tz = tileZ(k);
            int[][] neighbors = {{tx - 1, tz}, {tx + 1, tz}, {tx, tz - 1}, {tx, tz + 1}};
            for (int[] n : neighbors) {
                long nk = tileKey(n[0], n[1]);
                if (!visited.contains(nk) && placeable.contains(nk)) {
                    visited.add(nk);
                    queue.add(nk);
                }
            }
        }
    }

    public static void clear() {
        zonedTiles.clear();
        placeableCache = null;
        cacheDirty = true;
    }

    // === Persistance ===

    public static String toJson() {
        JsonArray arr = new JsonArray();
        for (Map.Entry<Long, Tools.AreaType> e : zonedTiles.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("tx", tileX(e.getKey()));
            o.addProperty("tz", tileZ(e.getKey()));
            o.addProperty("type", e.getValue().name());
            arr.add(o);
        }
        return new GsonBuilder().create().toJson(arr);
    }

    public static void fromJson(String content) {
        clear();
        if (content == null || content.isBlank()) return;
        try {
            JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                int tx = o.get("tx").getAsInt();
                int tz = o.get("tz").getAsInt();
                try {
                    Tools.AreaType type = Tools.AreaType.valueOf(o.get("type").getAsString());
                    zonedTiles.put(tileKey(tx, tz), type);
                } catch (IllegalArgumentException ignored) {
                    // Type inconnu — skip
                }
            }
        } catch (Exception e) {
            MCity.LOGGER.error("Failed to parse zones JSON", e);
        }
    }
}
