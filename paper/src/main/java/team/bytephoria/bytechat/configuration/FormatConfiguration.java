package team.bytephoria.bytechat.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single chat format file ({@code plugins/ByteChat/formats/<id>.yml}).
 * <p>
 * The format id is derived from the file name (minus the {@code .yml} extension),
 * so each file declares one format at its root: a {@code permission}, a
 * {@code priority}, and an ordered map of {@code components}.
 */
@ConfigSerializable
public final class FormatConfiguration {

    @Setting("permission")
    private String permission = "";

    @Setting("priority")
    private int priority = 0;

    @Setting("components")
    private Map<String, ChatComponent> components = new LinkedHashMap<>();

    public String permission() {
        return this.permission;
    }

    public int priority() {
        return this.priority;
    }

    public Map<String, ChatComponent> components() {
        return this.components;
    }

    @ConfigSerializable
    public static final class ChatComponent {

        @Setting("text")
        private String text = "";

        @Setting("hover")
        private List<String> hover = List.of();

        @Setting("click")
        private ClickAction click = new ClickAction();

        public String text() {
            return this.text;
        }

        public List<String> hover() {
            return this.hover;
        }

        public ClickAction click() {
            return this.click;
        }
    }

    @ConfigSerializable
    public static final class ClickAction {

        @Setting("action")
        private String action = "";

        @Setting("value")
        private String value = "";

        public String action() {
            return this.action;
        }

        public String value() {
            return this.value;
        }
    }

}
