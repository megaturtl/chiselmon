package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.feature.eggpreview.EggPreviewManager;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggCache;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;
import cc.turtl.chiselmon.module.ChiselmonModule;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class EggPreviewModule implements ChiselmonModule {
    @Override
    public String id() {
        return "egg-preview";
    }

    @Override
    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
    }

    public void onGuiInit(PCGUI pcGUI) {
        EggPreviewManager.onGuiInit(pcGUI);
    }

    public Pokemon onGetPokemon(Pokemon pokemon) {
        if (!isEnabled()) {
            return pokemon;
        }
        if (NeoDaycareEggDummy.isEgg(pokemon)) {
            return NeoDaycareEggCache.getDummyOrOriginal(pokemon);
        }
        return pokemon;
    }

    public PCPosition onGetPosition(ClientPC pc, Pokemon pokemon) {
        if (pokemon instanceof NeoDaycareEggDummy dummy) {
            return pc.getPosition(dummy.getOriginalEggPokemon());
        }
        return null;
    }

    private void onClientTickEnd(Minecraft client) {
        if (!isEnabled()) {
            return;
        }
        ChiselmonConfig config = Chiselmon.services().config().get();
        if (!config.eggPreview.attemptHatchSync || client.player == null
                || !(client.screen instanceof PCGUI pcGUI)) {
            return;
        }
        EggPreviewManager.tick(pcGUI);
    }

    private boolean isEnabled() {
        return !Chiselmon.isDisabled() && Chiselmon.services().config().get().eggPreview.enabled;
    }
}
