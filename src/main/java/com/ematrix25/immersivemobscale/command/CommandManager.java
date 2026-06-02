package com.ematrix25.immersivemobscale.command;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Manages registration of commands.
 */
public class CommandManager {
	private static final String MOD_CMD_NAME = "ims";
	private static final Set<String> COMMANDS = new LinkedHashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	private static LiteralArgumentBuilder<CommandSourceStack> rootCommand;

	/**
	 * Registers the root command.
	 */
	public static void register() {
		rootCommand = Commands.literal(MOD_CMD_NAME);

		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(rootCommand));

		registerSubCommand("help", _ -> Main.MOD_NAME + " Commands: " + commandsToString());
	}

	/**
	 * Registers a command under the root command without an action to execute.
	 * 
	 * @param cmdName
	 * @param msgProvider
	 */
	public static void registerSubCommand(String cmdName, Function<CommandSourceStack, String> msgProvider) {
		registerSubCommand(cmdName, _ -> {
		}, msgProvider);
	}

	/**
	 * Registers a command under the root command.
	 *
	 * @param cmdName
	 * @param cmdAction
	 * @param msgProvider
	 */
	public static void registerSubCommand(String cmdName, Consumer<CommandSourceStack> cmdAction,
			Function<CommandSourceStack, String> msgProvider) {
		if (!cmdName.equals("help"))
			COMMANDS.add(cmdName);
		LOGGER.info("Commands: {}", COMMANDS);
		rootCommand.then(Commands.literal(cmdName).executes(context -> {
			cmdAction.accept(context.getSource());
			context.getSource().sendSuccess(() -> Component.literal(msgProvider.apply(context.getSource())), false);
			return 1;
		}));
	}

	/**
	 * Returns the list of registered commands to String.
	 * 
	 * @return commands
	 */
	public static String commandsToString() {
		return String.join(", ", COMMANDS);
	}
}