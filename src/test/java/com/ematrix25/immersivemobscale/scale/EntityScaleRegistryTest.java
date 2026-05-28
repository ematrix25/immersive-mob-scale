package com.ematrix25.immersivemobscale.scale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.ematrix25.immersivemobscale.BaseTest;
import com.ematrix25.immersivemobscale.config.ConfigManager.ConfigType;

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
		super.initialize(ConfigType.CATEGORIES);
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
	}
}
