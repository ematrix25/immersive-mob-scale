package com.ematrix25.immersivemobscale.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * Adapts Mob behavior for custom scaling.
 */
@Mixin(Mob.class)
public abstract class MobMixin {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Adjusts the attack range through the bounding box.
	 * 
	 * @param horizontalExpansion
	 * @param cir
	 */
	@Inject(at = @At("RETURN"), method = "getAttackBoundingBox", cancellable = true)
	private void adjustAttackBoundingBox(double horizontalExpansion, CallbackInfoReturnable<AABB> cir) {
		AABB box = cir.getReturnValue();

		if (box == null)
			return;

		Mob mob = (Mob) (Object) this;
		double scale = EntityScaleHandler.getCurrentScale(mob);
		double scaledExpansion = horizontalExpansion * (scale - 1.0);
		AABB modBox = box.inflate(scaledExpansion, 0.0, scaledExpansion);

		if (Main.debugLogging)
			LOGGER.info(
					"Mob '{}': scale = {}; expansion = {}; scaledExpansion = {}; attackBox = {} => {} ",
					mob.getType(), scale, horizontalExpansion, horizontalExpansion + scaledExpansion, box, modBox);

		cir.setReturnValue(modBox);
	}
}
