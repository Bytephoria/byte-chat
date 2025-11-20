package team.bytephoria.bytechat.serializer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class PlainComponentSerializerAdapter implements ComponentSerializerAdapter {

    @Override
    public @NotNull Component deserialize(final @NotNull String input) {
        return PlainTextComponentSerializer.plainText().deserialize(input);
    }

    @Override
    public @NotNull String serialize(final @NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
