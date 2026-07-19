package com.ematrix25.immersivemobscale.scale.model;

import java.util.Set;

/**
 * Stores scale settings and entity mappings for a mob category.
 */
public record EntityScaleCategory(ScaleValue scale, ScaleValue speed, Set<String> entities) {
	/**
	 * Generates the entity scale configuration for the category.
	 * 
	 * @return entity scale configuration
	 */
	public EntityScaleConfig config() {
		return new EntityScaleConfig(scale, speed);
	}

	/**
	 * Generates a simplified string representation of the category.
	 *
	 * @return formatted category information
	 */
	@Override
	public String toString() {
		return "(scale=" + scale + ", speed=" + speed + ")";
	}
}