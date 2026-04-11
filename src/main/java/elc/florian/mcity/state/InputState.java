package elc.florian.mcity.state;

/** État brut de la souris et du clavier (utilisé par les mixins d'input). */
public class InputState {
    // Souris
    public static double mouseX, mouseY;
    public static double lastX, lastY;
    public static boolean mouseMiddlePressed = false;
    public static boolean newDragStart;
    public static boolean mouseMoving;

    // Clavier (mouvement WASD)
    public static boolean keyW_pressed = false;
    public static boolean keyA_pressed = false;
    public static boolean keyS_pressed = false;
    public static boolean keyD_pressed = false;

    public static boolean isKeyMoving() {
        return keyW_pressed || keyA_pressed || keyS_pressed || keyD_pressed;
    }
}
