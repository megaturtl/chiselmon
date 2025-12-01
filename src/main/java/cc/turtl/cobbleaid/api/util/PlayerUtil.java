package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;


public class PlayerUtil {
    public static int getHighestLevel() {
        ClientParty party = CobblemonClient.INSTANCE.getStorage().getParty();
            if (party.isEmpty()) {
                return 1;
            }

            int highestLevel = 1;
            for (Pokemon pokemon : party) {
                if (pokemon == null) {
                    continue;
                }
                int level = pokemon.getLevel(); 

                if (level > highestLevel) {
                    highestLevel = level;
                }
            }
        return highestLevel;
    }
}
