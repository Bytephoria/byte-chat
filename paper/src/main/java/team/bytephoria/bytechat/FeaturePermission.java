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

    }

    public static final class Format {

        /** Permission that allows players to use color and formatting codes in chat messages. */
        public static final String COLOR = "bytechat.format.color";

        /** Permission that allows players to mention other players using @name. */
        public static final String MENTION = "bytechat.format.mention";

    }

}
