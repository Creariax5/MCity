package elc.florian.mcity.client;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoadPlacer {

    public static int getWidth(MCity.RoadType type) {
        return MCity.ROAD_WIDTH;
    }

    public static BlockState getBlock(MCity.RoadType type) {
        return Blocks.COBBLESTONE.getDefaultState();
    }

    private static StructureKind getRoadKind(MCity.RoadType type) {
        return StructureKind.ROAD_ROAD;
    }

    /** Longueur euclidienne entre from et to en blocs. */
    public static double segmentLength(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Vérifie si une route entre from et to serait valide (longueur mini + pas de collision). */
    public static boolean isRoadValid(BlockPos from, BlockPos to) {
        if (from == null || to == null) return false;
        if (segmentLength(from, to) < MCity.ROAD_MIN_LENGTH) return false;
        List<BlockPos> blocks = computeRoadBlocks(from, to, MCity.ROAD_WIDTH);
        return !StructureRegistry.collides(new HashSet<>(blocks));
    }

    public static List<BlockPos> computeRoadBlocks(BlockPos from, BlockPos to, int width) {
        Set<Long> seen = new HashSet<>();
        List<BlockPos> blocks = new ArrayList<>();

        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) length = 1;

        int steps = Math.abs(dx) + Math.abs(dz);
        if (steps == 0) steps = 1;

        double perpX = -dz / length;
        double perpZ = dx / length;

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double cx = from.getX() + t * dx;
            double cz = from.getZ() + t * dz;

            for (int w = 0; w < width; w++) {
                double offset = w - (width - 1) / 2.0;
                int bx = (int) Math.round(cx + perpX * offset);
                int bz = (int) Math.round(cz + perpZ * offset);

                long key = ((long) bx << 32) | (bz & 0xFFFFFFFFL);
                if (!seen.add(key)) continue;

                int by = findSurfaceY(bx, bz);
                if (by > -64) {
                    blocks.add(new BlockPos(bx, by, bz));
                }
            }
        }

        return blocks;
    }

    private static int findSurfaceY(int x, int z) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return -1;
        for (int y = 320; y > -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!client.world.getBlockState(pos).isAir()) return y;
        }
        return -1;
    }

    private static ServerWorld world() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return null;
        return client.getServer().getOverworld();
    }

    public static boolean placeRoad(BlockPos from, BlockPos to, MCity.RoadType type) {
        ServerWorld world = world();
        if (world == null) return false;

        if (segmentLength(from, to) < MCity.ROAD_MIN_LENGTH) return false;

        int width = getWidth(type);
        List<BlockPos> blocks = computeRoadBlocks(from, to, width);
        Set<BlockPos> placedBlocks = new HashSet<>(blocks);

        if (StructureRegistry.collides(placedBlocks)) return false;

        PlacedStructure ps = new PlacedStructure();
        ps.kind = getRoadKind(type);
        ps.lineFrom = from;
        ps.lineTo = to;

        BlockState block = getBlock(type);
        for (BlockPos pos : blocks) {
            ps.previousStates.put(pos, world.getBlockState(pos));
            ps.blocks.add(pos);
            world.setBlockState(pos, block);
            for (int y = 1; y <= 4; y++) {
                BlockPos above = pos.up(y);
                if (!world.getBlockState(above).isAir()) world.breakBlock(above, false);
            }
        }

        StructureRegistry.add(ps);
        ZoneRegistry.markDirty();
        return true;
    }

    public static boolean placeCanalisation(BlockPos from, BlockPos to) {
        ServerWorld world = world();
        if (world == null) return false;

        List<BlockPos> blocks = computeRoadBlocks(from, to, 1);
        Set<BlockPos> placedBlocks = new HashSet<>(blocks);
        if (StructureRegistry.collides(placedBlocks)) return false;

        PlacedStructure ps = new PlacedStructure();
        ps.kind = StructureKind.CANALISATION;
        ps.lineFrom = from;
        ps.lineTo = to;

        BlockState copper = Blocks.COPPER_BLOCK.getDefaultState();
        for (BlockPos pos : blocks) {
            ps.previousStates.put(pos, world.getBlockState(pos));
            ps.blocks.add(pos);
            world.setBlockState(pos, copper);
        }

        StructureRegistry.add(ps);
        return true;
    }

    public static boolean placeCable(BlockPos from, BlockPos to) {
        ServerWorld world = world();
        if (world == null) return false;

        List<BlockPos> blocks = computeRoadBlocks(from, to, 1);
        Set<BlockPos> wireBlocks = new HashSet<>();
        for (BlockPos p : blocks) wireBlocks.add(p.up());

        if (StructureRegistry.collides(wireBlocks)) return false;

        PlacedStructure ps = new PlacedStructure();
        ps.kind = StructureKind.CABLE;
        ps.lineFrom = from;
        ps.lineTo = to;

        BlockState redstone = Blocks.REDSTONE_WIRE.getDefaultState();
        for (BlockPos pos : blocks) {
            BlockPos wirePos = pos.up();
            ps.previousStates.put(wirePos, world.getBlockState(wirePos));
            ps.blocks.add(wirePos);
            world.setBlockState(wirePos, redstone);
            for (int y = 2; y <= 4; y++) {
                BlockPos higher = pos.up(y);
                if (!world.getBlockState(higher).isAir()) world.breakBlock(higher, false);
            }
        }

        StructureRegistry.add(ps);
        return true;
    }
}
