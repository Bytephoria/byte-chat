package team.bytephoria.bytechat.chat.format;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import team.bytephoria.bytechat.api.chat.Format;
import team.bytephoria.bytechat.chat.component.ChatComponent;

import java.util.Collection;
import java.util.Map;

public record ChatFormat(
        String id,
        String permission,
        int priority,
        Map<String, ChatComponent> components
) implements Format {

    public ChatFormat(
            final @NotNull String id,
            final @Nullable String permission,
            final int priority,
            final @NotNull Map<String, ChatComponent> components
    ) {
        this.id = id;
        this.permission = permission;
        this.priority = priority;
        this.components = components;
    }

    @Contract(pure = true)
    public @NonNull Collection<ChatComponent> allComponents() {
        return this.components.values();
    }

}
