package com.ematrix25.immersivemobscale.scale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.config.ConfigManager;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Handles configured scale properties to entity by their category.
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
		Identifier entityId = EntityType.getKey(entity.getType());
		EntityScaleCategory category = EntityScaleRegistry.getCategory(entityId);

		if (category == null)
			return;

		var healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
		updateModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, category.scale());
		updateModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, category.scale());
		updateModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, category.speed());
		updateModifier(entity, Attributes.FLYING_SPEED, SPEED_MODIFIER_ID, category.speed());
		updateModifier(entity, Attributes.SCALE, SCALE_MODIFIER_ID, category.scale());

		if (healthAttribute != null)
			entity.setHealth((float) healthAttribute.getValue());

		if (Main.DEBUG_LOGGING)
			LOGGER.info("Applying scale category '{}' {} to entity {}", ConfigManager.getCategoryName(category),
					category, entityId);
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
}
