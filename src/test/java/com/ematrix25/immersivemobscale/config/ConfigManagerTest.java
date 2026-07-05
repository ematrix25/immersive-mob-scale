package com.ematrix25.immersivemobscale.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.ematrix25.immersivemobscale.BaseTest;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleCategory;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;

/**
 * Tests loading, creation and validation behavior of ConfigManager code.
 */
@DisplayName("ConfigManager Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigManagerTest extends BaseTest {
	/**
	 * Initializes test configuration environment.
	 */
	@BeforeAll
	public void initialize() {
		super.initialize();
	}

	/**
	 * Tests creation of default configuration files.
	 * 
	 * @throws IOException
	 */
	@Test
	@DisplayName("Creation of default config files")
	public void shouldCreateDefaultConfigFiles() throws IOException {
		String fileName;
		Path file;

		for (ConfigType configType : ConfigType.values()) {
			fileName = configType.getFileName();
			file = TEST_CONFIG_DIR.resolve(fileName);

			assertTrue(Files.exists(file));
		}
	}

	/**
	 * Tests loading configuration files.
	 */
	@Test
	@DisplayName("Loading of config files")
	public void shouldLoadConfigFiles() {
		for (ConfigType configType : ConfigType.values())
			ConfigManager.loadConfig(configType);

		assertEquals(ConfigType.values().length, ConfigManager.getLoadedConfigCount());
	}

	/**
	 * Tests loading categories from configuration file.
	 */
	@Test
	@DisplayName("Loading of categories config")
	public void shouldLoadCategoriesConfig() {
		ConfigManager.loadConfig(ConfigType.CATEGORIES);
		Map<String, EntityScaleCategory> categories = ConfigManager.getConfig(ConfigType.CATEGORIES);

		assertNotNull(categories);
		assertFalse(categories.isEmpty());
		assertTrue(categories.containsKey("tiny_insects"));

		EntityScaleCategory category = categories.get("tiny_insects");

		assertEquals(0.10f, category.scale());
		assertEquals(1.30f, category.speed());
		assertEquals("minecraft:silverfish", category.entities().iterator().next());
	}

	/**
	 * Tests loading entities from configuration file.
	 */
	@Test
	@DisplayName("Loading of entities config")
	public void shouldLoadEntitiesConfig() {
		ConfigManager.loadConfig(ConfigType.ENTITIES);
		Map<String, EntityScaleData> entities = ConfigManager.getConfig(ConfigType.ENTITIES);

		assertNotNull(entities);
		assertFalse(entities.isEmpty());
		assertTrue(entities.containsKey("minecraft:ender_dragon"));

		EntityScaleData data = entities.get("minecraft:ender_dragon");

		assertEquals(1.30f, data.scale());
		assertEquals(0.80f, data.speed());
		assertEquals(4.00f, data.health());
		assertEquals(2.50f, data.attack());
	}
}