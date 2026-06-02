package com.ematrix25.immersivemobscale.scale;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.config.ConfigManager.ConfigType;

import net.minecraft.resources.Identifier;

/**
 * Registers entity categories for fast runtime lookup.
 */
public class EntityScaleRegistry {
	private static final Map<Identifier, EntityScaleCategory> ENTITY_CATEGORIES = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Loads entity categories into runtime registry.
	 */
	public static void initialize() {
		Map<String, EntityScaleCategory> categories = ConfigManager.getConfig(ConfigType.CATEGORIES);

		if (categories == null)
			return;

		ENTITY_CATEGORIES.clear();
		categories.forEach((name, category) -> {
			if (category.entities() == null || category.entities().isEmpty())
				return;
			for (String entity : category.entities())
				ENTITY_CATEGORIES.put(Identifier.parse(entity), category);
			if (Main.debugLogging)
				LOGGER.info("Registered {} entities to category {}", category.entities().size(), name);
		});
	}

	/**
	 * Gets category for specified entity id.
	 * 
	 * @param entityId
	 * @return registered entity category
	 */
	public static EntityScaleCategory getCategory(Identifier entityId) {
		return ENTITY_CATEGORIES.get(entityId);
	}
}