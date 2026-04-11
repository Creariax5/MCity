package elc.florian.mcity.structure;

import com.google.gson.*;
import elc.florian.mcity.MCity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StructureRegistry {
    private static final List<PlacedStructure> structures = new ArrayList<>();
    // Index spatial : ChunkPos long → liste de structures qui touchent ce chunk
    private static final Map<Long, List<PlacedStructure>> chunkIndex = new HashMap<>();
    private static int nextId = 0;

    public static void clear() {
        structures.clear();
        chunkIndex.clear();
        nextId = 0;
    }

    public static List<PlacedStructure> all() {
        return structures;
    }

    public static void add(PlacedStructure s) {
        s.id = nextId++;
        structures.add(s);
        indexStructure(s);
    }

    public static void remove(PlacedStructure s) {
        structures.remove(s);
        unindexStructure(s);
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static void indexStructure(PlacedStructure s) {
        Set<Long> keys = new HashSet<>();
        for (BlockPos p : s.blocks) {
            keys.add(chunkKey(p.getX() >> 4, p.getZ() >> 4));
        }
        for (long k : keys) {
            chunkIndex.computeIfAbsent(k, x -> new ArrayList<>()).add(s);
        }
    }

    private static void unindexStructure(PlacedStructure s) {
        Set<Long> keys = new HashSet<>();
        for (BlockPos p : s.blocks) {
            keys.add(chunkKey(p.getX() >> 4, p.getZ() >> 4));
        }
        for (long k : keys) {
            List<PlacedStructure> list = chunkIndex.get(k);
            if (list != null) {
                list.remove(s);
                if (list.isEmpty()) chunkIndex.remove(k);
            }
        }
    }

    public static PlacedStructure findAt(BlockPos pos) {
        long key = chunkKey(pos.getX() >> 4, pos.getZ() >> 4);
        List<PlacedStructure> list = chunkIndex.get(key);
        if (list == null) return null;
        for (PlacedStructure s : list) {
            if (s.contains(pos)) return s;
        }
        return null;
    }

    public static boolean collides(Set<BlockPos> newBlocks) {
        Set<Long> keys = new HashSet<>();
        for (BlockPos p : newBlocks) {
            keys.add(chunkKey(p.getX() >> 4, p.getZ() >> 4));
        }
        for (long k : keys) {
            List<PlacedStructure> list = chunkIndex.get(k);
            if (list == null) continue;
            for (PlacedStructure s : list) {
                if (s.intersects(newBlocks)) return true;
            }
        }
        return false;
    }

    // === Persistance ===

    public static void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            JsonArray arr = new JsonArray();
            for (PlacedStructure s : structures) {
                JsonObject o = new JsonObject();
                o.addProperty("id", s.id);
                o.addProperty("kind", s.kind.name());
                o.addProperty("rotation", s.rotation);
                if (s.origin != null) o.add("origin", posToJson(s.origin));
                if (s.lineFrom != null) o.add("lineFrom", posToJson(s.lineFrom));
                if (s.lineTo != null) o.add("lineTo", posToJson(s.lineTo));
                JsonArray prevStates = new JsonArray();
                for (Map.Entry<BlockPos, BlockState> e : s.previousStates.entrySet()) {
                    JsonObject entry = new JsonObject();
                    entry.add("pos", posToJson(e.getKey()));
                    entry.addProperty("block", Registries.BLOCK.getId(e.getValue().getBlock()).toString());
                    prevStates.add(entry);
                }
                o.add("previousStates", prevStates);
                arr.add(o);
            }
            JsonObject root = new JsonObject();
            root.add("structures", arr);
            root.addProperty("nextId", nextId);
            Files.writeString(file, new GsonBuilder().create().toJson(root));
            MCity.LOGGER.info("Saved " + structures.size() + " structures to " + file);
        } catch (IOException e) {
            MCity.LOGGER.error("Failed to save structures", e);
        }
    }

    public static void load(Path file) {
        clear();
        if (!Files.exists(file)) return;
        try {
            String content = Files.readString(file);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("structures");
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                PlacedStructure s = new PlacedStructure();
                s.id = o.get("id").getAsInt();
                try {
                    s.kind = StructureKind.valueOf(o.get("kind").getAsString());
                } catch (IllegalArgumentException ex) {
                    continue; // type disparu (ex: HABITATION enlevé)
                }
                s.rotation = o.get("rotation").getAsInt();
                if (o.has("origin")) s.origin = posFromJson(o.getAsJsonArray("origin"));
                if (o.has("lineFrom")) s.lineFrom = posFromJson(o.getAsJsonArray("lineFrom"));
                if (o.has("lineTo")) s.lineTo = posFromJson(o.getAsJsonArray("lineTo"));
                if (o.has("previousStates")) {
                    for (JsonElement entry : o.getAsJsonArray("previousStates")) {
                        JsonObject obj = entry.getAsJsonObject();
                        BlockPos pos = posFromJson(obj.getAsJsonArray("pos"));
                        Block block = Registries.BLOCK.get(Identifier.tryParse(obj.get("block").getAsString()));
                        BlockState state = block != null ? block.getDefaultState() : Blocks.AIR.getDefaultState();
                        s.previousStates.put(pos, state);
                        s.blocks.add(pos);
                    }
                } else {
                    for (JsonElement be : o.getAsJsonArray("blocks")) {
                        s.blocks.add(posFromJson(be.getAsJsonArray()));
                    }
                }
                structures.add(s);
                indexStructure(s);
            }
            nextId = root.has("nextId") ? root.get("nextId").getAsInt() : structures.size();
            MCity.LOGGER.info("Loaded " + structures.size() + " structures from " + file);
        } catch (Exception e) {
            MCity.LOGGER.error("Failed to load structures", e);
        }
    }

    private static JsonArray posToJson(BlockPos p) {
        JsonArray arr = new JsonArray();
        arr.add(p.getX());
        arr.add(p.getY());
        arr.add(p.getZ());
        return arr;
    }

    private static BlockPos posFromJson(JsonArray arr) {
        return new BlockPos(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt());
    }
}
