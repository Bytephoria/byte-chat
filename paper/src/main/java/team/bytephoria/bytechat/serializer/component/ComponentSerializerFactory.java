package team.bytephoria.bytechat.serializer.component;

import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.Locale;

public final class ComponentSerializerFactory {

    private ComponentSerializerFactory() {
        throw new NonInstantiableClassException();
    }

    public static @NotNull ComponentSerializerAdapter create(final @NotNull String format) {
        return switch (format.trim().toUpperCase(Locale.ROOT)) {
            case "LEGACY_AMPERSAND" -> new LegacyAmpersandComponentSerializerAdapter();
            case "MINI_MESSAGE" -> new MiniMessageComponentSerializerAdapter();
            default -> new PlainComponentSerializerAdapter();
        };
    }
}
