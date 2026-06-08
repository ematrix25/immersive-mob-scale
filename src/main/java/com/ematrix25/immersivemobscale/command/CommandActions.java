package com.ematrix25.immersivemobscale.command;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Manages the actions of commands.
 */
public class CommandActions {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
	private static final String NEW_LINE = "\n";

	/**
	 * Reloads configuration files and reapplies categories to loaded entities.
	 * 
	 * @param server
	 */
	public static void reload(MinecraftServer server) {
		ConfigManager.loadConfig(ConfigManager.ConfigType.CATEGORIES);
		EntityScaleRegistry.initialize();

		for (ServerLevel level : server.getAllLevels())
			for (Entity entity : level.getAllEntities())
				if (entity instanceof LivingEntity livingEntity)
					EntityScaleHandler.apply(livingEntity);
		LOGGER.info("Reloaded configuration and reapplied entity categories");
	}

	/**
	 * Toggle to hide or show debug messages.
	 */
	public static void toggleDebug() {
		Main.debugLogging = !Main.debugLogging;
	}

	/**
	 * Retrieves the list of registered commands to String.
	 * 
	 * @return registered commands in string
	 */
	public static String commandsToString(Set<String> commands) {
		return Main.MOD_NAME + " commands " + NEW_LINE + String.join(NEW_LINE, commands);
	}

	/**
	 * Retrieves the statistics of the registered categories and entities
	 * 
	 * @return categories and entities count
	 */
	public static String getStats() {
		return Main.MOD_NAME + " registered data" + NEW_LINE + "Categories: " + EntityScaleRegistry.getCategoryCount()
				+ NEW_LINE + "Entities: " + EntityScaleRegistry.getEntityCount();
	}

	/**
	 * Retrieves the name of the registered categories
	 * 
	 * @return category names
	 */
	public static String getList() {
		return Main.MOD_NAME + " categories " + NEW_LINE
				+ String.join(NEW_LINE, EntityScaleRegistry.getCategoryNames());
	}

	/**
	 * Retrieves the name of the registered entities of the given category
	 * 
	 * @param categoryName
	 * @return entities names
	 */
	public static String getList(String categoryName) {
		return "Category " + categoryName.toLowerCase() + " entities " + NEW_LINE
				+ String.join(NEW_LINE, EntityScaleRegistry.getEntityNames(categoryName));
	}

	/**
	 * Retrieves the data of the given category
	 * 
	 * @param categoryName
	 * @return category data
	 */
	public static String getCategoryInfo(String categoryName) {
		var category = EntityScaleRegistry.getCategory(categoryName);

		if (category == null)
			return "Category " + categoryName + " not found";

		return String.format(
				"Category %s" + NEW_LINE + "Entities: %d" + NEW_LINE + "Scale: %.2f" + NEW_LINE + "Speed: %.2f",
				categoryName, category.entities().size(), category.scale(), category.speed());
	}

	/**
	 * Retrieves the data of the given entity
	 * 
	 * @param entityName
	 * @return entity data
	 */
	public static String getEntityInfo(String entityName) {
		Identifier entityId = Identifier.parse(entityName);
		
		if (entityId == null)
			return "Entity " + entityName + " not found";
		
		String categoryName = EntityScaleRegistry.getCategoryName(entityId);
		var category = EntityScaleRegistry.getCategory(categoryName);
	
		return String.format(
				"Entity %s" + NEW_LINE + "Category: %s" + NEW_LINE + "Scale: %.2f" + NEW_LINE + "Speed: %.2f",
				entityName, categoryName, category.scale(), category.speed());
	}

	/**
	 * Retrieves the current situation of debug.
	 * 
	 * @return situation of debug
	 */
	public static String getDebug() {
		return "Debug logging " + (Main.debugLogging ? "enabled" : "disabled");
	}

	/**
	 * Retrieves the current version in String.
	 * 
	 * @return version in String
	 */
	public static String getVersion() {
		return Main.MOD_NAME + " v" + Main.getVersion().getFriendlyString();
	}
}
