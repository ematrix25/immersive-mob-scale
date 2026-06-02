package com.ematrix25.immersivemobscale;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.command.CommandManager;
import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.scale.EntityScaleHandler;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Initializes the Mob Scale fabric modification.
 */
public class Main implements ModInitializer {
	public static final String MOD_ID = "immersivemobscale", MOD_NAME = "Immersive Mob Scale";
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	public static boolean debugLogging = true;

	/**
	 * Initializes systems during Fabric startup.
	 */
	@Override
	public void onInitialize() {
		ConfigManager.initialize();
		ConfigManager.loadConfig(ConfigManager.ConfigType.CATEGORIES);
		EntityScaleRegistry.initialize();
		CommandManager.register();

		CommandManager.registerSubCommand("reload", source -> reload(source.getServer()),
				_ -> MOD_NAME + " configuration reloaded");
		CommandManager.registerSubCommand("debug", _ -> toggleDebug(),
				_ -> "Debug logging " + (debugLogging ? "enabled" : "disabled"));
		CommandManager.registerSubCommand("version", _ -> MOD_NAME + " " + getVersion());

		ServerEntityEvents.ENTITY_LOAD.register((entity, _) -> {
			if (entity instanceof LivingEntity livingEntity)
				EntityScaleHandler.apply(livingEntity);
		});
		LOGGER.info("Mod system initialized");
	}

	/**
	 * Reloads configuration files and reapplies categories to loaded entities.
	 * 
	 * @param server
	 */
	public static void reload(MinecraftServer server) {
		ConfigManager.loadConfig(ConfigManager.ConfigType.CATEGORIES);
		EntityScaleRegistry.initialize();

		for (ServerLevel level : server.getAllLevels())
			for (Entity entity : level.getAllEntities())
				if (entity instanceof LivingEntity livingEntity)
					EntityScaleHandler.apply(livingEntity);
		LOGGER.info("Reloaded configuration and reapplied entity categories");
	}

	/**
	 * Toggle to hide or show debug messages.
	 */
	public static void toggleDebug() {
		debugLogging = !debugLogging;
	}

	/**
	 * Retrieves the current version.
	 */
	public static String getVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
	}
}