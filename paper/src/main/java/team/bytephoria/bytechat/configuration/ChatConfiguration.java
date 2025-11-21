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

        @Setting("mentions")
        private Mentions mentions = new Mentions();

        public boolean enabled() {
            return enabled;
        }

        public String defaultFormat() {
            return defaultFormat;
        }

        public boolean textFormatting() {
            return this.textFormatting;
        }

        public Mentions mentions() {
            return mentions;
        }

    }

    @ConfigSerializable
    public static final class Mentions {

        @Setting("enabled")
        private boolean enabled = true;

        @Setting("trigger-char")
        private char triggerChar = '@';

        @Setting("format")
        private String format = "<yellow>{tag}</yellow>";

        @Setting("allow-self-mention")
        private boolean allowSelfMention = true;

        @Setting("self-mention-sound")
        private boolean selfMentionSound = true;

        @Setting("sound")
        private Sound sound = new Sound();

        public boolean enabled() {
            return this.enabled;
        }

        public char triggerChar() {
            return this.triggerChar;
        }

        public String format() {
            return this.format;
        }

        public boolean allowSelfMention() {
            return this.allowSelfMention;
        }

        public boolean selfMentionSound() {
            return this.selfMentionSound;
        }

        public Sound sound() {
            return this.sound;
        }
    }

    @ConfigSerializable
    public static final class Sound {

        @Setting("key")
        private String key = "entity.player.levelup";

        @Setting("volume")
        private float volume = 1.0f;

        @Setting("pitch")
        private float pitch = 1.0f;

        public String key() {
            return this.key;
        }

        public float volume() {
            return this.volume;
        }

        public float pitch() {
            return this.pitch;
        }

    }

}
