package com.ematrix25.immersivemobscale.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.EntityScaleCategory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Manages creation, loading and validation of configuration files.
 */
public class ConfigManager {
	private static final String DEFAULT_DIR_NAME = "/default_config/";
	private static final Gson GSON = new Gson();
	private static final Map<String, Object> LOADED_CONFIG = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

	private static Path configDir;

	/**
	 * Configuration file types handled by ConfigManager.
	 */
	public enum ConfigType {
		/**
		 * Entity category configuration.
		 */
		CATEGORIES("categories", new TypeToken<Map<String, EntityScaleCategory>>() {
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
		 * @return
		 */
		public Type getType() {
			return type;
		}
	}

	/**
	 * Starts ConfigManager with default Main folder.
	 */
	public static void initialize() {
		initialize(Main.CONFIG_DIR);
	}

	/**
	 * Starts ConfigManager with the given folder.
	 * 
	 * @param configDir
	 */
	public static void initialize(Path configDir) {
		ConfigManager.configDir = configDir;
		String fileName = ConfigType.CATEGORIES.getFileName();
		Path file = configDir.resolve(fileName);

		try {
			if (Files.notExists(configDir)) {
				Files.createDirectories(configDir);
				LOGGER.info("Configuration directory initialized at {}", configDir);
			}
			if (Files.notExists(file))
				createDefaultFile(fileName, file);

			LOGGER.info("Config system initialized");
		} catch (IOException exception) {
			LOGGER.error("Failed to initialize config system", exception);
		}
	}

	/**
	 * Creates default files after ConfigManager initialization.
	 * 
	 * @param fileName
	 * @param outputFile
	 * @throws IOException
	 */
	private static void createDefaultFile(String fileName, Path outputFile) throws IOException {
		String resourcePath = DEFAULT_DIR_NAME + fileName;
		try (InputStream inputStream = ConfigManager.class.getResourceAsStream(resourcePath)) {
			if (inputStream == null)
				throw new IOException("Missing resource: " + resourcePath);
			Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.info("Created default config file: {}", outputFile.getFileName());
		}
	}

	/**
	 * Loads configuration files after ConfigManager initialization.
	 * 
	 * @param <T>
	 * @param configType
	 */
	public static <T> void loadConfig(ConfigType configType) {
		try {
			Path file = configDir.resolve(configType.getFileName());
			T config = GSON.fromJson(Files.readString(file), configType.getType());

			validate(configType, config);
			LOADED_CONFIG.put(configType.getKey(), config);
			LOGGER.info("Loaded config: {}", configType.getKey());
		} catch (Exception exception) {
			LOGGER.error("Failed to load config: {}", configType.getKey(), exception);
		}
	}

	/**
	 * Validates integrity and values of loaded configuration data.
	 * 
	 * @param <T>
	 * @param configType
	 * @param config
	 */
	@SuppressWarnings("unchecked")
	private static <T> void validate(ConfigType configType, T config) {
		switch (configType) {
		case CATEGORIES -> validateCategories((Map<String, EntityScaleCategory>) config);
		}
	}

	/**
	 * Validates categories configuration file.
	 * 
	 * @param categories
	 */
	private static void validateCategories(Map<String, EntityScaleCategory> categories) {
		Set<String> registeredEntities = new HashSet<>();

		categories.forEach((name, category) -> {
			if (category.scale() < 0.10f || category.scale() > 5.00f)
				LOGGER.warn("Bad scale value {} for category {}. Best use values between 0.1 and 5.0", category.scale(),
						name);
			if (category.speed() < 0.50f || category.speed() > 1.50f)
				LOGGER.warn("Bad speed value {} for category {}. Best use values between 0.5 and 1.5", category.speed(),
						name);
			if (category.entities() == null || category.entities().isEmpty()) {
				LOGGER.warn("Category {} has no entities", name);
				return;
			}
			for (String entity : category.entities()) {
				if (!entity.contains(":"))
					LOGGER.warn("Category {} has invalid entity id {}", name, entity);
				else if (!registeredEntities.add(entity))
					LOGGER.warn("Category {} has a duplicate entity {}", name, entity);
			}
		});
	}

	/**
	 * Gets configuration object for respective configuration type.
	 * 
	 * @param <T>        configuration object type
	 * @param configType
	 * @return loaded configuration object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getConfig(ConfigType configType) {
		return (T) LOADED_CONFIG.get(configType.getKey());
	}
}
