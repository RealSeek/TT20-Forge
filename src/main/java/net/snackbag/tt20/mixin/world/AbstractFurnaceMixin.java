package net.snackbag.tt20.mixin.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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
 * Accelerates furnace/blast furnace/smoker cooking and fuel consumption
 * by advancing internal counters proportionally to TPS compensation.
 * Direct counter adjustment is safer than replaying the entire serverTick.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceMixin {
    @Shadow
    int cookingProgress;
    @Shadow
    int cookingTotalTime;
    @Shadow
    int litTime;

    // Per-instance debt accumulator, injected into every AbstractFurnaceBlockEntity instance.
    // This avoids the ThreadLocal bug where all furnaces shared one accumulator.
    @Unique
    private final DebtAccumulator tt20$furnaceDebt = new DebtAccumulator();

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void accelerateFurnace(Level level, BlockPos pos, BlockState state,
                                          AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (!TT20.config.enabled() || !TT20.config.furnaceAcceleration()) return;

        AbstractFurnaceMixin self = (AbstractFurnaceMixin) (Object) blockEntity;

        // Only accelerate if actively cooking
        if (self.cookingProgress <= 0 || self.cookingTotalTime <= 0) return;

        self.tt20$furnaceDebt.accumulate();
        int extraTicks = self.tt20$furnaceDebt.consumeWhole();

        if (extraTicks <= 0) return;

        // Advance cooking progress, capped at total time
        self.cookingProgress = Math.min(self.cookingProgress + extraTicks, self.cookingTotalTime);

        // Consume fuel proportionally if lit
        if (self.litTime > 0) {
            self.litTime = Math.max(0, self.litTime - extraTicks);
        }
    }
}
