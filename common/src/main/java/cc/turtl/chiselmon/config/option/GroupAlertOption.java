package cc.turtl.chiselmon.config.option;

import cc.turtl.chiselmon.system.alert.AlertSounds;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A compound config option representing all settings for a single alert group.
 * This is designed to work with dynamic group registration.
 */
public class GroupAlertOption implements ConfigOption<GroupAlertOption.GroupAlertData> {

    private final String groupId;
    private final String displayName;
    private GroupAlertData value;
    private final GroupAlertData defaultValue;
    @Nullable
    private Consumer<GroupAlertData> changeCallback;

    public GroupAlertOption(String groupId, String displayName) {
        this.groupId = groupId;
        this.displayName = displayName;
        this.defaultValue = new GroupAlertData();
        this.value = new GroupAlertData();
    }

    public String getGroupId() {
        return groupId;
    }

    // Convenience accessors for the data fields
    public boolean isEnabled() {
        return value.enabled;
    }

    public void setEnabled(boolean enabled) {
        value.enabled = enabled;
        notifyChange();
    }

    public boolean shouldPlaySound() {
        return value.playSound;
    }

    public void setPlaySound(boolean playSound) {
        value.playSound = playSound;
        notifyChange();
    }

    public AlertSounds getSound() {
        return value.sound;
    }

    public void setSound(AlertSounds sound) {
        value.sound = sound;
        notifyChange();
    }

    public int getVolume() {
        return value.volume;
    }

    public void setVolume(int volume) {
        value.volume = Math.max(0, Math.min(100, volume));
        notifyChange();
    }

    public int getPitch() {
        return value.pitch;
    }

    public void setPitch(int pitch) {
        value.pitch = Math.max(0, Math.min(200, pitch));
        notifyChange();
    }

    public boolean shouldSendChatMessage() {
        return value.sendChatMessage;
    }

    public void setSendChatMessage(boolean sendChatMessage) {
        value.sendChatMessage = sendChatMessage;
        notifyChange();
    }

    public boolean shouldHighlightEntity() {
        return value.highlightEntity;
    }

    public void setHighlightEntity(boolean highlightEntity) {
        value.highlightEntity = highlightEntity;
        notifyChange();
    }

    public int getHighlightColor() {
        return value.highlightColor;
    }

    public void setHighlightColor(int highlightColor) {
        value.highlightColor = highlightColor;
        notifyChange();
    }

    private void notifyChange() {
        if (changeCallback != null) {
            changeCallback.accept(value);
        }
    }

    @Override
    public String getName() {
        return groupId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @Nullable
    public String getComment() {
        return "Alert settings for " + displayName;
    }

    @Override
    public GroupAlertData getValue() {
        return value;
    }

    @Override
    public GroupAlertData getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(GroupAlertData value) {
        this.value = value;
        notifyChange();
    }

    @Override
    public void resetToDefault() {
        this.value = new GroupAlertData();
        notifyChange();
    }

    @Override
    public boolean isModified() {
        return !value.equals(defaultValue);
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", value.enabled);
        obj.addProperty("playSound", value.playSound);
        obj.addProperty("sound", value.sound.name());
        obj.addProperty("volume", value.volume);
        obj.addProperty("pitch", value.pitch);
        obj.addProperty("sendChatMessage", value.sendChatMessage);
        obj.addProperty("highlightEntity", value.highlightEntity);
        obj.addProperty("highlightColor", String.format("#%06X", value.highlightColor & 0x00FFFFFF));
        return obj;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (!element.isJsonObject()) return;
        JsonObject obj = element.getAsJsonObject();

        if (obj.has("enabled")) value.enabled = obj.get("enabled").getAsBoolean();
        if (obj.has("playSound")) value.playSound = obj.get("playSound").getAsBoolean();
        if (obj.has("sound")) {
            try {
                value.sound = AlertSounds.valueOf(obj.get("sound").getAsString());
            } catch (IllegalArgumentException ignored) {}
        }
        if (obj.has("volume")) value.volume = Math.max(0, Math.min(100, obj.get("volume").getAsInt()));
        if (obj.has("pitch")) value.pitch = Math.max(0, Math.min(200, obj.get("pitch").getAsInt()));
        if (obj.has("sendChatMessage")) value.sendChatMessage = obj.get("sendChatMessage").getAsBoolean();
        if (obj.has("highlightEntity")) value.highlightEntity = obj.get("highlightEntity").getAsBoolean();
        if (obj.has("highlightColor")) {
            String hex = obj.get("highlightColor").getAsString();
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            try {
                value.highlightColor = Integer.parseInt(cleaned, 16);
            } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public void setChangeCallback(@Nullable Consumer<GroupAlertData> callback) {
        this.changeCallback = callback;
    }

    /**
     * Data class holding all settings for an alert group.
     */
    public static class GroupAlertData {
        public boolean enabled = true;
        public boolean playSound = true;
        public AlertSounds sound = AlertSounds.EXP_ORB;
        public int volume = 100;
        public int pitch = 100;
        public boolean sendChatMessage = true;
        public boolean highlightEntity = true;
        public int highlightColor = 0xFFFF00; // Yellow by default

        public GroupAlertData() {}

        public GroupAlertData(GroupAlertData other) {
            this.enabled = other.enabled;
            this.playSound = other.playSound;
            this.sound = other.sound;
            this.volume = other.volume;
            this.pitch = other.pitch;
            this.sendChatMessage = other.sendChatMessage;
            this.highlightEntity = other.highlightEntity;
            this.highlightColor = other.highlightColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupAlertData that = (GroupAlertData) o;
            return enabled == that.enabled &&
                    playSound == that.playSound &&
                    volume == that.volume &&
                    pitch == that.pitch &&
                    sendChatMessage == that.sendChatMessage &&
                    highlightEntity == that.highlightEntity &&
                    highlightColor == that.highlightColor &&
                    sound == that.sound;
        }
    }
}
