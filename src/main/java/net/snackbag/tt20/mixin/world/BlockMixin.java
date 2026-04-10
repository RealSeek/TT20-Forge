package net.snackbag.tt20.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.snackbag.tt20.TT20;
// TPSCalculator accessed via TT20.TPS_CALCULATOR
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class BlockMixin {
    @ModifyReturnValue(method = "getDestroyProgress", at = @At("RETURN"))
    private float onDestroyProgress(float original, @Local Player player) {
        if (!TT20.config.enabled() || !TT20.config.blockBreakingAcceleration()) return original;
        //? if >=1.20.1 {
        if (player.level().isClientSide()) return original;
        //?} else {
        /*if (player.getLevel().isClientSide()) return original;
        *///?}
        return original * (float) TT20.TPS_CALCULATOR.getCompensationFactor();
    }
}
