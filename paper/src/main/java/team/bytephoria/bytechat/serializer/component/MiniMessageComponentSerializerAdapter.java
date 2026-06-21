package team.bytephoria.bytechat.serializer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public final class MiniMessageComponentSerializerAdapter implements ComponentSerializerAdapter {

    @Override
    public @NotNull Component deserialize(final @NotNull String input) {
        return MiniMessage.miniMessage().deserialize(input);
    }

    @Override
    public @NotNull String serialize(final @NotNull Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    @Override
    public @NotNull String escape(final @NotNull String input) {
        return MiniMessage.miniMessage().escapeTags(input);
    }
}
