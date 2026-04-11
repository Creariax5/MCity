package elc.florian.mcity.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

/**
 * PersistentState attaché à l'overworld. Sauvegarde automatiquement les structures
 * et zones via NBT à chaque save du monde, sans gestion manuelle de fichiers.
 *
 * Stocke les données en JSON dans des champs NBT String — les Codecs natifs pour
 * PlacedStructure (avec BlockState) seraient bien plus verbeux et apportent peu.
 */
public class MCityPersistentState extends PersistentState {

    public static final PersistentState.Type<MCityPersistentState> TYPE = new PersistentState.Type<>(
            MCityPersistentState::new,
            MCityPersistentState::fromNbt,
            null
    );

    public String structuresJson = "{\"structures\":[],\"nextId\":0}";
    public String zonesJson = "[]";

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putString("structures", structuresJson);
        nbt.putString("zones", zonesJson);
        return nbt;
    }

    public static MCityPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        MCityPersistentState state = new MCityPersistentState();
        if (nbt.contains("structures")) state.structuresJson = nbt.getString("structures");
        if (nbt.contains("zones")) state.zonesJson = nbt.getString("zones");
        return state;
    }

    /** Récupère (ou crée) l'instance attachée à l'overworld. */
    public static MCityPersistentState get(ServerWorld world) {
        return world.getServer().getOverworld()
                .getPersistentStateManager()
                .getOrCreate(TYPE, "mcity");
    }

    /** Snapshot des registres dans cet état (à appeler avant que le monde sauvegarde). */
    public void snapshot() {
        this.structuresJson = StructureRegistry.toJson();
        this.zonesJson = ZoneRegistry.toJson();
        markDirty();
    }

    /** Restaure les registres à partir de l'état (à appeler au démarrage). */
    public void restore() {
        StructureRegistry.fromJson(structuresJson);
        ZoneRegistry.fromJson(zonesJson);
        ZoneRegistry.markDirty();
    }
}
