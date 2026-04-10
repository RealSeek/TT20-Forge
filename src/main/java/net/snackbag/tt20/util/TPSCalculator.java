package net.snackbag.tt20.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.snackbag.tt20.TT20;

import java.util.ArrayDeque;
import java.util.Deque;

public class TPSCalculator {
    private long lastTick = -1L;
    private long currentTick = -1L;
    private long cachedMSPT = FULL_TICK_MS;

    // EMA-smoothed TPS, more stable and responsive than min(instant, avg)
    private double smoothedTPS = MAX_TPS;
    private static final double EMA_ALPHA = 0.3;

    // Missed tick accumulator - consumed explicitly after all mixins read it
    private double allMissedTicks = 0.0;
    private int cachedApplicableMissedTicks = 0;

    // Ring buffer for TPS history (used for display/average only)
    private final Deque<Double> tpsHistory = new ArrayDeque<>();
    private static final int HISTORY_LIMIT = 40;

    public static final int MAX_TPS = 20;
    public static final int FULL_TICK_MS = 50;

    public TPSCalculator() {
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (currentTick >= 0L) {
            lastTick = currentTick;
        }
        currentTick = System.currentTimeMillis();

        // Cache MSPT for this tick to avoid repeated computation
        cachedMSPT = (lastTick < 0L) ? FULL_TICK_MS : Math.max(1, currentTick - lastTick);

        double currentTPS = getTPS();
        addToHistory(currentTPS);

        // Update EMA-smoothed TPS
        if (currentTPS > 0) {
            smoothedTPS = smoothedTPS * (1.0 - EMA_ALPHA) + currentTPS * EMA_ALPHA;
        }

        // First: snapshot the accumulated missed ticks for this tick's consumers
        // Consumers read cachedApplicableMissedTicks during the tick
        cachedApplicableMissedTicks = (int) Math.floor(allMissedTicks);

        // Then: consume what was snapshotted and accumulate new misses
        allMissedTicks -= cachedApplicableMissedTicks;
        recordMissedTicks();
    }

    private void addToHistory(double tps) {
        if (tpsHistory.size() >= HISTORY_LIMIT) {
            tpsHistory.pollFirst();
        }
        tpsHistory.addLast(tps);
    }

    public long getMSPT() {
        return cachedMSPT;
    }

    public double getTPS() {
        if (lastTick < 0L) return MAX_TPS;
        long mspt = getMSPT();
        if (mspt <= 0) return MAX_TPS;
        double tps = 1000.0 / mspt;
        return Math.min(tps, MAX_TPS);
    }

    public double getAverageTPS() {
        if (tpsHistory.isEmpty()) return MAX_TPS;
        double sum = 0;
        for (double tps : tpsHistory) {
            sum += tps;
        }
        return sum / tpsHistory.size();
    }

    /**
     * Returns the EMA-smoothed TPS, clamped to [0.1, MAX_TPS].
     * This replaces the old Math.min(instant, avg) which was biased toward worst-case.
     */
    public double getSmoothedTPS() {
        return Math.max(0.1, Math.min(MAX_TPS, smoothedTPS));
    }

    /**
     * Returns the TPS compensation factor: 20 / smoothedTPS.
     * Clamped to [1.0, maxCompensationFactor] from config.
     * Use this to scale rates/progress that need to be FASTER when TPS is low.
     * e.g. block break progress, random tick speed.
     */
    public double getCompensationFactor() {
        double factor = MAX_TPS / getSmoothedTPS();
        double maxFactor = TT20.config.maxCompensationFactor();
        return Math.min(factor, maxFactor);
    }

    /**
     * Returns the TPS scaling factor: smoothedTPS / 20.
     * Clamped to [1/maxCompensationFactor, 1.0].
     * Use this to scale durations that need to be SHORTER when TPS is low.
     * e.g. eating duration, fluid delay, portal wait time.
     */
    public double getScalingFactor() {
        double factor = getSmoothedTPS() / MAX_TPS;
        double minFactor = 1.0 / TT20.config.maxCompensationFactor();
        return Math.max(factor, minFactor);
    }

    private void recordMissedTicks() {
        if (lastTick < 0L) return;
        long mspt = cachedMSPT;
        double missed = (mspt / (double) FULL_TICK_MS) - 1;
        if (missed > 0) {
            allMissedTicks += missed;
        }
    }

    public double getAllMissedTicks() {
        return allMissedTicks;
    }

    /**
     * Returns the snapshotted missed ticks for this tick.
     * Safe to call multiple times within the same tick - always returns the same value.
     */
    public int applicableMissedTicks() {
        return cachedApplicableMissedTicks;
    }

    public void resetMissedTicks() {
        allMissedTicks = 0;
        cachedApplicableMissedTicks = 0;
    }
}
