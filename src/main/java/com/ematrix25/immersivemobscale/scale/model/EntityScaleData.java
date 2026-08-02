package com.ematrix25.immersivemobscale.scale.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stores scale data for entities attribute modification.
 */
public record EntityScaleData(float scale, float speed, float health, float attack) {
	public static final Codec<EntityScaleData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("scale").forGetter(EntityScaleData::scale),
					Codec.FLOAT.fieldOf("speed").forGetter(EntityScaleData::speed),
					Codec.FLOAT.fieldOf("health").forGetter(EntityScaleData::health),
					Codec.FLOAT.fieldOf("attack").forGetter(EntityScaleData::attack))
			.apply(instance, EntityScaleData::new));

	/**
	 * Calculates the difficulty multiplier for the scaled entity.
	 * 
	 * @return difficulty multiplier
	 */
	public float difficultyMultiplier() {
		return speed() * 0.2f + health() * 0.5f + attack() * 0.3f;
	}
}
