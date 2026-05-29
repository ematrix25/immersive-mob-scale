package com.ematrix25.immersivemobscale.scale;

import java.util.List;

/**
 * Stores scale settings and entity mappings for a mob category.
 */
public record EntityScaleCategory(float scale, float speed, List<String> entities) {
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