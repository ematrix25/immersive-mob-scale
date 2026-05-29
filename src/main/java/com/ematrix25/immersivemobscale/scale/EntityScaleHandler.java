package com.ematrix25.immersivemobscale.scale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles configured scale properties to entity by their category.
 */
public class EntityScaleHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Applies configured scale properties to the given entity.
	 * 
	 * @param entity
	 */
	public static void apply(LivingEntity entity) {
		Identifier entityId = EntityType.getKey(entity.getType());
		EntityScaleCategory category = EntityScaleRegistry.getCategory(entityId);

		if (category == null)
			return;
		else if (Main.DEBUG_LOGGING)
			LOGGER.info("Applying scale category '{}' {} to entity {}", ConfigManager.getCategoryName(category),
					category, entityId);

	}
}
