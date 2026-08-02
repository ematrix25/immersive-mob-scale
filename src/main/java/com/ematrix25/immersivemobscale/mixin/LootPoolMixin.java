package com.ematrix25.immersivemobscale.mixin;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Adapts Loot Pool behavior for custom scaling.
 */
@Mixin(LootPool.class)
public abstract class LootPoolMixin {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Adjusts the loot dropped.
	 */
	@ModifyVariable(method = "addRandomItems", at = @At("STORE"), ordinal = 0)
	private int scaleLoot(int count, Consumer<ItemStack> result, LootContext context) {
		Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);

		if (!(entity instanceof LivingEntity living))
			return count;

		double difficulty = EntityScaleHandler.getDifficultyMultiplier(living);
		int scaledCount = Math.max(1, Math.round((float) (count * difficulty)));

		if (Main.debugLogging)
			LOGGER.info("Loot Pool for Entity '{}': difficulty = {}; base loot count = {}; scaled loot count = {}",
					entity.getType(), String.format("%.2f", difficulty), count, scaledCount);

		return scaledCount;
	}
}
