package com.ematrix25.immersivemobscale.scale.model;

import java.util.Random;

/**
 * Stores scale values for entity scale data.
 */
public record ScaleValue(float base, float max) {
	private static final Random RANDOM = new Random();

	/**
	 * Stores only a single value.
	 * 
	 * @param value
	 */
	public ScaleValue(float value) {
		this(value, Float.NaN);
	}

	/**
	 * Tests if scale value is a range of values.
	 * 
	 * @return if has max value
	 */
	public boolean isRange() {
		return !Float.isNaN(max);
	}

	/**
	 * Gets a value or a random value between the range.
	 * 
	 * @return value or random value
	 */
	public float value() {
		return isRange() ? generate() : base;
	}

	/**
	 * Generates a random value between the range.
	 * 
	 * @return random value
	 */
	private float generate() {
		return base + RANDOM.nextFloat() * (max - base);
	}

	/**
	 * Checks if the value is equal or between range values.
	 * 
	 * @param value
	 * @return if equal or contains value
	 */
	public boolean matches(float value) {
		return isRange() ? contains(value) : value == base;
	}

	/**
	 * Checks if the value is between range values.
	 * 
	 * @param value
	 * @return if contains value
	 */
	private boolean contains(float value) {
		return value >= base && value <= max;
	}

	/**
	 * Generates a string representation of the scale value.
	 *
	 * @return formatted scale value
	 */
	@Override
	public String toString() {
		if (isRange())
			return String.format("%.2f-%.2f", base, max);

		return String.format("%.2f", base);
	}
}
