package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RoadPlacer {

    public static int getWidth(MCity.RoadType type) {
        return switch (type) {
            case PATH -> 2;
            case ROAD -> 4;
            case HIGHWAY -> 6;
        };
    }

    public static BlockState getBlock(MCity.RoadType type) {
        return switch (type) {
            case PATH -> Blocks.DIRT_PATH.getDefaultState();
            case ROAD -> Blocks.COBBLESTONE.getDefaultState();
            case HIGHWAY -> Blocks.STONE_BRICKS.getDefaultState();
        };
    }

    /**
     * Calcule tous les BlockPos de la route entre 2 points avec une largeur donnée.
     * La route suit le sol.
     */
    public static List<BlockPos> computeRoadBlocks(BlockPos from, BlockPos to, int width) {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        List<BlockPos> blocks = new ArrayList<>();

        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) length = 1;

        // Plus de pas pour éviter les trous en diagonale
        int steps = (int) Math.ceil(length * 2);
        if (steps == 0) steps = 1;

        // Direction perpendiculaire pour la largeur
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
                if (by > 0) {
                    blocks.add(new BlockPos(bx, by, bz));
                }
            }
        }

        return blocks;
    }

    /**
     * Trouve le Y de la surface au point donné
     */
    private static int findSurfaceY(int x, int z) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return -1;

        // Chercher du haut vers le bas
        for (int y = 320; y > -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!client.world.getBlockState(pos).isAir()) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Pose la route côté serveur
     */
    public static void placeRoad(BlockPos from, BlockPos to, MCity.RoadType type) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return;

        ServerWorld serverWorld = client.getServer().getOverworld();
        BlockState block = getBlock(type);
        int width = getWidth(type);
        List<BlockPos> blocks = computeRoadBlocks(from, to, width);

        for (BlockPos pos : blocks) {
            serverWorld.setBlockState(pos, block);
            for (int y = 1; y <= 4; y++) {
                BlockPos above = pos.up(y);
                if (!serverWorld.getBlockState(above).isAir()) {
                    serverWorld.breakBlock(above, false);
                }
            }
        }
    }

    public static void placeCanalisation(BlockPos from, BlockPos to) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return;

        ServerWorld serverWorld = client.getServer().getOverworld();
        BlockState copper = Blocks.COPPER_BLOCK.getDefaultState();
        List<BlockPos> blocks = computeRoadBlocks(from, to, 1);

        for (BlockPos pos : blocks) {
            // Remplace le bloc de surface par du cuivre
            serverWorld.setBlockState(pos, copper);
        }
    }

    public static void placeCable(BlockPos from, BlockPos to) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return;

        ServerWorld serverWorld = client.getServer().getOverworld();
        BlockState redstone = Blocks.REDSTONE_WIRE.getDefaultState();
        List<BlockPos> blocks = computeRoadBlocks(from, to, 1);

        for (BlockPos pos : blocks) {
            // Pose la redstone sur le bloc de surface
            BlockPos above = pos.up();
            serverWorld.setBlockState(above, redstone);
            // Enlever ce qui gêne au-dessus
            for (int y = 2; y <= 4; y++) {
                BlockPos higher = pos.up(y);
                if (!serverWorld.getBlockState(higher).isAir()) {
                    serverWorld.breakBlock(higher, false);
                }
            }
        }
    }
}
