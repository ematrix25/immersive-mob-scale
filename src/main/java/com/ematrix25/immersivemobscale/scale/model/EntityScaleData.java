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
	 * Convert category entity data to a simple entity scale data.
	 * 
	 * @param category
	 * @return entity scale data
	 */
	public static EntityScaleData of(EntityScaleCategory category) {
		return new EntityScaleData(category.scale(), category.speed(), category.scale(), category.scale());
	}
}
