
package team.bytephoria.bytechat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.PaperPlugin;

import java.util.Collections;
import java.util.List;

public final class ChatCommand extends BukkitCommand {

    private final PaperPlugin paperPlugin;
    
    public ChatCommand(final @NotNull PaperPlugin paperPlugin) {
        super("bytechat", "Main command for ByteChat", "/<command>", Collections.emptyList());
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

        if (args.length == 0) {
            commandSender.sendMessage(Component.text("Invalid command usage. Try '/bytechat reload' or '/bytechat mute'.", NamedTextColor.RED));
            return true;
        }

        final String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                if (!commandSender.hasPermission(FeaturePermission.Command.RELOAD)) {
                    commandSender.sendMessage(Component.text("You don't have permission to execute this command!", NamedTextColor.RED));
                    return true;
                }

                this.paperPlugin.reload();
                commandSender.sendMessage(Component.text("ByteChat has been successfully reloaded!", NamedTextColor.GREEN));
                return true;
            }

            case "mute" -> {
                if (!commandSender.hasPermission(FeaturePermission.Command.MUTE)) {
                    commandSender.sendMessage(Component.text("You don't have permission to execute this command!", NamedTextColor.RED));
                    return true;
                }

                if (this.paperPlugin.muteService().isChatMuted()) {
                    this.paperPlugin.muteService().unmuteChatGlobally();
                    commandSender.sendMessage(Component.text("The chat has been unmuted!", NamedTextColor.GREEN));
                } else {
                    this.paperPlugin.muteService().muteChatGlobally();
                    commandSender.sendMessage(Component.text("The chat has been muted globally!", NamedTextColor.GREEN));
                }
                return true;
            }

            default -> {
                commandSender.sendMessage(Component.text("Invalid command usage. Try '/bytechat reload' or '/bytechat mute'.", NamedTextColor.RED));
                return true;
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(
            final @NotNull CommandSender sender,
            final @NotNull String alias,
            final @NotNull String @NotNull [] args
    ) {
        if (args.length == 1) {
            return List.of("reload", "mute");
        }
        return Collections.emptyList();
    }
}
