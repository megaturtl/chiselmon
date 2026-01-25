package cc.turtl.chiselmon.feature.eggpreview;

import java.util.List;
import java.util.UUID;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;

public class EggPreviewManager {
    private static int tickCounter = 0;
    private static final int REFRESH_INTERVAL = 100; // every 5 seconds

    public static void tick(PCGUI pcGUI) {
        tickCounter++;
        if (tickCounter >= REFRESH_INTERVAL) {
            tickCounter = 0;
            refreshPartyEggs(pcGUI);
        }
    }

    // Super crude method to bounce the eggs in and out of the pc 'invisibly'
    public static void refreshPartyEggs(PCGUI pcGUI) {
        ClientParty party = pcGUI.getParty();

        // getSlots() doesn't return our dummies so we check if they are an egg
        List<Pokemon> eggsInParty = party.getSlots().stream().filter(PokemonPredicates.IS_NEODAYCARE_EGG).toList();

        if (eggsInParty.isEmpty())
            return;

        ClientPC pc = pcGUI.getPc();

        Pokemon anchorPokemon = findFirstValidAnchor(pc);
        PCPosition anchorPos = pc.getPosition(anchorPokemon);

        if (anchorPokemon == null) {
            Chiselmon.getLogger().warn("Aborting egg swap refresh: No valid pokemon found for anchor.");
            return;
        }

        UUID anchorUuid = anchorPokemon.getUuid();
        Chiselmon.getLogger().debug("Refreshing {} eggs using anchor {}", eggsInParty.size(), anchorUuid);

        for (Pokemon egg : eggsInParty) {

            UUID eggUuid = egg.getUuid();
            PartyPosition partyPos = party.getPosition(egg);

            new SwapPCPartyPokemonPacket(eggUuid, partyPos, anchorUuid, anchorPos).sendToServer();
            new SwapPCPartyPokemonPacket(anchorUuid, partyPos, eggUuid, anchorPos).sendToServer();

            Chiselmon.getLogger().debug("Sent swap packet for egg with UUID '{}'", eggUuid);
        }
    }

    public static void onGuiInit(PCGUI pcGUI) {
        refreshPartyEggs(pcGUI);
    }

    private static Pokemon findFirstValidAnchor(ClientPC pc) {
        for (ClientBox box : pc.getBoxes()) {
            for (Pokemon pokemon : box.getSlots()) {
                // Ensure slot is occupied and not an egg
                if (pokemon != null && !PokemonPredicates.IS_NEODAYCARE_EGG.test(pokemon)) {
                    return pokemon;
                }
            }
        }
        return null;
    }
}
