package com.ematrix25.immersivemobscale.scale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.ematrix25.immersivemobscale.BaseTest;
import com.ematrix25.immersivemobscale.config.ConfigType;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleCategory;
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

		assertNotNull(category);
		assertEquals(0.10f, category.scale());
		assertEquals(1.30f, category.speed());
	}

	/**
	 * Tests registration of entities.
	 */
	@Test
	@DisplayName("Registry should map entity ids")
	public void shouldRegisterEntities() {
		EntityScaleData data = EntityScaleRegistry.getEntityScaleData(Identifier.parse("minecraft:ender_dragon"));

		assertNotNull(data);
		assertEquals(1.30f, data.scale());
		assertEquals(0.80f, data.speed());
		assertEquals(4.00f, data.health());
		assertEquals(2.50f, data.attack());
	}
	
	/**
	 * Tests fallback from category to entity data.
	 */
	@Test
	@DisplayName("Registry should fallback to category data")
	public void shouldFallbackToCategoryData() {
		EntityScaleData data = EntityScaleRegistry.getEntityScaleData(Identifier.parse("minecraft:silverfish"));

		assertNotNull(data);
		assertEquals(0.10f, data.scale());
		assertEquals(1.30f, data.speed());
		assertEquals(0.10f, data.health());
		assertEquals(0.10f, data.attack());
	}
}
