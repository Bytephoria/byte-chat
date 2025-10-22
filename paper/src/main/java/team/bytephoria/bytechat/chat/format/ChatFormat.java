package team.bytephoria.bytechat.chat.format;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.Identifiable;
import team.bytephoria.bytechat.api.Prioritizable;
import team.bytephoria.bytechat.chat.element.ChatElement;

import java.util.Collection;
import java.util.Map;

public class ChatFormat implements Identifiable, Prioritizable {

    private final String id;
    private final String permission;
    private final int priority;

    private final Map<String, ChatElement> chatElements;

    public ChatFormat(
            final @NotNull String id,
            final @Nullable String permission,
            final int priority,
            final @NotNull Map<String, ChatElement> chatElements
    ) {
        this.id = id;
        this.permission = permission;
        this.priority = priority;
        this.chatElements = chatElements;
    }

    @Override
    public String id() {
        return this.id;
    }

    public String permission() {
        return this.permission;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public Collection<ChatElement> allElements() {
        return this.chatElements.values();
    }

    public Map<String, ChatElement> chatElements() {
        return this.chatElements;
    }

}
