package com.ematrix25.immersivemobscale;

import java.nio.file.Path;

import com.ematrix25.immersivemobscale.config.ConfigManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Initializes the Mob Scale fabric modification.
 * 
 * @author ematrix25
 */
public class Main implements ModInitializer {
	public static final String MOD_ID = "immersivemobscale";
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("immersivemobscale");

	/**
	 * Initializes systems during Fabric startup.
	 */
	@Override
	public void onInitialize() {
		ConfigManager.initialize();
	}
}