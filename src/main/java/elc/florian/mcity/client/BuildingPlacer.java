package elc.florian.mcity.client;
import elc.florian.mcity.state.Tools;


import elc.florian.mcity.MCity;
import elc.florian.mcity.structure.PlacedStructure;
import elc.florian.mcity.structure.StructureKind;
import elc.florian.mcity.structure.StructureRegistry;
import elc.florian.mcity.structure.ZoneRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class BuildingPlacer {

    private static ServerWorld world() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return null;
        return client.getServer().getOverworld();
    }

    public static void breakBlock(BlockPos pos) {
        ServerWorld world = world();
        if (world != null) world.breakBlock(pos, true);
    }

    public static void deleteStructure(PlacedStructure s) {
        ServerWorld world = world();
        if (world == null) return;
        for (BlockPos p : s.blocks) {
            BlockState prev = s.previousStates.get(p);
            world.setBlockState(p, prev != null ? prev : Blocks.AIR.getDefaultState());
        }
        StructureRegistry.remove(s);
        if (s.kind.isRoad()) ZoneRegistry.cleanupOrphaned();
    }

    private static void clearStructureBlocks(PlacedStructure s) {
        ServerWorld world = world();
        if (world == null) return;
        for (BlockPos p : s.blocks) {
            BlockState prev = s.previousStates.get(p);
            world.setBlockState(p, prev != null ? prev : Blocks.AIR.getDefaultState());
        }
    }

    public static boolean moveStructureTo(PlacedStructure s, BlockPos newClickedPos) {
        if (s.kind.isLine) {
            int dx = newClickedPos.getX() - s.lineFrom.getX();
            int dz = newClickedPos.getZ() - s.lineFrom.getZ();
            BlockPos newFrom = new BlockPos(s.lineFrom.getX() + dx, s.lineFrom.getY(), s.lineFrom.getZ() + dz);
            BlockPos newTo = new BlockPos(s.lineTo.getX() + dx, s.lineTo.getY(), s.lineTo.getZ() + dz);

            clearStructureBlocks(s);
            StructureRegistry.remove(s);

            boolean ok = switch (s.kind) {
                case ROAD_ROAD -> RoadPlacer.placeRoad(newFrom, newTo);
                case CANALISATION -> RoadPlacer.placeCanalisation(newFrom, newTo);
                case CABLE -> RoadPlacer.placeCable(newFrom, newTo);
                default -> false;
            };
            if (s.kind.isRoad()) ZoneRegistry.cleanupOrphaned();
            Tools.selectedStructure = ok ? StructureRegistry.all().get(StructureRegistry.all().size() - 1) : null;
            return ok;
        }

        int rotation = s.rotation;
        clearStructureBlocks(s);
        StructureRegistry.remove(s);

        boolean ok = replaceBuilding(s.kind, newClickedPos, rotation);
        Tools.selectedStructure = ok ? StructureRegistry.all().get(StructureRegistry.all().size() - 1) : null;
        return ok;
    }

    public static boolean rotateStructure(PlacedStructure s) {
        if (s.kind.isLine) return false;

        BlockPos clicked = s.origin.down();
        int newRotation = (s.rotation + 1) & 3;

        clearStructureBlocks(s);
        StructureRegistry.remove(s);

        boolean ok = replaceBuilding(s.kind, clicked, newRotation);
        Tools.selectedStructure = ok ? StructureRegistry.all().get(StructureRegistry.all().size() - 1) : null;
        return ok;
    }

    private static boolean replaceBuilding(StructureKind kind, BlockPos clicked, int rotation) {
        return switch (kind) {
            case PUITS -> placePuits(clicked, rotation);
            case RESERVOIR -> placeReservoir(clicked, rotation);
            case GENERATEUR -> placeGenerateur(clicked, rotation);
            case TOUR_RELAIS -> placeTourRelais(clicked, rotation);
            default -> false;
        };
    }

    // === Bâtiments (eau + électricité uniquement) ===

    public static boolean placePuits(BlockPos origin, int rotation) {
        Blueprint bp = new Blueprint(StructureKind.PUITS, origin, rotation);
        BlockState stone = Blocks.STONE_BRICKS.getDefaultState();
        BlockState slab = Blocks.STONE_BRICK_SLAB.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();
        BlockState fence = Blocks.OAK_FENCE.getDefaultState();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean isCenter = x == 1 && z == 1;
                if (isCenter) {
                    bp.set(x, 0, z, water);
                } else {
                    bp.set(x, 0, z, stone);
                    bp.set(x, 1, z, stone);
                }
            }
        }
        bp.set(0, 2, 0, fence);
        bp.set(2, 2, 0, fence);
        bp.set(0, 2, 2, fence);
        bp.set(2, 2, 2, fence);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                bp.set(x, 3, z, slab);
            }
        }
        return bp.commit();
    }

    public static boolean placeReservoir(BlockPos origin, int rotation) {
        Blueprint bp = new Blueprint(StructureKind.RESERVOIR, origin, rotation);
        BlockState copper = Blocks.COPPER_BLOCK.getDefaultState();
        BlockState wood = Blocks.OAK_LOG.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();

        for (int y = 0; y < 6; y++) {
            bp.set(0, y, 0, wood);
            bp.set(3, y, 0, wood);
            bp.set(0, y, 3, wood);
            bp.set(3, y, 3, wood);
        }
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                bp.set(x, 6, z, copper);
                boolean isBorder = x == 0 || x == 3 || z == 0 || z == 3;
                bp.set(x, 7, z, isBorder ? copper : water);
            }
        }
        return bp.commit();
    }

    public static boolean placeGenerateur(BlockPos origin, int rotation) {
        Blueprint bp = new Blueprint(StructureKind.GENERATEUR, origin, rotation);
        BlockState stone = Blocks.STONE_BRICKS.getDefaultState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.getDefaultState();
        BlockState furnace = Blocks.FURNACE.getDefaultState();
        BlockState slab = Blocks.STONE_BRICK_SLAB.getDefaultState();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                bp.set(x, 0, z, stone);
                bp.set(x, 1, z, stone);
            }
        }
        bp.set(1, 1, 1, furnace);
        bp.set(1, 2, 1, redstone);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean isBorder = x == 0 || x == 2 || z == 0 || z == 2;
                if (isBorder) {
                    bp.set(x, 2, z, stone);
                    bp.set(x, 3, z, slab);
                }
            }
        }
        return bp.commit();
    }

    public static boolean placeTourRelais(BlockPos origin, int rotation) {
        Blueprint bp = new Blueprint(StructureKind.TOUR_RELAIS, origin, rotation);
        BlockState wood = Blocks.OAK_LOG.getDefaultState();
        BlockState plank = Blocks.OAK_PLANKS.getDefaultState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.getDefaultState();
        BlockState lamp = Blocks.REDSTONE_LAMP.getDefaultState();

        for (int y = 0; y < 8; y++) {
            bp.set(1, y, 1, wood);
        }
        bp.set(0, 7, 1, plank);
        bp.set(2, 7, 1, plank);
        bp.set(1, 7, 0, plank);
        bp.set(1, 7, 2, plank);
        bp.set(1, 8, 1, redstone);
        bp.set(1, 9, 1, lamp);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                bp.set(x, 0, z, plank);
            }
        }
        return bp.commit();
    }

    // === Blueprint ===

    private static class Blueprint {
        final StructureKind kind;
        final BlockPos origin;
        final int rotation;
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        Blueprint(StructureKind kind, BlockPos clicked, int rotation) {
            this.kind = kind;
            this.origin = clicked.up();
            this.rotation = rotation;
        }

        void set(int x, int y, int z, BlockState state) {
            int w = kind.width;
            int d = kind.depth;
            int rx, rz;
            switch (rotation & 3) {
                case 1 -> { rx = d - 1 - z; rz = x; }
                case 2 -> { rx = w - 1 - x; rz = d - 1 - z; }
                case 3 -> { rx = z; rz = w - 1 - x; }
                default -> { rx = x; rz = z; }
            }
            blocks.put(origin.add(rx, y, rz), state);
        }

        boolean commit() {
            ServerWorld world = world();
            if (world == null) return false;
            if (StructureRegistry.collides(blocks.keySet())) return false;

            PlacedStructure ps = new PlacedStructure();
            ps.kind = kind;
            ps.origin = origin;
            ps.rotation = rotation;

            for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
                BlockPos pos = e.getKey();
                ps.previousStates.put(pos, world.getBlockState(pos));
                ps.blocks.add(pos);
                world.setBlockState(pos, e.getValue());
            }

            StructureRegistry.add(ps);
            return true;
        }
    }
}
