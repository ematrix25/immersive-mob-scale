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
import com.ematrix25.immersivemobscale.config.ConfigManager.ConfigType;
import com.ematrix25.immersivemobscale.scale.EntityScaleCategory;

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
		String fileName = ConfigType.CATEGORIES.getFileName();
		Path file = TEST_CONFIG_DIR.resolve(fileName);

		assertTrue(Files.exists(file));
	}

	/**
	 * Tests loading categories from configuration file.
	 */
	@Test
	@DisplayName("Loading of config files")
	public void shouldLoadCategoriesConfig() {
		ConfigManager.loadConfig(ConfigType.CATEGORIES);
		Map<String, EntityScaleCategory> categories = ConfigManager.getConfig(ConfigType.CATEGORIES);

		assertNotNull(categories);
		assertFalse(categories.isEmpty());
		assertTrue(categories.containsKey("tiny_insects"));

		EntityScaleCategory insects = categories.get("tiny_insects");

		assertEquals(0.10f, insects.scale());
		assertEquals(1.30f, insects.speed());
		assertEquals("minecraft:silverfish", insects.entities().get(0));
	}
}