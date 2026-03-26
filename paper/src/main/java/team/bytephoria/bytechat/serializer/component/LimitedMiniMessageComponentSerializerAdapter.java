package team.bytephoria.bytechat.serializer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

public final class LimitedMiniMessageComponentSerializerAdapter implements ComponentSerializerAdapter {

    private final MiniMessage LIMITED_MINI_MESSAGE_SERIALIZER = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.rainbow())
                    .resolver(StandardTags.gradient())
                    .build()
            )
            .build();

    @Override
    public @NotNull Component deserialize(final @NotNull String input) {
        return LIMITED_MINI_MESSAGE_SERIALIZER.deserialize(input);
    }

    @Override
    public @NotNull String serialize(final @NotNull Component component) {
        return LIMITED_MINI_MESSAGE_SERIALIZER.serialize(component);
    }
}
