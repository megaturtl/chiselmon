package cc.turtl.chiselmon.feature.pc.sort;

import cc.turtl.chiselmon.feature.pc.eggspy.EggDummy;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.*;

public final class Sorter {

    public static void sortPCBox(ClientPC pc, int boxIndex, SortMode mode, boolean reversed) {
        ClientBox box = pc.getBoxes().get(boxIndex);
        if (box == null) return;

        // 1. Prepare the state
        List<Pokemon> allSlots = new ArrayList<>(box.getSlots()); // Exactly 30 slots
        List<Pokemon> sortedList = allSlots.stream()
                .filter(Objects::nonNull)
                .sorted(createComparator(mode, reversed))
                .toList();

        // Map current Pokemon to their current index for O(1) lookups
        Map<Pokemon, Integer> positions = new HashMap<>();
        for (int i = 0; i < allSlots.size(); i++) {
            if (allSlots.get(i) != null) positions.put(allSlots.get(i), i);
        }

        // 2. The Synchronization Loop
        // We only care about placing the sorted Pokemon into the first N slots
        for (int targetIdx = 0; targetIdx < sortedList.size(); targetIdx++) {
            Pokemon targetPkm = sortedList.get(targetIdx);
            int currentIdx = positions.get(targetPkm);

            if (currentIdx == targetIdx) continue;

            // Decide and Send Packet
            Pokemon displacedPkm = allSlots.get(targetIdx);
            sendPacket(boxIndex, targetPkm, currentIdx, displacedPkm, targetIdx);

            // Update Local State (The "Virtual Swap")
            allSlots.set(currentIdx, displacedPkm);
            allSlots.set(targetIdx, targetPkm);

            positions.put(targetPkm, targetIdx);
            if (displacedPkm != null) positions.put(displacedPkm, currentIdx);
        }
    }

    private static void sendPacket(int box, Pokemon target, int from, Pokemon displaced, int to) {
        PCPosition source = new PCPosition(box, from);
        PCPosition destination = new PCPosition(box, to);

        if (displaced == null) {
            new MovePCPokemonPacket(target.getUuid(), source, destination).sendToServer();
        } else {
            new SwapPCPokemonPacket(target.getUuid(), source, displaced.getUuid(), destination).sendToServer();
        }
    }

    private static Comparator<Pokemon> createComparator(SortMode mode, boolean reversed) {
        return Comparator.comparing((Pokemon p) -> p instanceof EggDummy) // Eggs last
                .thenComparing(p -> p, mode.comparator(reversed))
                .thenComparing(Pokemon::getLevel)
                .thenComparingDouble(p -> p instanceof EggDummy e ? e.getHatchCompletion() : 0.0);
    }
}