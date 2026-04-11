package elc.florian.mcity.tools;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;

/**
 * Une option dans le sous-menu d'un outil (ex: "Habitation", "Puits", "Cable").
 * Encapsule le label, la couleur, et l'action de placement.
 */
public abstract class SubType {
    public final String name;
    public final int color;

    protected SubType(String name, int color) {
        this.name = name;
        this.color = color;
    }

    /** Vrai si cet outil pose une ligne entre 2 points (sinon : clic unique). */
    public boolean isLine() { return false; }

    /** Action déclenchée par un clic sur le terrain. Pour les outils-ligne, gère la logique 1er/2ème point. */
    public abstract void onClick(BlockPos pos);

    /** Optionnel : preview à afficher dans le monde. */
    public void renderPreview(WorldRenderContext ctx) {}
}
