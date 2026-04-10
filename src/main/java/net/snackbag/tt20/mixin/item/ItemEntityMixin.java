package net.snackbag.tt20.mixin.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.snackbag.tt20.TT20;
import net.snackbag.tt20.util.DebtAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private int pickupDelay;

    @Unique
    private final DebtAccumulator tt20$pickupDebt = new DebtAccumulator();

    @Inject(method = "tick", at = @At("HEAD"))
    private void pickupDelayTT20(CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.pickupAcceleration()) return;
        //? if >=1.20.1 {
        if (((Entity) (Object) this).level().isClientSide()) return;
        //?} else {
        /*if (((Entity) (Object) this).getLevel().isClientSide()) return;
        *///?}
        if (pickupDelay == 0) return;

        tt20$pickupDebt.accumulate();
        int reduction = tt20$pickupDebt.consumeWhole();
        pickupDelay = Math.max(0, pickupDelay - reduction);
    }
}
