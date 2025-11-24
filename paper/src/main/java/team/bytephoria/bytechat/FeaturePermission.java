package team.bytephoria.bytechat;

import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

/**
 * Centralized permission constants for all ByteChat features and commands.
 * <p>
 * This class acts as a static reference point to prevent hardcoded
 * permission strings throughout the codebase.
 */
public final class FeaturePermission {

    private FeaturePermission() {
        throw new NonInstantiableClassException();
    }

    public static final class Command {

        /** Base permission for all ByteChat commands. */
        public static final String MAIN = "bytechat.command";

        /** Permission required to execute the /bytechat reload command. */
        public static final String RELOAD = "bytechat.command.reload";

        /** Permission required to execute the /bytechat mute */
        public static final String MUTE = "bytechat.command.mute";

    }

    public static final class Feature {

        /** Permission to bypass global mute state. */
        public static final String BYPASS_MUTE = "bytechat.bypass.mute";

    }

    public static final class Format {

        /** Allows players to use color and formatting codes in chat messages. */
        public static final String COLOR = "bytechat.format.color";

        /** Allows players to mention other players using @name. */
        public static final String MENTION = "bytechat.format.mention";

        /** Allows players to use any supported chat tag. (Wildcard) */
        public static final String TAG = "bytechat.format.tag.*";

        /** Allows players to use [inv] tag. */
        public static final String TAG_INVENTORY = "bytechat.format.tag.inventory";

        /** Allows players to use [armor] tag. */
        public static final String TAG_ARMOR = "bytechat.format.tag.armor";

        /** Allows players to use [item] tag. */
        public static final String TAG_ITEM = "bytechat.format.tag.item";
    }


}
