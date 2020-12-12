package me.regexmc.statsoverlay.utils.hypixel;

public class ExpCalculator {
    public static final int EASY_LEVELS = 4;

    public static final int EASY_LEVELS_XP = 7000;

    public static final int XP_PER_PRESTIGE = 487000;

    public static final int LEVELS_PER_PRESTIGE = 100;

    public static final int HIGHEST_PRESTIGE = 10;

    public static double getExpForLevel(int level) {
        if (level == 0)
            return 0.0D;
        double respectedLevel = getLevelRespectingPrestige(level);
        if (respectedLevel > 4.0D)
            return 5000.0D;
        switch ((int) respectedLevel) {
            case 1:
                return 500.0D;
            case 2:
                return 1000.0D;
            case 3:
                return 2000.0D;
            case 4:
                return 3500.0D;
        }
        return 5000.0D;
    }

    public static double getLevelRespectingPrestige(double level) {
        if (level > 1000.0D)
            return level - 1000.0D;
        return level % 100.0D;
    }

    public static int getLevelForExp(int exp) {
        int prestiges = (int) Math.floor((exp / 487000));
        int level = prestiges * 100;
        double expWithoutPrestiges = (exp - prestiges * 487000);
        for (int i = 1; i <= 4; i++) {
            double expForEasyLevel = getExpForLevel(i);
            if (expWithoutPrestiges < expForEasyLevel)
                break;
            level++;
            expWithoutPrestiges -= expForEasyLevel;
        }
        return level + (int) Math.floor(expWithoutPrestiges / 5000.0D);
    }

    public static int getBWLevel(int xp) {
        int prestiges = (int) Math.floor((xp / 487000));
        int level = prestiges * 100;
        double expWithoutPrestiges = (xp - prestiges * 487000);
        for (int i = 1; i <= 4; i++) {
            double expForEasyLevel = getExpForLevel(i);
            if (expWithoutPrestiges < expForEasyLevel)
                break;
            level++;
            expWithoutPrestiges -= expForEasyLevel;
        }
        level += (int) Math.floor(expWithoutPrestiges / 5000.0D);
        return level;
    }
}
