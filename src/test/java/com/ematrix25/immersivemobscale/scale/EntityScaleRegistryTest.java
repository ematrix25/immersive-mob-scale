package com.ematrix25.immersivemobscale.scale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.ematrix25.immersivemobscale.BaseTest;
import com.ematrix25.immersivemobscale.config.ConfigType;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleCategory;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleConfig;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;

import net.minecraft.resources.Identifier;

/**
 * Tests loading behavior of EntityScaleRegistry code.
 */
@DisplayName("EntityScaleRegistry Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityScaleRegistryTest extends BaseTest {
	/**
	 * Initializes test registration environment.
	 */
	@BeforeAll
	public void initialize() {
		super.initialize(ConfigType.CATEGORIES, ConfigType.ENTITIES);
		EntityScaleRegistry.initialize();
	}

	/**
	 * Tests registration of entities to categories.
	 */
	@Test
	@DisplayName("Registry should map entity ids to categories")
	public void shouldRegisterEntityCategories() {
		EntityScaleCategory category = EntityScaleRegistry.getCategory(Identifier.parse("minecraft:silverfish"));
		var config = category.config();

		assertNotNull(category);
		assertTrue(config.scale().matches(0.10f));
		assertTrue(config.speed().matches(1.30f));
	}

	/**
	 * Tests registration of entities.
	 */
	@Test
	@DisplayName("Registry should map entity ids")
	public void shouldRegisterEntities() {
		EntityScaleConfig config = EntityScaleRegistry.getEntityScaleConfig(Identifier.parse("minecraft:ender_dragon"));

		assertNotNull(config);
		assertTrue(config.scale().matches(1.30f));
		assertTrue(config.speed().matches(0.80f));
		assertTrue(config.health().matches(4.00f));
		assertTrue(config.attack().matches(2.50f));
	}

	/**
	 * Tests fallback from category to entity data.
	 */
	@Test
	@DisplayName("Registry should fallback to category data")
	public void shouldFallbackToCategoryData() {
		EntityScaleConfig config = EntityScaleRegistry.getEntityScaleConfig(Identifier.parse("minecraft:silverfish"));

		assertNotNull(config);

		EntityScaleData data = config.generate();

		assertTrue(config.scale().matches(data.scale()));
		assertTrue(config.speed().matches(data.speed()));
		assertTrue(config.scale().matches(data.health()));
		assertTrue(config.scale().matches(data.attack()));
	}
}
