package carpet.commands;

import carpet.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.BlockInfo;
import carpet.utils.EntityInfo;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import carpet.fakes.ServerLightingProviderInterface;
import net.minecraft.text.BaseText;
import carpet.fakes.ThreadedAnvilChunkStorageInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

import static net.minecraft.command.CommandSource.suggestMatching;

public class LightCommand {
    private static String[] lightTypes = new String[]{"block","sky"};
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("light").
        requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandLight))
            .then(CommandManager.literal("batch")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .then(argument("batch size", integer(1,1000000)).executes(LightCommand::setBatchSizeCommand))
                    .executes(LightCommand::getBatchSizeCommand))
                .then(argument("batch size", integer(1,1000000)))
                .executes(LightCommand::getBatchSizeCommand))
            .then(CommandManager.literal("status")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::statusCommand))
                .executes(LightCommand::statusCommand))
            .then(CommandManager.literal("pause")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::pauseCommand))
                .executes(LightCommand::pauseCommand))
            .then(CommandManager.literal("stop")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::stopCommand))
                .executes(LightCommand::stopCommand))
            .then(CommandManager.literal("run")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::runCommand))
                .executes(LightCommand::runCommand))
            .then(CommandManager.literal("log")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::logCommand))
                .executes(LightCommand::logCommand))
            .then(CommandManager.literal("clear")
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(LightCommand::clearCommand))
                .executes(LightCommand::clearCommand))
            .then(CommandManager.literal("enable")
                .then(argument("type", StringArgumentType.word()).suggests((c, b) -> suggestMatching(lightTypes,b))
                    .then(argument("dimension", DimensionArgumentType.dimension())
                        .executes(LightCommand::enableCommand))
                    .executes(LightCommand::enableCommand)))
            .then(CommandManager.literal("disable")
                .then(argument("type", StringArgumentType.word()).suggests((c, b) -> suggestMatching(lightTypes,b))
                    .then(argument("dimension", DimensionArgumentType.dimension())
                        .executes(LightCommand::disableCommand))
                    .executes(LightCommand::disableCommand)))
            .then(CommandManager.literal("relight")
                .then(CommandManager.argument("from", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
                    return relightChunks(commandContext, ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "from"));
                }).then(CommandManager.argument("to", ColumnPosArgumentType.columnPos()).executes((commandContext) -> {
                    return relightChunks(commandContext, ColumnPosArgumentType.getColumnPos(commandContext, "from"), ColumnPosArgumentType.getColumnPos(commandContext, "to"));
                }))));
 

        dispatcher.register(command);
    }

  
    
    private static ServerWorld getWorld(CommandContext<ServerCommandSource> ctx) {
        try {
            return DimensionArgumentType.getDimensionArgument(ctx, "dimension");
        } catch (Exception e) {
            ServerWorld world = ctx.getSource().getWorld();
            if (world == null) world = ctx.getSource().getMinecraftServer().getOverworld();
            return world;
        }
    }

    private static int relightChunks(CommandContext<ServerCommandSource> ctx, ColumnPos from, ColumnPos to) {
        ServerWorld world = getWorld(ctx);
        int minx = Math.min(from.x, to.x) >> 4;
        int minz = Math.min(from.z, to.z) >> 4;
        int maxx = Math.max(from.x, to.x) >> 4;
        int maxz = Math.max(from.z, to.z) >> 4;

        ThreadedAnvilChunkStorageInterface chunkStorage = (ThreadedAnvilChunkStorageInterface) world.getChunkManager().threadedAnvilChunkStorage;
        for (int x = minx; x <= maxx; x++) {
            for (int z = minz; z <= maxz; z++) {
                chunkStorage.relightChunk(new ChunkPos(x,z));
            }
        }
        long count = (maxx - minx + 1L) * (maxz - minz + 1L);
        Messenger.m(ctx.getSource(), new Object[] { "w Relit " + count + " chunks"});
       

       
        return 1;
    }
    public static int setBatchSizeCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        int batchSize = getInteger(ctx, "batch size");
        world.getChunkManager().getLightingProvider().setTaskBatchSize(batchSize);
        Messenger.m(ctx.getSource(), new Object[] { "w Batch Size for " + world.toString() + " set to " + batchSize});
        return 1;
    }

    public static int getBatchSizeCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        int batchSize = ((ServerLightingProviderInterface) world.getChunkManager().getLightingProvider()).getTaskBatchSize();
        Messenger.m(ctx.getSource(), new Object[] { "w Batch Size for " + world.toString() + " is " + batchSize});
        return 1;
    }
    
    public static int statusCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int pauseCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }
    
    public static int stopCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int runCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int logCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int clearCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int enableCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

    public static int disableCommand(CommandContext<ServerCommandSource> ctx)
    {
        ServerWorld world = getWorld(ctx);
        return 1;
    }

}

