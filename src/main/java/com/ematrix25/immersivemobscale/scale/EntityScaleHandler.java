package com.ematrix25.immersivemobscale.scale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleConfig;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Handles configured scale properties to entities.
 */
public class EntityScaleHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
	private static final Identifier HEALTH_MODIFIER_ID = Identifier.parse(Main.MOD_ID + ":health");
	private static final Identifier DAMAGE_MODIFIER_ID = Identifier.parse(Main.MOD_ID + ":damage");
	private static final Identifier SPEED_MODIFIER_ID = Identifier.parse(Main.MOD_ID + ":speed");
	private static final Identifier SCALE_MODIFIER_ID = Identifier.parse(Main.MOD_ID + ":scale");

	/**
	 * Applies configured scale properties to the given entity.
	 * 
	 * @param entity
	 */
	public static void apply(LivingEntity entity) {
		if (entity == null)
			return;

		Identifier entityId = EntityType.getKey(entity.getType());
		EntityScaleConfig config = EntityScaleRegistry.getEntityScaleConfig(entityId);

		var attachment = EntityScaleAttachment.SCALE_DATA;

		if (config == null) {
			if (entity.hasAttached(attachment))
				entity.removeAttached(attachment);

			updateModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID);
			updateModifier(entity, Attributes.ARMOR, HEALTH_MODIFIER_ID);
			updateModifier(entity, Attributes.ARMOR_TOUGHNESS, HEALTH_MODIFIER_ID);
			updateModifier(entity, Attributes.KNOCKBACK_RESISTANCE, HEALTH_MODIFIER_ID);
			updateModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID);
			updateModifier(entity, Attributes.ATTACK_KNOCKBACK, DAMAGE_MODIFIER_ID);
			updateModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID);
			updateModifier(entity, Attributes.FLYING_SPEED, SPEED_MODIFIER_ID);
			updateModifier(entity, Attributes.ATTACK_SPEED, SPEED_MODIFIER_ID);
			updateModifier(entity, Attributes.SCALE, SCALE_MODIFIER_ID);

			var healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);

			if (healthAttribute != null)
				entity.setHealth((float) healthAttribute.getValue());

			return;
		}

		EntityScaleData data;

		if (!entity.hasAttached(attachment)) {
			data = config.generate();
			entity.setAttached(attachment, data);

			if (Main.debugLogging)
				LOGGER.info("Applied {} attachment to entity {}", EntityScaleAttachment.SCALE_DATA_ID, entityId);
		} else {
			EntityScaleData currentData = entity.getAttached(attachment);
			data = config.update(currentData);

			if (!currentData.equals(data)) {
				entity.setAttached(attachment, data);

				if (Main.debugLogging)
					LOGGER.info("Updated {} attachment to entity {}", EntityScaleAttachment.SCALE_DATA_ID, entityId);
			}
		}

		updateModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, data.health());
		updateModifier(entity, Attributes.ARMOR, HEALTH_MODIFIER_ID, data.health());
		updateModifier(entity, Attributes.ARMOR_TOUGHNESS, HEALTH_MODIFIER_ID, data.health());
		updateModifier(entity, Attributes.KNOCKBACK_RESISTANCE, HEALTH_MODIFIER_ID, data.health());
		updateModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, data.attack());
		updateModifier(entity, Attributes.ATTACK_KNOCKBACK, DAMAGE_MODIFIER_ID, data.attack());
		updateModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, data.speed());
		updateModifier(entity, Attributes.FLYING_SPEED, SPEED_MODIFIER_ID, data.speed());
		updateModifier(entity, Attributes.ATTACK_SPEED, SPEED_MODIFIER_ID, data.speed());
		updateModifier(entity, Attributes.SCALE, SCALE_MODIFIER_ID, data.scale());

		var healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);

		if (healthAttribute != null)
			entity.setHealth((float) healthAttribute.getValue());

		if (Main.debugLogging) {
			String source = EntityScaleRegistry.getCategoryName(entityId);

			LOGGER.info("Applied {} scaling to entity {}", source != null ? source : "entity override", entityId);
		}
	}

	/**
	 * Removes a permanent attribute modifier on an entity.
	 * 
	 * @param entity
	 * @param attribute
	 * @param modifierId
	 */
	private static void updateModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier modifierId) {
		updateModifier(entity, attribute, modifierId, 1.0);
	}

	/**
	 * Updates a permanent attribute modifier on an entity.
	 * 
	 * @param entity
	 * @param attribute
	 * @param modifierId
	 * @param multiplier
	 */
	private static void updateModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier modifierId,
			double multiplier) {
		var attributeInstance = entity.getAttribute(attribute);

		if (attributeInstance == null)
			return;

		var oldModifier = attributeInstance.getModifier(modifierId);

		if (oldModifier != null)
			attributeInstance.removeModifier(oldModifier);

		if (multiplier == 1.0)
			return;

		double amount = attributeInstance.getBaseValue() * (multiplier - 1.0);
		attributeInstance
				.addPermanentModifier(new AttributeModifier(modifierId, amount, AttributeModifier.Operation.ADD_VALUE));
	}

	/**
	 * Returns the current scale multiplier applied to an entity.
	 * 
	 * @param entity
	 * @return current scale, or default 1.0 if there is no scale
	 */
	public static double getCurrentScale(LivingEntity entity) {
		if (entity == null || !entity.hasAttached(EntityScaleAttachment.SCALE_DATA))
			return 1.0;

		return entity.getAttached(EntityScaleAttachment.SCALE_DATA).scale();
	}

	/**
	 * Returns the current difficulty multiplier of a scaled entity.
	 *
	 * @param entity
	 * @return current difficulty multiplier, or default 1.0 if there is no scale
	 */
	public static double getDifficultyMultiplier(LivingEntity entity) {
		if (entity == null || !entity.hasAttached(EntityScaleAttachment.SCALE_DATA))
			return 1.0;

		return entity.getAttached(EntityScaleAttachment.SCALE_DATA).difficultyMultiplier();
	}
}
