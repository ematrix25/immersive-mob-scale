package com.ematrix25.immersivemobscale.scale.model;

/**
 * Stores scale data for entities attribute modification.
 */
public record EntityScaleData(float scale, float speed, float health, float attack) {
	/**
	 * Convert category entity data to a simple entity scale data 
	 * 
	 * @param category
	 * @return entity scale data
	 */
	public static EntityScaleData of(EntityScaleCategory category) {
		return new EntityScaleData(category.scale(), category.speed(), category.scale(), category.scale());
	}
}
