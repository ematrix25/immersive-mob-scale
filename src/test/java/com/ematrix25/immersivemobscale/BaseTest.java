package com.ematrix25.immersivemobscale;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;

import com.ematrix25.immersivemobscale.config.ConfigManager;
import com.ematrix25.immersivemobscale.config.ConfigManager.ConfigType;

/**
 * Provides shared setup and cleanup utilities for tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {
	protected static final Path TEST_CONFIG_DIR = Path.of("build/resources/test/");

	/**
	 * Initializes test configuration environment.
	 */
	public void initialize() {
		ConfigManager.initialize(TEST_CONFIG_DIR);
	}

	/**
	 * Initializes test loading configuration environment.
	 */
	public void initialize(ConfigType... configTypes) {
		ConfigManager.initialize(TEST_CONFIG_DIR);
		for (ConfigType configType : configTypes) {
			ConfigManager.loadConfig(configType);
		}
	}

	/**
	 * Cleans generated test configuration files.
	 * 
	 * @throws IOException
	 */
	@AfterAll
	public void terminate() throws IOException {
		if (Files.exists(TEST_CONFIG_DIR))
			Files.walk(TEST_CONFIG_DIR).sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			});
	}
}