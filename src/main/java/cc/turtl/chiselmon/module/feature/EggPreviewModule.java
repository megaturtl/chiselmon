package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.eggpreview.EggPreviewFeature;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggCache;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;
import cc.turtl.chiselmon.module.ChiselmonModule;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class EggPreviewModule implements ChiselmonModule {
    private final EggPreviewFeature feature = new EggPreviewFeature();

    public EggPreviewFeature feature() {
        return feature;
    }

    @Override
    public String id() {
        return "egg-preview";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }

    public void onGuiInit(com.cobblemon.mod.common.client.gui.pc.PCGUI pcGUI) {
        cc.turtl.chiselmon.feature.eggpreview.EggPreviewManager.onGuiInit(pcGUI);
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

    private boolean isEnabled() {
        return !Chiselmon.isDisabled() && Chiselmon.services().config().get().eggPreview.enabled;
    }
}
