package net.snackbag.tt20.mixin.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb; // Mixin target
import net.snackbag.tt20.TT20;
import net.snackbag.tt20.util.DebtAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Accelerates experience orb aging and collection so XP pickup
 * feels responsive at low TPS. XP orbs track their age internally
 * and become collectible after a short delay.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Unique
    private final DebtAccumulator tt20$xpDebt = new DebtAccumulator();

    @Inject(method = "tick", at = @At("TAIL"))
    private void accelerateXpOrb(CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.xpPickupAcceleration()) return;
        //? if >=1.20.1 {
        if (((Entity) (Object) this).level().isClientSide()) return;
        //?} else {
        /*if (((Entity) (Object) this).getLevel().isClientSide()) return;
        *///?}

        tt20$xpDebt.accumulate();
        int extraTicks = tt20$xpDebt.consumeWhole();
        // Advance the orb's age so it expires/becomes collectible faster
        // Entity.tickCount is public int — direct access is safe
        ((Entity) (Object) this).tickCount += extraTicks;
    }
}
