package elc.florian.mcity.tools;

import net.minecraft.item.Item;

import java.util.List;

/**
 * Un outil dans la toolbar (Route, Zone, Eau, Électricité).
 * Définit son icône et ses sous-types.
 */
public abstract class Tool {
    public final String name;
    public final Item icon;

    protected Tool(String name, Item icon) {
        this.name = name;
        this.icon = icon;
    }

    public abstract List<SubType> subTypes();

    /** Vrai si l'outil n'a qu'un sous-type et doit être auto-sélectionné sans afficher le panneau. */
    public boolean autoSelectSingleSubType() {
        return subTypes().size() == 1;
    }
}
