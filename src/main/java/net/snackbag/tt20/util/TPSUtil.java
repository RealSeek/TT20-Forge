package net.snackbag.tt20.util;

import net.snackbag.tt20.TT20;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TPSUtil {
    private static final DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final DecimalFormat dfMissedTicks = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.ROOT));

    // Minimum effective TPS to prevent extreme compensation values
    private static final double MIN_EFFECTIVE_TPS = 1.0;

    public static String colorizeTPS(double tps, boolean format) {
        if (tps > 15) {
            return "§a" + (format ? formatTPS(tps) : tps);
        } else if (tps > 10) {
            return "§e" + (format ? formatTPS(tps) : tps);
        } else {
            return "§c" + (format ? formatTPS(tps) : tps);
        }
    }

    public static String formatTPS(double tps) {
        return df.format(tps);
    }

    public static String formatMissedTicks(double missedTicks) {
        return dfMissedTicks.format(missedTicks);
    }

    /**
     * Scales a duration (in ticks) by the current TPS ratio.
     * Lower TPS = shorter duration = faster action for the player.
     */
    public static float tt20(float ticks, boolean limitZero) {
        float newTicks = (float) rawTT20(ticks);
        if (limitZero) return newTicks > 0 ? newTicks : 1;
        else return newTicks;
    }

    public static int tt20(int ticks, boolean limitZero) {
        int newTicks = (int) Math.ceil(rawTT20(ticks));
        if (limitZero) return newTicks > 0 ? newTicks : 1;
        else return newTicks;
    }

    public static double tt20(double ticks, boolean limitZero) {
        double newTicks = rawTT20(ticks);
        if (limitZero) return newTicks > 0 ? newTicks : 1;
        else return newTicks;
    }

    /**
     * Core TT20 formula: ticks * (smoothedTPS / 20).
     * Reduces tick durations proportionally to TPS drop.
     * Clamped to prevent extreme values at very low TPS.
     */
    public static double rawTT20(double ticks) {
        if (ticks == 0) return 0;
        double tps = Math.max(MIN_EFFECTIVE_TPS, TT20.TPS_CALCULATOR.getSmoothedTPS());
        return ticks * tps / TPSCalculator.MAX_TPS;
    }
}