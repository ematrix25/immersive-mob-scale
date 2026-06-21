package com.ematrix25.immersivemobscale.command;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Manages the actions of commands.
 */
public class CommandActions {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
	private static final String NEW_LINE = "\n", EMPTY = "";

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

		Set<String> dataSet = new HashSet<>();

		dataSet.add("Entities: " + category.entities().size());
		dataSet.add("Scale: " + category.scale());
		dataSet.add("Speed: " + category.speed());

		return "Category " + categoryName + NEW_LINE + String.join(NEW_LINE, dataSet);
	}

	/**
	 * Retrieves the data of the given entity
	 * 
	 * @param entityName
	 * @return entity data
	 */
	public static String getEntityInfo(String entityName) {
		return getEntityInfo(null, entityName);
	}

	/**
	 * Retrieves the data of the given entity
	 * 
	 * @param server
	 * @param entityName
	 * @return entity data
	 */
	public static String getEntityInfo(MinecraftServer server, String entityName) {
		Identifier entityId;

		try {
			entityId = Identifier.parse(entityName);
		} catch (Exception e) {
			return "Entity " + entityName + " not found";
		}

		String categoryName = EntityScaleRegistry.getCategoryName(entityId);
		var category = EntityScaleRegistry.getCategory(categoryName);
		Set<String> dataSet = new LinkedHashSet<>();

		dataSet.add("Category: " + categoryName);
		dataSet.add("Scale Mult: " + category.scale());
		dataSet.add("Speed Mult: " + category.speed());

		if (server != null)
			dataSet.addAll(getExtraEntityInfo(server, entityId));

		return "Entity " + entityName + NEW_LINE + String.join(NEW_LINE, dataSet);
	}

	/**
	 * Retrieves the extra data of the given entity id
	 * 
	 * @param server
	 * @param entityId
	 * @return extra entity data
	 */
	private static Set<String> getExtraEntityInfo(MinecraftServer server, Identifier entityId) {
		EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
		Entity entity = entityType.create(server.overworld(), EntitySpawnReason.COMMAND);

		if (!(entity instanceof LivingEntity livingEntity))
			return Set.of();

		EntityDimensions dimensions = livingEntity.getDimensions(Pose.STANDING);
		Set<String> dataSet = new LinkedHashSet<>();
		String dimension = String.format("Dimensions: %.2fW x %.2fH", dimensions.width(), dimensions.height());
		String health = "Health: " + String.format("%.2f", livingEntity.getAttributeValue(Attributes.MAX_HEALTH));
		String attack = EMPTY;
		if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null)
			attack = "Attack: " + String.format("%.2f", livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE));
		String speed = "Speed: " + String.format("%.2f",
				(livingEntity.getAttribute(Attributes.FLYING_SPEED) == null
						? livingEntity.getAttributeValue(Attributes.MOVEMENT_SPEED)
						: livingEntity.getAttributeValue(Attributes.FLYING_SPEED)));

		EntityScaleHandler.apply(livingEntity);
		livingEntity.refreshDimensions();

		dimensions = livingEntity.getDimensions(Pose.STANDING);
		dataSet.add(dimension + String.format(" -> %.2fW x %.2fH", dimensions.width(), dimensions.height()));
		dataSet.add(health + " -> " + String.format("%.2f", livingEntity.getAttributeValue(Attributes.MAX_HEALTH)));
		if (!attack.isEmpty())
			dataSet.add(
					attack + " -> " + String.format("%.2f", livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE)));
		dataSet.add(speed + " -> "
				+ String.format("%.2f",
						(livingEntity.getAttribute(Attributes.FLYING_SPEED) == null
								? livingEntity.getAttributeValue(Attributes.MOVEMENT_SPEED)
								: livingEntity.getAttributeValue(Attributes.FLYING_SPEED))));
		entity.discard();

		return dataSet;
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
