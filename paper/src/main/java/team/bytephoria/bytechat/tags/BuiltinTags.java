package team.bytephoria.bytechat.tags;

import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.api.tag.TagRegistry;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.tags.builtin.ArmorTag;
import team.bytephoria.bytechat.tags.builtin.EnderChestTag;
import team.bytephoria.bytechat.tags.builtin.InventoryTag;
import team.bytephoria.bytechat.tags.builtin.ItemTag;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

/**
 * Registers ByteChat's built-in tags ({@code [item]}, {@code [inv]},
 * {@code [armor]}, {@code [ec]}/{@code [enderchest]}) into a {@link TagRegistry}.
 * Only tags whose configuration is {@code enabled} are registered, so disabled tags
 * simply fall through to literal text.
 */
public final class BuiltinTags {

    private BuiltinTags() {
        throw new NonInstantiableClassException();
    }

    /**
     * Registers every enabled built-in tag described by the given configuration.
     *
     * @param registry      the registry to populate
     * @param configuration the active chat configuration
     */
    public static void registerDefaults(
            final @NotNull TagRegistry registry,
            final @NotNull ComponentSerializerAdapter serializerAdapter,
            final @NotNull ChatConfiguration configuration
    ) {
        final ChatConfiguration.Tags tags = configuration.chat().tags();

        if (tags.item().enabled()) {
            registry.register(new ItemTag(tags.item()));
        }

        if (tags.inventory().enabled()) {
            registry.register(new InventoryTag(serializerAdapter, tags.inventory()));
        }

        if (tags.armor().enabled()) {
            registry.register(new ArmorTag(serializerAdapter, tags.armor()));
        }

        if (tags.enderChest().enabled()) {
            registry.register(new EnderChestTag("ec", serializerAdapter, tags.enderChest()));
            registry.register(new EnderChestTag("enderchest", serializerAdapter, tags.enderChest()));
        }
    }
}
