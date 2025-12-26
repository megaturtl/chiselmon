package cc.turtl.cobbleaid.mixin;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.feature.hud.spawntracker.SpawnTrackerFeature;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void cobbleaid$captureSpawnResponses(Component chatComponent, MessageSignature signature, GuiMessageTag guiMessageTag, CallbackInfo ci) {
        if (CobbleAid.isDisabled()) return;
        if (signature != null) {
            return;
        }

        if (SpawnTrackerFeature.captureChat(chatComponent)) {
            ci.cancel();
        }
    }
}
