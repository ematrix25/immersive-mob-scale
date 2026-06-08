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
	private static final String WHITESPACE = "\\s+", EMPTY = "";

	private static LiteralArgumentBuilder<CommandSourceStack> rootCommand;

	/**
	 * Registers the root command.
	 */
	public static void register() {
		rootCommand = Commands.literal(MOD_CMD_NAME);

		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(rootCommand));

		registerCommand("help", _ -> CommandActions.commandsToString(COMMANDS));

		registerCommand("reload", source -> CommandActions.reload(source.getServer()),
				_ -> Main.MOD_NAME + " configuration reloaded");
		registerCommand("debug", _ -> CommandActions.toggleDebug(), _ -> CommandActions.getDebug());

		registerCommand("version", _ -> CommandActions.getVersion());
		registerCommand("stats", _ -> CommandActions.getStats());
		registerCommand("list", _ -> CommandActions.getList());
		registerCommand("list", "category", CommandActions::getList);
		registerCommand("info category", "category", CommandActions::getCategoryInfo);
		registerCommand("info entity", "entity", CommandActions::getEntityInfo);
	}

	/**
	 * Registers a command under the root command without argument and action to
	 * execute.
	 * 
	 * @param cmdPath
	 * @param msgProvider
	 */
	private static void registerCommand(String cmdPath, Function<CommandSourceStack, String> msgProvider) {
		registerCommand(cmdPath, null, context -> (CommandSourceStack) context.getSource(), _ -> {
		}, msgProvider);
	}

	/**
	 * Register a command under the root command without an action to execute.
	 * 
	 * @param cmdPath
	 * @param argumentName
	 * @param msgProvider
	 */
	private static void registerCommand(String cmdPath, String argumentName, Function<String, String> msgProvider) {

		registerCommand(cmdPath, argumentName, context -> StringArgumentType.getString(context, argumentName), _ -> {
		}, msgProvider);
	}

	/**
	 * Register a command under the root command without an argument.
	 *
	 * @param cmdPath
	 * @param cmdAction
	 * @param msgProvider
	 */
	private static void registerCommand(String cmdPath, Consumer<CommandSourceStack> cmdAction,
			Function<CommandSourceStack, String> msgProvider) {

		registerCommand(cmdPath, null, context -> context.getSource(), cmdAction, msgProvider);
	}

	/**
	 * Registers a command under the root command.
	 *
	 * @param cmdPath
	 * @param argumentName
	 * @param cmdAction
	 * @param msgProvider
	 */
	private static <T> void registerCommand(String cmdPath, String argumentName,
			Function<CommandContext<CommandSourceStack>, T> valueProvider, Consumer<T> cmdAction,
			Function<T, String> msgProvider) {
		if (!cmdPath.equals("help"))
			COMMANDS.add(cmdPath);
		if (Main.debugLogging)
			LOGGER.info("Commands: {}", COMMANDS);

		String[] cmdNames = cmdPath.trim().split(WHITESPACE);
		int last = cmdNames.length - 1;
		var command = Commands.literal(cmdNames[last]);

		var executor = (Command<CommandSourceStack>) context -> executeCommand(context, valueProvider, cmdAction,
				msgProvider);

		if (argumentName == null)
			command.executes(executor);
		else
			command.then(Commands
					.argument(argumentName.replaceAll(WHITESPACE, EMPTY).toLowerCase(), StringArgumentType.greedyString())
					.executes(executor));

		for (int i = last - 1; i >= 0; i--)
			command = Commands.literal(cmdNames[i]).then(command);
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