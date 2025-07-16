package me.maartin0.CarpetSurvivalBots;

import carpet.commands.PlayerCommand;
import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.GameMode;

import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class BotCommand extends PlayerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("bot")
            .then(literal("stop").executes(manipulation(EntityPlayerActionPack::stopAll)))
            .then(makeActionCommand("use", ActionType.USE))
            .then(makeActionCommand("jump", ActionType.JUMP))
            .then(makeActionCommand("attack", ActionType.ATTACK))
            .then(makeActionCommand("drop", ActionType.DROP_ITEM))
            .then(makeDropCommand("drop", false))
            .then(makeActionCommand("dropStack", ActionType.DROP_STACK))
            .then(makeDropCommand("dropStack", true))
            .then(makeActionCommand("swapHands", ActionType.SWAP_HANDS))
            .then(literal("hotbar")
                    .then(argument("slot", IntegerArgumentType.integer(1, 9))
                            .executes(c -> manipulate(c, ap -> ap.setSlot(IntegerArgumentType.getInteger(c, "slot"))))))
            .then(literal("mount").executes(manipulation(ap -> ap.mount(true)))
                    .then(literal("anything").executes(manipulation(ap -> ap.mount(false)))))
            .then(literal("dismount").executes(manipulation(EntityPlayerActionPack::dismount)))
            .then(literal("sneak").executes(manipulation(ap -> ap.setSneaking(true))))
            .then(literal("unsneak").executes(manipulation(ap -> ap.setSneaking(false))))
            .then(literal("sprint").executes(manipulation(ap -> ap.setSprinting(true))))
            .then(literal("unsprint").executes(manipulation(ap -> ap.setSprinting(false))))
            .then(literal("look")
                    .then(literal("north").executes(manipulation(ap -> ap.look(Direction.NORTH))))
                    .then(literal("south").executes(manipulation(ap -> ap.look(Direction.SOUTH))))
                    .then(literal("east").executes(manipulation(ap -> ap.look(Direction.EAST))))
                    .then(literal("west").executes(manipulation(ap -> ap.look(Direction.WEST))))
                    .then(literal("up").executes(manipulation(ap -> ap.look(Direction.UP))))
                    .then(literal("down").executes(manipulation(ap -> ap.look(Direction.DOWN))))
                    .then(literal("at").then(argument("position", Vec3ArgumentType.vec3())
                            .executes(c -> manipulate(c, ap -> ap.lookAt(Vec3ArgumentType.getVec3(c, "position"))))))
                    .then(argument("direction", RotationArgumentType.rotation())
                            .executes(c -> manipulate(c, ap -> ap.look(RotationArgumentType.getRotation(c, "direction").toAbsoluteRotation(c.getSource())))))
            ).then(literal("turn")
                    .then(literal("left").executes(manipulation(ap -> ap.turn(-90, 0))))
                    .then(literal("right").executes(manipulation(ap -> ap.turn(90, 0))))
                    .then(literal("back").executes(manipulation(ap -> ap.turn(180, 0))))
                    .then(argument("rotation", RotationArgumentType.rotation())
                            .executes(c -> manipulate(c, ap -> ap.turn(RotationArgumentType.getRotation(c, "rotation").toAbsoluteRotation(c.getSource())))))
            ).then(literal("move").executes(manipulation(EntityPlayerActionPack::stopMovement))
                    .then(literal("forward").executes(manipulation(ap -> ap.setForward(1))))
                    .then(literal("backward").executes(manipulation(ap -> ap.setForward(-1))))
                    .then(literal("left").executes(manipulation(ap -> ap.setStrafing(1))))
                    .then(literal("right").executes(manipulation(ap -> ap.setStrafing(-1))))
            ).then(literal("spawn").executes(BotCommand::spawn));
        dispatcher.register(command);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeActionCommand(String actionName, ActionType type) {
        return literal(actionName)
                .executes(manipulation(ap -> ap.start(type, EntityPlayerActionPack.Action.once())))
                .then(literal("once").executes(manipulation(ap -> ap.start(type, EntityPlayerActionPack.Action.once()))))
                .then(literal("continuous").executes(manipulation(ap -> ap.start(type, EntityPlayerActionPack.Action.continuous()))))
                .then(literal("interval").then(argument("ticks", IntegerArgumentType.integer(1))
                        .executes(c -> manipulate(c, ap -> ap.start(type, EntityPlayerActionPack.Action.interval(IntegerArgumentType.getInteger(c, "ticks")))))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeDropCommand(String actionName, boolean dropAll) {
        return literal(actionName)
                .then(literal("all").executes(manipulation(ap -> ap.drop(-2, dropAll))))
                .then(literal("mainhand").executes(manipulation(ap -> ap.drop(-1, dropAll))))
                .then(literal("offhand").executes(manipulation(ap -> ap.drop(40, dropAll))))
                .then(argument("slot", IntegerArgumentType.integer(0, 40)).
                        executes(c -> manipulate(c, ap -> ap.drop(IntegerArgumentType.getInteger(c, "slot"), dropAll))));
    }

    private static String getPlayerName(CommandContext<ServerCommandSource> context) {
        String name = context.getSource().getName();
        if (name.length() >= 16) return "#" + name.substring(1);
        else return "#" + name;
    }

    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = getPlayerName(context);
        MinecraftServer server = context.getSource().getServer();
        return server.getPlayerManager().getPlayer(playerName);
    }

    private static String formatLocation(ServerPlayerEntity entity) {
        return String.format("%s %s %s in %s", entity.getBlockX(), entity.getBlockY(), entity.getBlockZ(), entity.getWorld().getRegistryKey().getValue().toString());
    }

    private static void reply(CommandContext<ServerCommandSource> context, String message) {
        context.getSource().sendFeedback(() -> Text.of(message), false);
    }

    private static int spawn(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity existing = getPlayer(context);
        if (existing != null) {
            reply(context, String.format("Your bot is already spawned! It's at %s", formatLocation(existing)));
            return 0;
        }
        ServerCommandSource source = context.getSource();
        Vec2f facing = source.getRotation();
        EntityPlayerMPFake.createFake(
                getPlayerName(context),
                source.getServer(),
                source.getPosition(),
                facing.y, facing.x,
                source.getWorld().getRegistryKey(),
                GameMode.SURVIVAL,
                false
        );
        return 1;
    }

    private static int manipulate(CommandContext<ServerCommandSource> context, Consumer<EntityPlayerActionPack> action) {
        ServerPlayerEntity player = getPlayer(context);
        if (player == null) {
            reply(context, "Can't get player from context");
            return 0;
        }
        action.accept(((ServerPlayerInterface) player).getActionPack());
        return 1;
    }

    private static Command<ServerCommandSource> manipulation(Consumer<EntityPlayerActionPack> action) {
        return c -> manipulate(c, action);
    }
}
