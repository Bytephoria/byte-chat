package team.bytephoria.bytechat.serializer.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import team.bytephoria.bytechat.api.Provider;

import java.util.*;
import java.util.function.Supplier;

public final class ComponentSerializerProvider implements Provider<ComponentSerializerAdapter> {

    private static final Map<String, Supplier<ComponentSerializerAdapter>> ENGINES = new HashMap<>();

    static {
        register("MINI_MESSAGE", MiniMessageComponentSerializerAdapter::new);
        register("LEGACY_AMPERSAND", LegacyAmpersandComponentSerializerAdapter::new);
    }

    public static void register(
            final @NotNull String name,
            final @NotNull Supplier<ComponentSerializerAdapter> engineSupplier
    ) {
        ENGINES.put(name.toUpperCase(), engineSupplier);
    }

    private final ComponentSerializerAdapter adapter;

    public ComponentSerializerProvider(final @NotNull String engineName) {
        final String key = engineName.toUpperCase();
        final Supplier<ComponentSerializerAdapter> supplier = Optional
                .ofNullable(ENGINES.get(key))
                .orElseGet(() -> LegacyAmpersandComponentSerializerAdapter::new);

        this.adapter = supplier.get();
    }

    @Override
    public @NotNull ComponentSerializerAdapter get() {
        return this.adapter;
    }

    public static @NotNull @UnmodifiableView Set<String> availableEngines() {
        return Collections.unmodifiableSet(ENGINES.keySet());
    }
}