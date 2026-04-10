package net.snackbag.tt20.mixin.item;

import net.minecraft.world.item.ItemCooldowns;
import net.snackbag.tt20.TT20;
import net.snackbag.tt20.util.DebtAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Accelerates item cooldowns (ender pearl, chorus fruit, shield, etc.)
 * by advancing the internal tick counter faster when TPS is low.
 * This is one of the most player-noticeable lag symptoms.
 */
@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin {
    @Shadow
    private int tickCount;

    @Unique
    private final DebtAccumulator tt20$cooldownDebt = new DebtAccumulator();

    @Inject(method = "tick", at = @At("TAIL"))
    private void accelerateCooldowns(CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.itemCooldownAcceleration()) return;

        tt20$cooldownDebt.accumulate();
        int extraTicks = tt20$cooldownDebt.consumeWhole();
        // Advance tick counter so cooldowns expire sooner.
        // Expired entries will be cleaned up by the next natural tick() call.
        tickCount += extraTicks;
    }
}
