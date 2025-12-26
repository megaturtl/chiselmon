package cc.turtl. cobbleaid.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SpawnAlertConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnShiny = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnLegendary = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnUltraBeast = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnParadox = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnCustomList = false;

    @ConfigEntry.Gui.Tooltip
    public List<String> customPokemonList = new ArrayList<>(Arrays.asList(
            "Ditto",
            "Eevee",
            "Rotom"));

    @ConfigEntry.Gui.Tooltip
    public AlertDisplayMode displayMode = AlertDisplayMode.CHAT_AND_SOUND;

    @ConfigEntry.Gui.Tooltip
    public String alertSound = "entity.experience_orb.pickup";

    @ConfigEntry. Gui.Tooltip
    public float soundVolume = 1.0f;

    @ConfigEntry.Gui.Tooltip
    public float soundPitch = 1.0f;

    public enum AlertDisplayMode {
        CHAT_ONLY,
        SOUND_ONLY,
        CHAT_AND_SOUND,
        TOAST_NOTIFICATION
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        if (soundVolume < 0.0f) {
            soundVolume = 0.0f;
        }
        if (soundVolume > 1.0f) {
            soundVolume = 1.0f;
        }
    }
}