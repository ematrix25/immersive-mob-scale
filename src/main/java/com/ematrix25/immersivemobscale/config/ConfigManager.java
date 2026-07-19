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
import com.ematrix25.immersivemobscale.scale.model.EntityScaleConfig;
import com.ematrix25.immersivemobscale.scale.model.ScaleValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;

import net.minecraft.resources.Identifier;

/**
 * Manages creation, loading and validation of configuration files.
 */
public class ConfigManager {
	private static final String DEFAULT_DIR_NAME = "/default_config/";
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ScaleValue.class, (JsonDeserializer<ScaleValue>) (json, _, _) -> {
				if (json.isJsonPrimitive())
					return new ScaleValue(json.getAsFloat());

				JsonArray array = json.getAsJsonArray();
				return new ScaleValue(array.get(0).getAsFloat(), array.get(1).getAsFloat());
			}).setPrettyPrinting().create();
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
		case ENTITIES -> validateEntities((Map<String, EntityScaleConfig>) config);
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
			var config = category.config();

			validateScaleValue(config.scale(), "scale", name, 0.10f, 5.00f);
			validateScaleValue(config.speed(), "speed", name, 0.50f, 1.50f);

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
	private static void validateEntities(Map<String, EntityScaleConfig> entities) {
		entities.forEach((name, config) -> {
			validateScaleValue(config.scale(), "scale", name, 0.10f, 5.00f);
			validateScaleValue(config.speed(), "speed", name, 0.50f, 1.50f);
			validateScaleValue(config.health(), "health", name, 0.10f, 5.00f);
			validateScaleValue(config.attack(), "attack", name, 0.10f, 5.00f);

			if (Identifier.tryParse(name) == null)
				LOGGER.warn("Entity {} is an invalid entity id", name);
		});
	}

	/**
	 * Validates if scale value is between limits.
	 * 
	 * @param value
	 * @param valueName
	 * @param name
	 * @param min
	 * @param max
	 */
	private static void validateScaleValue(ScaleValue value, String valueName, String name, float min, float max) {
		if (value == null)
			return;

		float base = value.base();
		float upper = value.max();

		if ((value.isRange() && ((upper > base && (upper < min || base > max)) || upper < base))
				|| (base < min || base > max))
			LOGGER.warn("Bad {} value {} for {}. Best use values between {} and {}", valueName, value, name, min, max);
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
