package team.bytephoria.bytechat.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public final class FormatConfiguration {

    @Setting("formats")
    private Map<String, ChatFormat> formats = new LinkedHashMap<>();

    public Map<String, ChatFormat> formats() {
        return this.formats;
    }

    @ConfigSerializable
    public static final class ChatFormat {

        @Setting("permission")
        private String permission = "";

        @Setting("priority")
        private int priority = 0;

        @Setting("elements")
        private Map<String, ChatElement> elements = new LinkedHashMap<>();

        public String permission() {
            return permission;
        }

        public int priority() {
            return this.priority;
        }

        public Map<String, ChatElement> elements() {
            return elements;
        }
    }

    @ConfigSerializable
    public static final class ChatElement {

        @Setting("text")
        private String text = "";

        @Setting("hover")
        private List<String> hover = List.of();

        @Setting("click")
        private ClickAction click = new ClickAction();

        public String text() {
            return text;
        }

        public List<String> hover() {
            return hover;
        }

        public ClickAction click() {
            return click;
        }
    }

    @ConfigSerializable
    public static final class ClickAction {

        @Setting("action")
        private String action = "";

        @Setting("value")
        private String value = "";

        public String action() {
            return action;
        }

        public String value() {
            return value;
        }
    }
}