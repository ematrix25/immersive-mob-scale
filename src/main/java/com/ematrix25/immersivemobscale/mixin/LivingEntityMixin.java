package com.ematrix25.immersivemobscale.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.entity.LivingEntity;

/**
 * Adapts Living Entity behavior for custom scaling.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Adjusts the experience dropped.
	 */
	@ModifyReturnValue(method = "getExperienceReward", at = @At("RETURN"))
	private int scaleExperience(int experience) {
		LivingEntity entity = (LivingEntity) (Object) this;
		double difficulty = EntityScaleHandler.getDifficultyMultiplier(entity);
		int scaledExperience = Math.round((float) (experience * difficulty));

		if (Main.debugLogging)
			LOGGER.info("Living Entity '{}': difficulty = {}; base xp = {}; scaled xp = {}", entity.getType(),
					String.format("%.2f", difficulty), experience, scaledExperience);

		return scaledExperience;
	}
}
