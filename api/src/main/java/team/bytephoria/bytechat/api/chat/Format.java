package team.bytephoria.bytechat.api.chat;

import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.util.Identifiable;
import team.bytephoria.bytechat.api.util.Prioritizable;

/**
 * Read-only view of a chat format, as exposed to API consumers.
 * <p>
 * The id is the format file name, the permission gates who may use it, and the
 * priority decides which format wins when several apply to a player.
 */
public interface Format extends Identifiable, Prioritizable {

    /**
     * The permission required to use this format, or {@code null} / empty when the
     * format is available to everyone.
     *
     * @return the required permission, or {@code null}
     */
    @Nullable String permission();
}
