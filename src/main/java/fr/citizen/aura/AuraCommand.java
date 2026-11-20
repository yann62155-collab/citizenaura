package fr.citizen.aura;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;

/**
 * /aura donner <joueur> <couleur> [style]   (un seul joueur à la fois)
 * /aura retirer <joueur>
 * /aura vider
 * /aura liste
 * /aura couleurs
 */
public final class AuraCommand {
    private AuraCommand() {}

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("aura")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("donner")
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .then(Commands.argument("couleur", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(AuraColors.NAMED.keySet(), b))
                                        .executes(c -> give(c, AuraStyle.ORBITE))
                                        .then(Commands.argument("style", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(AuraStyle.values()).map(AuraStyle::id), b))
                                                .executes(c -> {
                                                    String s = StringArgumentType.getString(c, "style");
                                                    AuraStyle st = AuraStyle.parse(s);
                                                    if (st == null) {
                                                        c.getSource().sendFailure(Component.literal(
                                                                "Style inconnu : " + s + " (orbite, spirale, halo)"));
                                                        return 0;
                                                    }
                                                    return give(c, st);
                                                })))))
                .then(Commands.literal("retirer")
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .executes(AuraCommand::remove)))
                .then(Commands.literal("vider").executes(AuraCommand::clear))
                .then(Commands.literal("liste").executes(AuraCommand::list))
                .then(Commands.literal("couleurs").executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal("Couleurs : " + String.join(", ", AuraColors.NAMED.keySet())
                            + " — ou un code hexa, ex : ff8800").withStyle(ChatFormatting.GOLD), false);
                    return 1;
                })));
    }

    private static int give(CommandContext<CommandSourceStack> c, AuraStyle style) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "joueur");
        String raw = StringArgumentType.getString(c, "couleur");
        Integer color = AuraColors.parse(raw);
        if (color == null) {
            c.getSource().sendFailure(Component.literal("Couleur inconnue : " + raw + ". Tape /aura couleurs."));
            return 0;
        }
        AuraData data = AuraData.get(c.getSource().getServer());
        String name = target.getGameProfile().getName();
        data.set(target.getUUID(), new AuraData.Entry(name, color, style));
        target.sendSystemMessage(Component.literal("✦ Une aura " + AuraColors.describe(color) + " vous entoure.")
                .withStyle(ChatFormatting.GOLD));
        c.getSource().sendSuccess(() -> Component.literal("Aura " + AuraColors.describe(color) + " (" + style.id()
                + ") donnée à " + name + " uniquement.").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "joueur");
        AuraData data = AuraData.get(c.getSource().getServer());
        boolean had = data.remove(target.getUUID());
        String name = target.getGameProfile().getName();
        c.getSource().sendSuccess(() -> Component.literal(had ? "Aura retirée à " + name + "."
                : name + " n'avait pas d'aura.").withStyle(ChatFormatting.GOLD), false);
        return had ? 1 : 0;
    }

    private static int clear(CommandContext<CommandSourceStack> c) {
        AuraData data = AuraData.get(c.getSource().getServer());
        int n = data.clear();
        c.getSource().sendSuccess(() -> Component.literal("Toutes les auras ont été retirées (" + n + ").")
                .withStyle(ChatFormatting.GOLD), false);
        return n;
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        AuraData data = AuraData.get(c.getSource().getServer());
        if (data.all().isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("Aucune aura active.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        StringBuilder sb = new StringBuilder("Auras :");
        data.all().values().forEach(e -> sb.append("\n - ").append(e.name()).append(" : ")
                .append(AuraColors.describe(e.color())).append(" (").append(e.style().id()).append(")"));
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GOLD), false);
        return data.all().size();
    }
}
