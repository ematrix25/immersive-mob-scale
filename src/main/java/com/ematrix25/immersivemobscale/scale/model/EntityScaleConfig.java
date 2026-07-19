package com.ematrix25.immersivemobscale.scale.model;

/**
 * Stores scale values from configuration to entity scale data.
 */
public record EntityScaleConfig(ScaleValue scale, ScaleValue speed, ScaleValue health, ScaleValue attack) {
	/**
	 * Creates a configuration using scale for health and attack.
	 * 
	 * @param scale
	 * @param speed
	 */
	public EntityScaleConfig(ScaleValue scale, ScaleValue speed) {
		this(scale, speed, null, null);
	}

	/**
	 * Generates concrete entity scale data from this configuration.
	 *
	 * @return generated entity scale data
	 */
	public EntityScaleData generate() {
		float scaleValue = scale.value();

		return new EntityScaleData(scaleValue, speed.value(), health != null ? health.value() : scaleValue,
				attack != null ? attack.value() : scaleValue);
	}

	/**
	 * Updates current entity scale data according to this configuration.
	 *
	 * @param currentData
	 * @return updated entity scale data
	 */
	public EntityScaleData update(EntityScaleData currentData) {
		float scaleValue = updateValue(scale, currentData.scale());

		return new EntityScaleData(scaleValue, updateValue(speed, currentData.speed()),
				health != null ? updateValue(health, currentData.health()) : scaleValue,
				attack != null ? updateValue(attack, currentData.attack()) : scaleValue);
	}

	/**
	 * Updates a single value from the current entity data.
	 *
	 * @param configValue
	 * @param currentValue
	 * @return preserved or regenerated value
	 */
	private static float updateValue(ScaleValue configValue, float currentValue) {
		return configValue.matches(currentValue) ? currentValue : configValue.value();
	}
}
