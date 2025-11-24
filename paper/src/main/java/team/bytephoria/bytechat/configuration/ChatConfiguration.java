
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

        @Setting("tags")
        private Tags tags = new Tags();

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

        public Tags tags() {
            return tags;
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
    public static final class Tags {

        @Setting("enabled")
        private boolean enabled = true;

        @Setting("max-tags-per-message")
        private int maxTagsPerMessage = 1;

        @Setting("item")
        private ItemTag item = new ItemTag();

        @Setting("inventory")
        private InventoryTag inventory = new InventoryTag();

        @Setting("armor")
        private ArmorTag armor = new ArmorTag();

        public boolean enabled() {
            return this.enabled;
        }

        public int maxTagsPerMessage() {
            return this.maxTagsPerMessage;
        }

        public ItemTag item() {
            return this.item;
        }

        public InventoryTag inventory() {
            return this.inventory;
        }

        public ArmorTag armor() {
            return this.armor;
        }

        @ConfigSerializable
        public static final class ItemTag {

            @Setting("enabled")
            private boolean enabled = true;

            @Setting("empty-hand-text")
            private String emptyHandText = "[Empty Hand]";

            @Setting("empty-hand-color")
            private String emptyHandColor = "GRAY";

            public boolean enabled() {
                return this.enabled;
            }

            public String emptyHandText() {
                return this.emptyHandText;
            }

            public String emptyHandColor() {
                return this.emptyHandColor;
            }
        }

        @ConfigSerializable
        public static final class InventoryTag {

            @Setting("enabled")
            private boolean enabled = true;

            @Setting("display-text")
            private String displayText = "[Inventory]";

            @Setting("display-color")
            private String displayColor = "AQUA";

            @Setting("preview-title")
            private String previewTitle = "Inventory of {player_name}";

            @Setting("max-clicks")
            private int maxClicks = 10;

            @Setting("expiration-seconds")
            private int expirationSeconds = 70;

            public boolean enabled() {
                return this.enabled;
            }

            public String displayText() {
                return this.displayText;
            }

            public String displayColor() {
                return this.displayColor;
            }

            public String previewTitle() {
                return this.previewTitle;
            }

            public int maxClicks() {
                return this.maxClicks;
            }

            public int expirationSeconds() {
                return this.expirationSeconds;
            }
        }

        @ConfigSerializable
        public static final class ArmorTag {

            @Setting("enabled")
            private boolean enabled = true;

            @Setting("display-text")
            private String displayText = "[Armor Inventory]";

            @Setting("display-color")
            private String displayColor = "AQUA";

            @Setting("preview-title")
            private String previewTitle = "Armor of {player_name}";

            @Setting("max-clicks")
            private int maxClicks = 10;

            @Setting("expiration-seconds")
            private int expirationSeconds = 3600;

            public boolean enabled() {
                return this.enabled;
            }

            public String displayText() {
                return this.displayText;
            }

            public String displayColor() {
                return this.displayColor;
            }

            public String previewTitle() {
                return this.previewTitle;
            }

            public int maxClicks() {
                return this.maxClicks;
            }

            public int expirationSeconds() {
                return this.expirationSeconds;
            }
        }
    }

    @ConfigSerializable
    public static final class Inventory {

        @Setting("enabled")
        private boolean enabled = true;

        @Setting("display-text")
        private String displayText = "[Inventory]";

        @Setting("display-color")
        private String displayColor = "AQUA";

        @Setting("preview-title")
        private String previewTitle = "Inventory of {player_name}";

        @Setting("max-clicks")
        private int maxClicks = 10;

        @Setting("expiration-seconds")
        private int expirationSeconds = 70;

        public boolean enabled() {
            return this.enabled;
        }

        public String displayText() {
            return this.displayText;
        }

        public String displayColor() {
            return this.displayColor;
        }

        public String previewTitle() {
            return this.previewTitle;
        }

        public int maxClicks() {
            return this.maxClicks;
        }

        public int expirationSeconds() {
            return this.expirationSeconds;
        }
    }

    @ConfigSerializable
    public static final class Armor {

        @Setting("enabled")
        private boolean enabled = true;

        @Setting("display-text")
        private String displayText = "[Armor Inventory]";

        @Setting("display-color")
        private String displayColor = "AQUA";

        @Setting("preview-title")
        private String previewTitle = "Armor of {player_name}";

        @Setting("max-clicks")
        private int maxClicks = 10;

        @Setting("expiration-seconds")
        private int expirationSeconds = 3600;

        public boolean enabled() {
            return this.enabled;
        }

        public String displayText() {
            return this.displayText;
        }

        public String displayColor() {
            return this.displayColor;
        }

        public String previewTitle() {
            return this.previewTitle;
        }

        public int maxClicks() {
            return this.maxClicks;
        }

        public int expirationSeconds() {
            return this.expirationSeconds;
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
