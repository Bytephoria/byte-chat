package team.bytephoria.bytechat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.PaperPlugin;

import java.util.List;

public final class ChatCommand extends BukkitCommand {

    private final PaperPlugin paperPlugin;
    public ChatCommand(final @NotNull PaperPlugin paperPlugin) {
        super("bytechat", "Main command for ByteChat", "/<command>", List.of("bchat", "b-chat"));
        this.paperPlugin = paperPlugin;
    }

    @Override
    public boolean execute(
            final @NotNull CommandSender commandSender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (!commandSender.hasPermission(FeaturePermission.Command.MAIN)) {
            commandSender.sendMessage(Component.text("You don't have permission to execute this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission(FeaturePermission.Command.RELOAD)) {
                commandSender.sendMessage(Component.text("You don't have permission to execute this command!", NamedTextColor.RED));
                return true;
            }

            this.paperPlugin.reload();
            commandSender.sendMessage(Component.text("ByteChat has been successfully reloaded!", NamedTextColor.GREEN));
            return true;
        }

        commandSender.sendMessage(Component.text("Invalid command usage. Try '/bytechat reload'.", NamedTextColor.RED));
        return true;
    }
}
