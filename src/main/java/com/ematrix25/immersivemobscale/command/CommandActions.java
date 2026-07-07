package com.ematrix25.immersivemobscale.command;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.config.ConfigType;
import com.ematrix25.immersivemobscale.scale.EntityScaleAttachment;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Manages the actions of commands.
 */
public class CommandActions {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
	private static final String NEW_LINE = "\n", SEPARATOR = NEW_LINE + NEW_LINE, HYPHEN = "-";

	/**
	 * Reloads configuration files and applies scaling to loaded entities.
	 * 
	 * @param source
	 */
	public static void reload(CommandSourceStack source) {
		for (ConfigType configType : ConfigType.values())
			ConfigManager.loadConfig(configType);

		EntityScaleRegistry.initialize();

		for (ServerLevel level : source.getServer().getAllLevels())
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
	 * Retrieves the statistics of the registered categories and entities.
	 * 
	 * @return categories and entities count
	 */
	public static String getStats() {
		return Main.MOD_NAME + " registered data" + SEPARATOR + HYPHEN
				+ String.format("Categories: %d", EntityScaleRegistry.getCategoryCount()) + NEW_LINE + HYPHEN
				+ String.format("Entities:   %d", EntityScaleRegistry.getEntityCount());
	}

	/**
	 * Retrieves the name of the registered categories.
	 * 
	 * @return category names
	 */
	public static String getList() {
		return Main.MOD_NAME + " categories " + SEPARATOR + HYPHEN
				+ String.join(NEW_LINE + HYPHEN, EntityScaleRegistry.getCategoryNames());
	}

	/**
	 * Retrieves the name of the registered entities of the given category.
	 * 
	 * @param categoryName
	 * @return entities names
	 */
	public static String getList(String categoryName) {
		return "Category " + categoryName.toLowerCase() + " entities " + SEPARATOR + HYPHEN
				+ String.join(NEW_LINE + HYPHEN, EntityScaleRegistry.getEntityNames(categoryName));
	}

	/**
	 * Retrieves the data of the given category.
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
	 * Retrieves the data of the player entity.
	 * 
	 * @param source
	 * @return player entity data
	 */
	public static String getSelfInfo(CommandSourceStack source) {
		var player = source.getPlayer();

		if (player == null)
			return "This command can only be executed by a player.";

		Identifier entityId = EntityType.getKey(player.getType());
		String registryInfo = getEntityInfo(entityId);

		if (!Main.debugLogging)
			return registryInfo;

		Set<String> dataSet = new LinkedHashSet<>();

		dataSet.addAll(getEntityAttachment(player));
		dataSet.addAll(getEntityAttributes(player));

		return registryInfo + NEW_LINE + HYPHEN + String.join(NEW_LINE + HYPHEN, dataSet);
	}

	/**
	 * Retrieves the data of the living given entity.
	 * 
	 * @param source
	 * @param entityName
	 * @return living entity data
	 */
	public static String getEntityInfo(CommandSourceStack source, String entityName) {
		Identifier entityId;

		try {
			entityId = Identifier.parse(entityName);
		} catch (Exception e) {
			return "Unknown entity: " + entityName;
		}

		String registryInfo = getEntityInfo(entityId);

		if (!Main.debugLogging)
			return registryInfo;

		LivingEntity livingEntity = findNearestEntity(source, entityId);

		if (livingEntity == null)
			return "No loaded " + entityName + " found nearby.";

		Set<String> dataSet = new LinkedHashSet<>();

		dataSet.addAll(getEntityAttachment(livingEntity));
		dataSet.addAll(getEntityAttributes(livingEntity));

		return registryInfo + NEW_LINE + HYPHEN + String.join(NEW_LINE + HYPHEN, dataSet);
	}

	/**
	 * Retrieves the data of the given entity id.
	 * 
	 * @param entityId
	 * @return entity data
	 */
	public static String getEntityInfo(Identifier entityId) {
		String categoryName = EntityScaleRegistry.getCategoryName(entityId);
		var data = EntityScaleRegistry.getEntityScaleData(entityId);
		Set<String> dataSet = new LinkedHashSet<>();

		if (data == null)
			return "Entity " + entityId + " is not registered";

		dataSet.add(String.format("Category:   %s", (categoryName != null ? categoryName : "None (Entity override)")));
		dataSet.add(String.format("Scale Mult: %.2f", data.scale()));
		dataSet.add(String.format("Speed Mult: %.2f", data.speed()));
		dataSet.add(String.format("Health Mult: %.2f", data.health()));
		dataSet.add(String.format("Attack Mult: %.2f", data.attack()));

		return "Entity " + entityId + SEPARATOR + HYPHEN + String.join(NEW_LINE + HYPHEN, dataSet);
	}

	/**
	 * Find the nearest living entity by its entity id.
	 * 
	 * @param source
	 * @param entityId
	 * @return living entity
	 */
	private static LivingEntity findNearestEntity(CommandSourceStack source, Identifier entityId) {
		double nearestDistance = Double.MAX_VALUE;
		ServerLevel level = source.getLevel();
		LivingEntity nearestEntity = null;

		for (Entity entity : level.getAllEntities()) {
			if (!(entity instanceof LivingEntity livingEntity))
				continue;

			var entityType = livingEntity.getType();

			if (!EntityType.getKey(entityType).equals(entityId))
				continue;

			double distance = livingEntity.distanceToSqr(source.getPosition());

			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestEntity = livingEntity;
			}
		}

		return nearestEntity;

	}

	/**
	 * Retrieves the attachment of the given living entity.
	 * 
	 * @param livingEntity
	 * @return entity attachment
	 */
	private static Set<String> getEntityAttachment(LivingEntity livingEntity) {
		Set<String> dataSet = new LinkedHashSet<>();
		var attachment = EntityScaleAttachment.SCALE_DATA;
		boolean attached = livingEntity.hasAttached(attachment);

		if (attached) {
			EntityScaleData data = livingEntity.getAttached(attachment);

			dataSet.add("Attachment-");
			dataSet.add(String.format("Scale Mult: %.2f", data.scale()));
			dataSet.add(String.format("Speed Mult: %.2f", data.speed()));
			dataSet.add(String.format("Health Mult: %.2f", data.health()));
			dataSet.add(String.format("Attack Mult: %.2f", data.attack()));
		} else
			dataSet.add("No Attachment!-");

		return dataSet;
	}

	/**
	 * Retrieves the attributes of the given living entity.
	 * 
	 * @param livingEntity
	 * @return entity attributes
	 */
	private static Set<String> getEntityAttributes(LivingEntity livingEntity) {
		Set<String> dataSet = new LinkedHashSet<>();
		Entity tempEntity = (livingEntity instanceof Player) ? FakePlayer.get((ServerLevel) livingEntity.level())
				: livingEntity.getType().create(livingEntity.level(), EntitySpawnReason.COMMAND);

		if (tempEntity instanceof LivingEntity tempLivingEntity) {
			EntityDimensions dimensions = tempLivingEntity.getDimensions(Pose.STANDING);
			double healthValue = tempLivingEntity.getAttributeValue(Attributes.MAX_HEALTH);
			boolean hasAttack = tempLivingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null;
			double attackValue = hasAttack ? tempLivingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0;
			double speedValue = getSpeedValue(tempLivingEntity);

			EntityDimensions scaledDimensions = livingEntity.getDimensions(Pose.STANDING);

			dataSet.add("Attributes-");
			dataSet.add(String.format("Dimensions: %.2fW x %.2fH -> %.2fW x %.2fH", dimensions.width(),
					dimensions.height(), scaledDimensions.width(), scaledDimensions.height()));
			dataSet.add(String.format("Health:     %.2f -> %.2f", healthValue,
					livingEntity.getAttributeValue(Attributes.MAX_HEALTH)));

			if (hasAttack)
				dataSet.add(String.format("Attack:     %.2f -> %.2f", attackValue,
						livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE)));

			dataSet.add(String.format("Speed:      %.2f -> %.2f", speedValue, getSpeedValue(livingEntity)));
		}

		if (tempEntity != null)
			tempEntity.discard();

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
