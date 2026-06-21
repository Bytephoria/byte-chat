package team.bytephoria.bytechat.serializer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

public interface ComponentSerializerAdapter extends ComponentSerializer<Component, Component, String> {

    /**
     * Escapes the given input so that any markup it contains renders literally
     * instead of being parsed.
     * <p>
     * Used to neutralize player formatting when {@code text-formatting} is disabled:
     * the player's tags are shown verbatim rather than consumed. The default
     * implementation returns the input unchanged (suitable for serializers, such as
     * plain text, that do not interpret markup).
     *
     * @param input the raw input to escape
     * @return the escaped input
     */
    default @NotNull String escape(final @NotNull String input) {
        return input;
    }
}
