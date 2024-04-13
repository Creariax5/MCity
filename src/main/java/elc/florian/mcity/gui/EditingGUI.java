package elc.florian.mcity.gui;

import elc.florian.mcity.MCity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditingGUI extends Screen {
    public EditingGUI(Text title) {
        super(title);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F6 || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            MCity.detached = !MCity.detached;
            return true;
        }

        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 100000, 0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
