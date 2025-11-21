package cc.turtl.cobbleaid.features.pc.gui;

import com.cobblemon.mod.common.CobblemonSounds;

import cc.turtl.cobbleaid.api.gui.CustomButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;


public class PcButton extends CustomButton {

    public PcButton(int x, int y, int width, int height, Component message, OnPress clickAction) {
        super(x, y, width, height, message, clickAction);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }
}
