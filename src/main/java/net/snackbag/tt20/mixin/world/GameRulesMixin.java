package net.snackbag.tt20.mixin.world;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.snackbag.tt20.TT20;
// TPSCalculator accessed via TT20.TPS_CALCULATOR
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRules.class)
public abstract class GameRulesMixin {
    @ModifyReturnValue(method = "getInt", at = @At("RETURN"))
    private int randomTickSpeedAcceleration(int original, @Local(argsOnly = true) GameRules.Key<GameRules.IntegerValue> rule) {
        if (!TT20.config.enabled() || !TT20.config.randomTickSpeedAcceleration()) return original;
        if (!(rule == GameRules.RULE_RANDOMTICKING)) return original;
        return (int) (original * TT20.TPS_CALCULATOR.getCompensationFactor());
    }
}
