
package team.bytephoria.bytechat.chat;

public final class ChatMuteState {

    private boolean chatMuted;

    public ChatMuteState() {
        this.chatMuted = false;
    }

    public void muteChatGlobally() {
        this.chatMuted = true;
    }

    public void unmuteChatGlobally() {
        this.chatMuted = false;
    }

    public boolean isChatMuted() {
        return this.chatMuted;
    }
}
