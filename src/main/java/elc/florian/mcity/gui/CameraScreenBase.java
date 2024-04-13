package elc.florian.mcity.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class CameraScreenBase extends Screen{
    protected static final int FONT_COLOR = 4210752;

    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected CameraScreenBase(Component title, int xSize, int ySize) {
        super((Text) title);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    public static class HoverArea {
        private final int posX, posY;
        private final int width, height;

        public HoverArea(int posX, int posY, int width, int height) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isHovered(int guiLeft, int guiTop, int mouseX, int mouseY) {
            if (mouseX >= guiLeft + posX && mouseX < guiLeft + posX + width) {
                return mouseY >= guiTop + posY && mouseY < guiTop + posY + height;
            }
            return false;
        }
    }

}
