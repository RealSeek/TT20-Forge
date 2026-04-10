package net.snackbag.tt20.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.snackbag.tt20.TT20;
import net.snackbag.tt20.util.DebtAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Accelerates the attack strength recharge ticker so that
 * combat feels responsive even at low TPS.
 * attackStrengthTicker is incremented each tick in LivingEntity.tick()
 * and used by Player.getAttackStrengthScale() to compute attack power.
 */
@Mixin(LivingEntity.class)
public abstract class AttackCooldownMixin {
    @Shadow
    public int attackStrengthTicker;

    @Unique
    private final DebtAccumulator tt20$attackDebt = new DebtAccumulator();

    @Inject(method = "tick", at = @At("TAIL"))
    private void accelerateAttackCooldown(CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.attackCooldownAcceleration()) return;
        //? if >=1.20.1 {
        if (((Entity) (Object) this).level().isClientSide()) return;
        //?} else {
        /*if (((Entity) (Object) this).getLevel().isClientSide()) return;
        *///?}

        tt20$attackDebt.accumulate();
        int extraTicks = tt20$attackDebt.consumeWhole();
        // Advance the attack strength ticker so recharge feels faster
        attackStrengthTicker += extraTicks;
    }
}
