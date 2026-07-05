package com.ematrix25.immersivemobscale.config;

import java.lang.reflect.Type;
import java.util.Map;

import com.ematrix25.immersivemobscale.scale.model.EntityScaleCategory;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;
import com.google.gson.reflect.TypeToken;

/**
 * Configuration file types handled by ConfigManager.
 */
public enum ConfigType {
	/**
	 * Entity category configuration.
	 */
	CATEGORIES("categories", new TypeToken<Map<String, EntityScaleCategory>>() {
	}.getType()),
	/**
	 * Entity configuration.
	 */
	ENTITIES("entities", new TypeToken<Map<String, EntityScaleData>>() {
	}.getType());

	private final String key;
	private final Type type;

	ConfigType(String key, Type type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Gets internal configuration identifier.
	 * 
	 * @return configuration identifier
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Gets configuration file name.
	 * 
	 * @return configuration file name
	 */
	public String getFileName() {
		return this.key + ".json";
	}

	/**
	 * Gets configuration loaded object type.
	 * 
	 * @return configuration object type
	 */
	public Type getType() {
		return type;
	}
}
