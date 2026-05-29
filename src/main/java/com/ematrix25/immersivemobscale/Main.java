package com.ematrix25.immersivemobscale;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;

/**
 * Initializes the Mob Scale fabric modification.
 */
public class Main implements ModInitializer {
	public static final String MOD_ID = "immersivemobscale";
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
	public static final boolean DEBUG_LOGGING = true;
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/**
	 * Initializes systems during Fabric startup.
	 */
	@Override
	public void onInitialize() {
		ConfigManager.initialize();
		ConfigManager.loadConfig(ConfigManager.ConfigType.CATEGORIES);
		EntityScaleRegistry.initialize();
		ServerEntityEvents.ENTITY_LOAD.register((entity, _) -> {
			if (entity instanceof LivingEntity livingEntity)
				EntityScaleHandler.apply(livingEntity);
		});
		LOGGER.info("Mod system initialized");
	}
}