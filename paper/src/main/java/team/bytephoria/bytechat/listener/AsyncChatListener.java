package team.bytephoria.bytechat.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.PaperPlugin;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.chat.renderer.ViewerUnawareChatRenderer;
import team.bytephoria.bytechat.manager.ChatManager;

public final class AsyncChatListener implements Listener {

    private final PaperPlugin paperPlugin;
    public AsyncChatListener(final @NotNull PaperPlugin paperPlugin) {
        this.paperPlugin = paperPlugin;
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(final @NotNull AsyncChatEvent asyncChatEvent) {
        final Player player = asyncChatEvent.getPlayer();

        if (this.paperPlugin.muteService().isChatMuted() && !player.hasPermission(FeaturePermission.Feature.BYPASS_MUTE)) {
            asyncChatEvent.setCancelled(true);
            player.sendMessage(Component.text("The chat is currently muted!", NamedTextColor.RED));
            return;
        }

        final ChatManager chatManager = this.paperPlugin.chatManager();
        final ChatFormat chatFormat = chatManager.search(player);

        if (chatFormat != null) {
            final ViewerUnawareChatRenderer viewerUnawareChatRenderer = new ViewerUnawareChatRenderer(
                    chatFormat,
                    asyncChatEvent.signedMessage(),
                    this.paperPlugin.chatConfiguration(),
                    this.paperPlugin.mentionResolverService(),
                    this.paperPlugin.tagResolverService(),
                    this.paperPlugin.serializerAdapter()
            );

            asyncChatEvent.renderer(ChatRenderer.viewerUnaware(viewerUnawareChatRenderer));
        }
    }

}