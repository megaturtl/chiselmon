package cc.turtl.chiselmon.api.event;

import net.minecraft.network.chat.Component;

/**
 * Game/system message.
 */
public class MessageReceivedEvent {
    private Component message;

    public MessageReceivedEvent(Component message) {
        this.message = message;
    }

    public Component getMessage() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }
}