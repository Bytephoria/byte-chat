package team.bytephoria.bytechat;

import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

public final class Constants {

    private Constants() {
        throw new NonInstantiableClassException();
    }

    public static final byte MINECRAFT_MIN_USERNAME_LENGTH = 3;
    public static final byte MINECRAFT_MAX_USERNAME_LENGTH = 16;

}
