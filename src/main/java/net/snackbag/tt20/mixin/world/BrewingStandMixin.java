package net.snackbag.tt20.mixin.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.snackbag.tt20.TT20;
import net.snackbag.tt20.util.DebtAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Accelerates brewing stand progression by decrementing brewTime faster.
 * brewTime counts down from 400 to 0.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandMixin {
    @Shadow
    int brewTime;

    // Per-instance debt accumulator, injected into every BrewingStandBlockEntity instance.
    @Unique
    private final DebtAccumulator tt20$brewDebt = new DebtAccumulator();

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void accelerateBrewing(Level level, BlockPos pos, BlockState state,
                                          BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.furnaceAcceleration()) return;

        BrewingStandMixin self = (BrewingStandMixin) (Object) blockEntity;

        // Only accelerate if actively brewing
        if (self.brewTime <= 0) return;

        self.tt20$brewDebt.accumulate();
        int extraTicks = self.tt20$brewDebt.consumeWhole();

        if (extraTicks <= 0) return;

        // brewTime counts DOWN, so subtract extra ticks
        self.brewTime = Math.max(0, self.brewTime - extraTicks);
    }
}
