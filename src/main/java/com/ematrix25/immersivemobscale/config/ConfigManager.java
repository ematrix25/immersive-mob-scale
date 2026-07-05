package com.ematrix25.immersivemobscale.config;

import java.io.IOException;
import java.io.InputStream;
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
import com.ematrix25.immersivemobscale.scale.model.EntityScaleCategory;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;
import com.google.gson.Gson;

import net.minecraft.resources.Identifier;

/**
 * Manages creation, loading and validation of configuration files.
 */
public class ConfigManager {
	private static final String DEFAULT_DIR_NAME = "/default_config/";
	private static final Gson GSON = new Gson();
	private static final Map<ConfigType, Object> LOADED_CONFIG = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	private static Path configDir;

	/**
	 * Starts ConfigManager with the given folder.
	 * 
	 * @param configDir
	 */
	public static void initialize(Path configDir) {
		ConfigManager.configDir = configDir;
		String fileName;
		Path file;

		try {
			for (ConfigType configType : ConfigType.values()) {
				fileName = configType.getFileName();
				file = configDir.resolve(fileName);

				if (Files.notExists(configDir)) {
					Files.createDirectories(configDir);
					if (Main.debugLogging)
						LOGGER.info("Configuration directory initialized at {}", configDir);
				}
				if (Files.notExists(file))
					createDefaultFile(fileName, file);
			}
			if (Main.debugLogging)
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
			if (Main.debugLogging)
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
			LOADED_CONFIG.put(configType, config);
			if (Main.debugLogging)
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
		case ENTITIES -> validateEntities((Map<String, EntityScaleData>) config);
		}
	}

	/**
	 * Validates categories configuration file.
	 * 
	 * @param categories
	 */
	private static void validateCategories(Map<String, EntityScaleCategory> categories) {
		Set<Identifier> registeredEntities = new HashSet<>();

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
				Identifier entityId = Identifier.tryParse(entity);
				if (entityId == null)
					LOGGER.warn("Category {} has invalid entity id {}", name, entity);
				else if (!registeredEntities.add(entityId))
					LOGGER.warn("Category {} has a duplicate entity {}", name, entity);
			}
		});
	}

	/**
	 * Validates entities configuration file.
	 * 
	 * @param entities
	 */
	private static void validateEntities(Map<String, EntityScaleData> entities) {
		entities.forEach((name, data) -> {
			if (data.scale() < 0.10f || data.scale() > 5.00f)
				LOGGER.warn("Bad scale value {} for category {}. Best use values between 0.1 and 5.0", data.scale(),
						name);
			if (data.speed() < 0.50f || data.speed() > 1.50f)
				LOGGER.warn("Bad speed value {} for category {}. Best use values between 0.5 and 1.5", data.speed(),
						name);
			if (data.health() < 0.10f || data.health() > 5.00f)
				LOGGER.warn("Bad scale value {} for category {}. Best use values between 0.1 and 5.0", data.health(),
						name);
			if (data.attack() < 0.10f || data.attack() > 5.00f)
				LOGGER.warn("Bad scale value {} for category {}. Best use values between 0.1 and 5.0", data.attack(),
						name);
			if (Identifier.tryParse(name) == null)
				LOGGER.warn("Entity {} is an invalid entity id", name);
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
		return (T) LOADED_CONFIG.get(configType);
	}

	/**
	 * Gets category name from the respective category object.
	 * 
	 * @param category
	 * @return category name
	 */
	public static String getCategoryName(EntityScaleCategory category) {
		Map<String, EntityScaleCategory> categories = getConfig(ConfigType.CATEGORIES);

		for (Map.Entry<String, EntityScaleCategory> entry : categories.entrySet())
			if (entry.getValue().equals(category))
				return entry.getKey();

		return "unknown";
	}

	/**
	 * Gets the number of loaded configurations.
	 *
	 * @return loaded configuration count
	 */
	public static int getLoadedConfigCount() {
		return LOADED_CONFIG.size();
	}
}
