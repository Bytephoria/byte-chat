package team.bytephoria.bytechat.serializer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class LegacyAmpersandComponentSerializerAdapter implements ComponentSerializerAdapter {

    @Override
    public @NotNull Component deserialize(final @NotNull String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

    @Override
    public @NotNull String serialize(final @NotNull Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

}
