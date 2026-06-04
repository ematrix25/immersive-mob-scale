package com.ematrix25.immersivemobscale.scale;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	private static final Map<Identifier, String> ENTITY_CATEGORIES = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	private static Map<String, EntityScaleCategory> categories = new HashMap<>();

	/**
	 * Loads entity categories into runtime registry.
	 */
	public static void initialize() {
		categories = ConfigManager.getConfig(ConfigType.CATEGORIES);

		if (categories == null)
			return;

		ENTITY_CATEGORIES.clear();
		categories.forEach((name, category) -> {
			if (category.entities() == null || category.entities().isEmpty())
				return;
			for (String entity : category.entities())
				ENTITY_CATEGORIES.put(Identifier.parse(entity), name);
			if (Main.debugLogging)
				LOGGER.info("Registered {} entities to category {}", category.entities().size(), name);
		});
	}

	/**
	 * Gets a category by its name.
	 *
	 * @param categoryName
	 * @return category
	 */
	public static EntityScaleCategory getCategory(String categoryName) {
		return categories.get(categoryName);
	}

	/**
	 * Gets category for specified entity id.
	 * 
	 * @param entityId
	 * @return registered entity category
	 */
	public static EntityScaleCategory getCategory(Identifier entityId) {
		return getCategory(ENTITY_CATEGORIES.get(entityId));
	}

	/**
	 * Gets all registered entities names.
	 *
	 * @return entities names
	 */
	public static Set<String> getEntitiesNames(String categoryName) {
		var category = getCategory(categoryName);

		return category != null ? category.entities() : Set.of();
	}

	/**
	 * Gets all registered category names.
	 *
	 * @return category names
	 */
	public static Set<String> getCategoryNames() {
		return categories.keySet();
	}

	/**
	 * Gets the number of registered categories.
	 *
	 * @return category count
	 */
	public static int getCategoryCount() {
		return categories.size();
	}

	/**
	 * Gets the number of registered entities.
	 *
	 * @return entity count
	 */
	public static int getEntityCount() {
		return ENTITY_CATEGORIES.size();
	}
}