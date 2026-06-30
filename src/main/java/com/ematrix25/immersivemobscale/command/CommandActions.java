package com.ematrix25.immersivemobscale.command;

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
	private static final String NEW_LINE = "\n", SEPARATOR = NEW_LINE + NEW_LINE, HYPHEN = "-";

	/**
	 * Reloads configuration files and applies categories to loaded entities.
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
		return Main.MOD_NAME + " commands " + SEPARATOR + HYPHEN + String.join(NEW_LINE + HYPHEN, commands);
	}

	/**
	 * Retrieves the statistics of the registered categories and entities
	 * 
	 * @return categories and entities count
	 */
	public static String getStats() {
		return Main.MOD_NAME + " registered data" + SEPARATOR + HYPHEN
				+ String.format("Categories: %d", EntityScaleRegistry.getCategoryCount()) + NEW_LINE + HYPHEN
				+ String.format("Entities:   %d", EntityScaleRegistry.getEntityCount());
	}

	/**
	 * Retrieves the name of the registered categories
	 * 
	 * @return category names
	 */
	public static String getList() {
		return Main.MOD_NAME + " categories " + SEPARATOR + HYPHEN
				+ String.join(NEW_LINE + HYPHEN, EntityScaleRegistry.getCategoryNames());
	}

	/**
	 * Retrieves the name of the registered entities of the given category
	 * 
	 * @param categoryName
	 * @return entities names
	 */
	public static String getList(String categoryName) {
		return "Category " + categoryName.toLowerCase() + " entities " + SEPARATOR + HYPHEN
				+ String.join(NEW_LINE + HYPHEN, EntityScaleRegistry.getEntityNames(categoryName));
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

		Set<String> dataSet = new LinkedHashSet<>();

		dataSet.add(String.format("Entities: %d", category.entities().size()));
		dataSet.add(String.format("Scale:    %.2f", category.scale()));
		dataSet.add(String.format("Speed:    %.2f", category.speed()));

		return "Category " + categoryName + SEPARATOR + HYPHEN + String.join(NEW_LINE + HYPHEN, dataSet);
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
			return "Unknown entity: " + entityName;
		}

		String categoryName = EntityScaleRegistry.getCategoryName(entityId);
		var category = EntityScaleRegistry.getCategory(categoryName);
		Set<String> dataSet = new LinkedHashSet<>();

		if (category == null)
			return "Entity " + entityName + " is not registered to any category";

		dataSet.add(String.format("Category:   %s", categoryName));
		dataSet.add(String.format("Scale Mult: %.2f", category.scale()));
		dataSet.add(String.format("Speed Mult: %.2f", category.speed()));

		if (server != null)
			dataSet.addAll(getExtraEntityInfo(server, entityId));

		return "Entity " + entityName + SEPARATOR + HYPHEN + String.join(NEW_LINE + HYPHEN, dataSet);
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
		if (entityType == null)
			return Set.of();

		Entity entity = entityType.create(server.overworld(), EntitySpawnReason.COMMAND);
		if (!(entity instanceof LivingEntity livingEntity)) {
			if (entity != null)
				entity.discard();
			return Set.of();
		}

		Set<String> dataSet = new LinkedHashSet<>();
		EntityDimensions dimensions = livingEntity.getDimensions(Pose.STANDING), scaledDimensions;
		double healthValue = livingEntity.getAttributeValue(Attributes.MAX_HEALTH);
		boolean hasAttack = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null;
		double attackValue = hasAttack ? livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0;
		double speedValue = getSpeedValue(livingEntity);

		EntityScaleHandler.apply(livingEntity);
		livingEntity.refreshDimensions();

		scaledDimensions = livingEntity.getDimensions(Pose.STANDING);
		dataSet.add(String.format("Dimensions: %.2fW x %.2fH -> %.2fW x %.2fH", dimensions.width(), dimensions.height(),
				scaledDimensions.width(), scaledDimensions.height()));
		dataSet.add(String.format("Health:     %.2f -> %.2f", healthValue,
				livingEntity.getAttributeValue(Attributes.MAX_HEALTH)));
		if (hasAttack)
			dataSet.add(String.format("Attack:     %.2f -> %.2f", attackValue,
					livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE)));
		dataSet.add(String.format("Speed:      %.2f -> %.2f", speedValue, getSpeedValue(livingEntity)));

		entity.discard();

		return dataSet;
	}

	/**
	 * Retrieves the speed attribute value of a given living entity.
	 * 
	 * @return speed attribute value
	 */
	private static double getSpeedValue(LivingEntity livingEntity) {
		return livingEntity.getAttribute(Attributes.FLYING_SPEED) == null
				? livingEntity.getAttributeValue(Attributes.MOVEMENT_SPEED)
				: livingEntity.getAttributeValue(Attributes.FLYING_SPEED);
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
