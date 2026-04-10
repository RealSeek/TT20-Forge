package net.snackbag.tt20.util;

import net.snackbag.tt20.TT20;

/**
 * A reusable fractional debt accumulator for TPS compensation.
 * <p>
 * Each subsystem/entity that needs catch-up ticking should own one instance.
 * Every server tick, call {@link #accumulate()} to add fractional debt based on
 * the current compensation factor, then call {@link #consumeWhole()} to get the
 * number of whole extra ticks to execute.
 * <p>
 * This avoids the old global missed-tick bucket problem and provides smooth,
 * per-target compensation even at mild TPS drops (17-19 TPS).
 */
public class DebtAccumulator {
    private double debt = 0.0;

    /**
     * Accumulates fractional debt based on the current TPS compensation factor.
     * Should be called once per server tick for this target.
     */
    public void accumulate() {
        double extra = TT20.TPS_CALCULATOR.getCompensationFactor() - 1.0;
        if (extra > 0) {
            debt += extra;
        }
    }

    /**
     * Accumulates debt with a custom extra-per-tick value.
     *
     * @param extraPerTick the fractional extra ticks to add
     */
    public void accumulate(double extraPerTick) {
        if (extraPerTick > 0) {
            debt += extraPerTick;
        }
    }

    /**
     * Consumes and returns the number of whole extra ticks available,
     * clamped to the configured maximum.
     *
     * @return number of extra ticks to execute (0 if none available)
     */
    public int consumeWhole() {
        int maxExtra = TT20.config.maxExtraTicksPerTick();
        int whole = (int) debt;
        if (whole > maxExtra) {
            whole = maxExtra;
        }
        debt -= whole;
        return whole;
    }

    /**
     * Consumes and returns the number of whole extra ticks available,
     * with a custom cap override.
     *
     * @param maxExtra the maximum extra ticks to return
     * @return number of extra ticks to execute (0 if none available)
     */
    public int consumeWhole(int maxExtra) {
        int whole = (int) debt;
        if (whole > maxExtra) {
            whole = maxExtra;
        }
        debt -= whole;
        return whole;
    }

    /**
     * Returns the current fractional debt without consuming it.
     */
    public double getDebt() {
        return debt;
    }

    /**
     * Resets the accumulator to zero. Call on entity unload or when compensation
     * should stop cleanly.
     */
    public void reset() {
        debt = 0.0;
    }
}
