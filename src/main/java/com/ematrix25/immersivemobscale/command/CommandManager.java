package com.ematrix25.immersivemobscale.command;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

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

		registerSubCommand("help", _ -> CommandActions.commandsToString(COMMANDS));

		registerSubCommand("reload", source -> CommandActions.reload(source.getServer()),
				_ -> Main.MOD_NAME + " configuration reloaded");
		registerSubCommand("debug", _ -> CommandActions.toggleDebug(), _ -> CommandActions.getDebug());

		registerSubCommand("version", _ -> CommandActions.getVersion());
		registerSubCommand("stats", _ -> CommandActions.getStats());
		registerSubCommand("list", _ -> CommandActions.getList());
		registerSubCommand("list", "category", CommandActions::getList);
	}

	/**
	 * Registers a command under the root command without argument and action to
	 * execute.
	 * 
	 * @param cmdName
	 * @param msgProvider
	 */
	private static void registerSubCommand(String cmdName, Function<CommandSourceStack, String> msgProvider) {
		registerSubCommand(cmdName, null, context -> (CommandSourceStack) context.getSource(), _ -> {
		}, msgProvider);
	}

	/**
	 * Register a command under the root command without an action to execute.
	 * 
	 * @param cmdName
	 * @param argumentName
	 * @param msgProvider
	 */
	private static void registerSubCommand(String cmdName, String argumentName, Function<String, String> msgProvider) {

		registerSubCommand(cmdName, argumentName, context -> StringArgumentType.getString(context, argumentName), _ -> {
		}, msgProvider);
	}

	/**
	 * Register a command under the root command without an argument.
	 *
	 * @param cmdName
	 * @param cmdAction
	 * @param msgProvider
	 */
	private static void registerSubCommand(String cmdName, Consumer<CommandSourceStack> cmdAction,
			Function<CommandSourceStack, String> msgProvider) {

		registerSubCommand(cmdName, null, context -> context.getSource(), cmdAction, msgProvider);
	}

	/**
	 * Registers a command under the root command.
	 *
	 * @param cmdName
	 * @param argumentName
	 * @param cmdAction
	 * @param msgProvider
	 */
	private static <T> void registerSubCommand(String cmdName, String argumentName,
			Function<CommandContext<CommandSourceStack>, T> valueProvider, Consumer<T> cmdAction,
			Function<T, String> msgProvider) {
		if (!cmdName.equals("help"))
			COMMANDS.add(cmdName);
		if (Main.debugLogging)
			LOGGER.info("Commands: {}", COMMANDS);

		var command = Commands.literal(cmdName);
		var executor = (Command<CommandSourceStack>) context -> executeCommand(context, valueProvider, cmdAction,
				msgProvider);

		if (argumentName == null)
			command.executes(executor);
		else
			command.then(Commands.argument(argumentName, StringArgumentType.word()).executes(executor));

		rootCommand.then(command);
	}

	/**
	 * Generates the executor for commands.
	 * 
	 * @param <T>
	 * @param context
	 * @param valueProvider
	 * @param cmdAction
	 * @param msgProvider
	 * @return integer result
	 */
	private static <T> int executeCommand(CommandContext<CommandSourceStack> context,
			Function<CommandContext<CommandSourceStack>, T> valueProvider, Consumer<T> cmdAction,
			Function<T, String> msgProvider) {
		T value = valueProvider.apply(context);
		cmdAction.accept(value);
		context.getSource().sendSuccess(() -> Component.literal(msgProvider.apply(value)), false);
		return 1;
	}
}