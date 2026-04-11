package elc.florian.mcity.state;

/** État et logique de la simulation (date, économie, demande, bonheur, population). */
public class Simulation {
    public static boolean paused = false;
    public static int gameSpeed = 1;

    public static int gameDay = 1;
    public static int gameMonth = 1;
    public static int gameYear = 1;

    public static long money = 10000;
    public static int demandResidential = 50;
    public static int demandCommercial = 30;
    public static int demandIndustrial = 40;
    public static int happiness = 70;
    public static int population = 0;

    public static long lastTickTime = 0;
    private static long tickAccumulator = 0;
    private static final long TICK_INTERVAL_MS = 2000;

    public static void tick() {
        if (paused || gameSpeed == 0) return;

        long now = System.currentTimeMillis();
        if (lastTickTime == 0) {
            lastTickTime = now;
            return;
        }

        tickAccumulator += (now - lastTickTime) * gameSpeed;
        lastTickTime = now;

        while (tickAccumulator >= TICK_INTERVAL_MS) {
            tickAccumulator -= TICK_INTERVAL_MS;
            advanceDay();
        }
    }

    private static void advanceDay() {
        gameDay++;
        if (gameDay > 30) {
            gameDay = 1;
            gameMonth++;
            if (gameMonth > 12) {
                gameMonth = 1;
                gameYear++;
            }
        }
        money += 10;
    }

    public static String getDateString() {
        return String.format("%02d/%02d/An %d", gameDay, gameMonth, gameYear);
    }

    public static String getMoneyString() {
        return String.format("%,d $", money);
    }

    public static String getHappinessEmoji() {
        if (happiness >= 70) return ":)";
        if (happiness >= 40) return ":/";
        return ":(";
    }

    public static String getPopulationString() {
        return String.format("%,d hab.", population);
    }
}
