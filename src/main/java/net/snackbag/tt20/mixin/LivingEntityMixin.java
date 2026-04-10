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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    protected abstract void tickEffects();

    @Unique
    private final DebtAccumulator tt20$effectDebt = new DebtAccumulator();

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tickEffects()V"))
    private void fixPotionDelayTick(CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.potionEffectAcceleration()) return;
        //? if >=1.20.1 {
        if (((Entity) (Object) this).level().isClientSide()) return;
        //?} else {
        /*if (((Entity) (Object) this).getLevel().isClientSide()) return;
        *///?}

        tt20$effectDebt.accumulate();
        int extraTicks = tt20$effectDebt.consumeWhole();
        for (int i = 0; i < extraTicks; i++) {
            tickEffects();
        }
    }
}
