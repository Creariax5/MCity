package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BuildingPlacer {

    public static void breakBlock(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return;
        ServerWorld serverWorld = client.getServer().getOverworld();
        serverWorld.breakBlock(pos, true);
    }

    private static void setBlock(BlockPos pos, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getServer() != null) {
            client.getServer().getOverworld().setBlockState(pos, state);
        }
    }

    private static void clearAbove(BlockPos pos, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getServer() == null) return;
        ServerWorld world = client.getServer().getOverworld();
        for (int y = 1; y <= height; y++) {
            BlockPos above = pos.up(y);
            if (!world.getBlockState(above).isAir()) {
                world.breakBlock(above, false);
            }
        }
    }

    // ===== ZONE =====

    public static void placeHouse(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState wall = Blocks.OAK_PLANKS.getDefaultState();
        BlockState floor = Blocks.STONE_BRICKS.getDefaultState();
        BlockState roof = Blocks.OAK_SLAB.getDefaultState();
        BlockState glass = Blocks.GLASS_PANE.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                setBlock(base.add(x, 0, z), floor);
                for (int y = 1; y <= 4; y++) {
                    boolean isWall = x == 0 || x == 4 || z == 0 || z == 4;
                    if (isWall) {
                        boolean isWindow = y == 2 && ((x != 0 && x != 4 && (z == 0 || z == 4)) || (z != 0 && z != 4 && (x == 0 || x == 4)));
                        boolean isDoor = z == 0 && x == 2 && (y == 1 || y == 2);
                        if (isDoor) setBlock(base.add(x, y, z), air);
                        else if (isWindow) setBlock(base.add(x, y, z), glass);
                        else setBlock(base.add(x, y, z), wall);
                    } else {
                        setBlock(base.add(x, y, z), air);
                    }
                }
                setBlock(base.add(x, 5, z), roof);
            }
        }
    }

    public static void placeCommerce(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState wood = Blocks.STRIPPED_OAK_LOG.getDefaultState();
        BlockState plank = Blocks.OAK_PLANKS.getDefaultState();
        BlockState wool = Blocks.RED_WOOL.getDefaultState();
        BlockState slab = Blocks.OAK_SLAB.getDefaultState();

        // Comptoir 5x3
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 3; z++) {
                setBlock(base.add(x, 0, z), plank);
            }
        }
        // Piliers aux 4 coins
        for (int y = 1; y <= 3; y++) {
            setBlock(base.add(0, y, 0), wood);
            setBlock(base.add(4, y, 0), wood);
            setBlock(base.add(0, y, 2), wood);
            setBlock(base.add(4, y, 2), wood);
        }
        // Comptoir
        for (int x = 1; x < 4; x++) {
            setBlock(base.add(x, 1, 0), slab);
        }
        // Toit en laine
        for (int x = -1; x <= 5; x++) {
            for (int z = -1; z <= 3; z++) {
                setBlock(base.add(x, 4, z), wool);
            }
        }
    }

    public static void placeIndustrie(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState stone = Blocks.STONE_BRICKS.getDefaultState();
        BlockState cobble = Blocks.COBBLESTONE.getDefaultState();
        BlockState furnace = Blocks.FURNACE.getDefaultState();
        BlockState slab = Blocks.STONE_BRICK_SLAB.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();

        // Murs 6x5, hauteur 5
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 5; z++) {
                setBlock(base.add(x, 0, z), cobble);
                for (int y = 1; y <= 5; y++) {
                    boolean isWall = x == 0 || x == 5 || z == 0 || z == 4;
                    if (isWall) {
                        setBlock(base.add(x, y, z), stone);
                    } else {
                        setBlock(base.add(x, y, z), air);
                    }
                }
                setBlock(base.add(x, 6, z), slab);
            }
        }
        // Fours à l'intérieur
        setBlock(base.add(2, 1, 3), furnace);
        setBlock(base.add(3, 1, 3), furnace);
        // Cheminée
        for (int y = 6; y <= 9; y++) {
            setBlock(base.add(5, y, 4), cobble);
        }
    }

    public static void placeFerme(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState fence = Blocks.OAK_FENCE.getDefaultState();
        BlockState farmland = Blocks.FARMLAND.getDefaultState();
        BlockState wheat = Blocks.WHEAT.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();

        // Champ 7x7
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                boolean isBorder = x == 0 || x == 6 || z == 0 || z == 6;
                if (isBorder) {
                    setBlock(base.add(x, 0, z), Blocks.GRASS_BLOCK.getDefaultState());
                    setBlock(base.add(x, 1, z), fence);
                } else if (x == 3 && z == 3) {
                    // Eau au centre
                    setBlock(base.add(x, -1, z), water);
                } else {
                    setBlock(base.add(x, 0, z), farmland);
                    setBlock(base.add(x, 1, z), wheat);
                }
            }
        }
    }

    // ===== EAU =====

    public static void placePuits(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState stone = Blocks.STONE_BRICKS.getDefaultState();
        BlockState slab = Blocks.STONE_BRICK_SLAB.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();
        BlockState fence = Blocks.OAK_FENCE.getDefaultState();

        // Base 3x3
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean isCenter = x == 1 && z == 1;
                if (isCenter) {
                    setBlock(base.add(x, -1, z), water);
                    setBlock(base.add(x, 0, z), Blocks.AIR.getDefaultState());
                } else {
                    setBlock(base.add(x, 0, z), stone);
                    setBlock(base.add(x, 1, z), stone);
                }
            }
        }
        // Piliers et toit
        setBlock(base.add(0, 2, 0), fence);
        setBlock(base.add(2, 2, 0), fence);
        setBlock(base.add(0, 2, 2), fence);
        setBlock(base.add(2, 2, 2), fence);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                setBlock(base.add(x, 3, z), slab);
            }
        }
    }

    public static void placeReservoir(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState copper = Blocks.COPPER_BLOCK.getDefaultState();
        BlockState wood = Blocks.OAK_LOG.getDefaultState();
        BlockState plank = Blocks.OAK_PLANKS.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();

        // 4 piliers en bois
        for (int y = 0; y < 6; y++) {
            setBlock(base.add(0, y, 0), wood);
            setBlock(base.add(3, y, 0), wood);
            setBlock(base.add(0, y, 3), wood);
            setBlock(base.add(3, y, 3), wood);
        }
        // Bassin en haut 4x4
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                setBlock(base.add(x, 6, z), copper);
                boolean isBorder = x == 0 || x == 3 || z == 0 || z == 3;
                if (isBorder) {
                    setBlock(base.add(x, 7, z), copper);
                } else {
                    setBlock(base.add(x, 7, z), water);
                }
            }
        }
    }

    // ===== ELECTRICITE =====

    public static void placeGenerateur(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState stone = Blocks.STONE_BRICKS.getDefaultState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.getDefaultState();
        BlockState furnace = Blocks.FURNACE.getDefaultState();
        BlockState slab = Blocks.STONE_BRICK_SLAB.getDefaultState();

        // Base 3x3
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                setBlock(base.add(x, 0, z), stone);
                setBlock(base.add(x, 1, z), stone);
            }
        }
        // Coeur : fours + redstone
        setBlock(base.add(1, 1, 1), furnace);
        setBlock(base.add(1, 2, 1), redstone);
        // Murs extérieurs hauteur 2-3
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean isBorder = x == 0 || x == 2 || z == 0 || z == 2;
                if (isBorder) {
                    setBlock(base.add(x, 2, z), stone);
                    setBlock(base.add(x, 3, z), slab);
                }
            }
        }
    }

    public static void placeTourRelais(BlockPos origin) {
        BlockPos base = origin.up();
        BlockState wood = Blocks.OAK_LOG.getDefaultState();
        BlockState plank = Blocks.OAK_PLANKS.getDefaultState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.getDefaultState();
        BlockState lamp = Blocks.REDSTONE_LAMP.getDefaultState();

        // Pilier central
        for (int y = 0; y < 8; y++) {
            setBlock(base.add(1, y, 1), wood);
        }
        // Croix en haut
        setBlock(base.add(0, 7, 1), plank);
        setBlock(base.add(2, 7, 1), plank);
        setBlock(base.add(1, 7, 0), plank);
        setBlock(base.add(1, 7, 2), plank);
        // Lampe + redstone au sommet
        setBlock(base.add(1, 8, 1), redstone);
        setBlock(base.add(1, 9, 1), lamp);
        // Base renforcée
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                setBlock(base.add(x, 0, z), plank);
            }
        }
    }
}
