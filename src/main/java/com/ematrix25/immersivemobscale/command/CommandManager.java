package com.ematrix25.immersivemobscale.command;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.EntityScaleRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

/**
 * Manages registration of commands.
 */
public class CommandManager {
	private static final String MOD_CMD_NAME = "ims";
	private static final Set<String> COMMANDS = new LinkedHashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
	private static final String WHITESPACE = "\\s+";

	private static LiteralArgumentBuilder<CommandSourceStack> rootCommand;

	/**
	 * Registers the root command.
	 */
	public static void register() {
		rootCommand = Commands.literal(MOD_CMD_NAME);

		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(rootCommand));

		// Simple commands without arguments and actions.
		register("help", _ -> CommandActions.commandsToString(COMMANDS));
		register("version", _ -> CommandActions.getVersion());
		register("stats", _ -> CommandActions.getStats());
		register("list", _ -> CommandActions.getList());

		// Commands without arguments and with actions.
		register("reload", ctx -> {
			CommandActions.reload(ctx.getSource().getServer());
			return Main.MOD_NAME + " configuration reloaded";
		}, PermissionLevel.GAMEMASTERS);
		register("debug", _ -> {
			CommandActions.toggleDebug();
			return CommandActions.getDebug();
		}, PermissionLevel.GAMEMASTERS);

		// Commands with arguments and actions.
		register("list category", "category", EntityScaleRegistry::getCategoryNames,
				ctx -> CommandActions.getList(ctx.getArgument("category", String.class)));
		register("info category", "category", EntityScaleRegistry::getCategoryNames,
				ctx -> CommandActions.getCategoryInfo(ctx.getArgument("category", String.class)));
		register("info entity", "entity", EntityScaleRegistry::getEntityNames, ctx -> CommandActions
				.getEntityInfo(ctx.getSource().getServer(), ctx.getArgument("entity", String.class)));
	}

	/**
	 * Register commands without arguments, actions and permission
	 * 
	 * @param path
	 * @param action
	 */
	private static void register(String path, Function<CommandContext<CommandSourceStack>, String> action) {
		register(path, null, null, action, null);
	}

	/**
	 * Register commands without arguments and actions, but with permission
	 * 
	 * @param path
	 * @param action
	 * @param permissionLevel
	 */
	private static void register(String path, Function<CommandContext<CommandSourceStack>, String> action,
			PermissionLevel permissionLevel) {
		register(path, null, null, action, permissionLevel);
	}

	/**
	 * Register commands with arguments and actions, but without permission
	 * 
	 * @param path
	 * @param argument
	 * @param suggestions
	 * @param action
	 */
	private static void register(String path, String argument, Supplier<Set<String>> suggestions,
			Function<CommandContext<CommandSourceStack>, String> action) {
		register(path, argument, suggestions, action, null);
	}

	/**
	 * Main method where command registration really happens
	 * 
	 * @param path
	 * @param argument
	 * @param suggestions
	 * @param action
	 * @param permissionLevel
	 */
	private static void register(String path, String argument, Supplier<Set<String>> suggestions,
			Function<CommandContext<CommandSourceStack>, String> action, PermissionLevel permissionLevel) {

		if (!path.equals("help"))
			COMMANDS.add(path + (permissionLevel != null ? " (Requires " + permissionLevel + ")" : ""));
		if (Main.debugLogging)
			LOGGER.info("Commands: {}", COMMANDS);

		String[] parts = path.trim().split(WHITESPACE);
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(parts[parts.length - 1]);

		if (permissionLevel != null)
			command.requires(
					source -> source.permissions().hasPermission(new Permission.HasCommandLevel(permissionLevel)));

		Command<CommandSourceStack> executor = ctx -> {
			String message = action.apply(ctx);
			ctx.getSource().sendSuccess(() -> Component.literal(message), false);
			return 1;
		};

		if (argument != null) {
			var arg = Commands.argument(argument.toLowerCase(), StringArgumentType.greedyString());

			if (suggestions != null) {
				arg.suggests((_, builder) -> {
					suggestions.get().forEach(builder::suggest);
					return builder.buildFuture();
				});
			}

			arg.executes(executor);
			command.then(arg);
		} else {
			command.executes(executor);
		}

		// Build sub commands hierarchy
		for (int i = parts.length - 2; i >= 0; i--)
			command = Commands.literal(parts[i]).then(command);

		rootCommand.then(command);
	}
}