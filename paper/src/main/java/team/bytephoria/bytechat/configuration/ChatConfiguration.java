package team.bytephoria.bytechat.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ChatConfiguration {

    @Setting("settings")
    private Settings settings = new Settings();

    @Setting("chat")
    private Chat chat = new Chat();

    public Settings settings() {
        return settings;
    }

    public Chat chat() {
        return chat;
    }

    @ConfigSerializable
    public static final class Settings {

        @Setting("serializer")
        private String serializer = "MINI_MESSAGE";

        // Getters
        public String serializer() {
            return serializer;
        }

    }

    @ConfigSerializable
    public static final class Chat {

        @Setting("enabled")
        private boolean enabled = true;

        @Setting("default-format")
        private String defaultFormat = "default";

        @Setting("text-formatting")
        private boolean textFormatting = true;

        public boolean enabled() {
            return enabled;
        }

        public String defaultFormat() {
            return defaultFormat;
        }

        public boolean textFormatting() {
            return this.textFormatting;
        }

    }

}
