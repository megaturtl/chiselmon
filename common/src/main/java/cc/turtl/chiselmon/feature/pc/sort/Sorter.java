package cc.turtl.chiselmon.feature.pc.sort;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.feature.eggspy.EggDummy;
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

        // Prepare the state
        List<Pokemon> allSlots = new ArrayList<>(box.getSlots());

        // Map each original pokemon to its preview for sorting purposes
        Map<Pokemon, Pokemon> originalToPreview = new HashMap<>();
        for (Pokemon p : allSlots) {
            if (p != null) {
                originalToPreview.put(p, ((DuckPreviewPokemon) p).chiselmon$getPreview());
            }
        }

        // Sort originals, but compare using their previews
        List<Pokemon> sortedList = allSlots.stream()
                .filter(Objects::nonNull)
                .sorted(createComparator(mode, reversed, originalToPreview))
                .toList();

        // Map current Pokemon to their current index for O(1) lookups
        Map<Pokemon, Integer> positions = new HashMap<>();
        for (int i = 0; i < allSlots.size(); i++) {
            if (allSlots.get(i) != null) positions.put(allSlots.get(i), i);
        }

        for (int targetIdx = 0; targetIdx < sortedList.size(); targetIdx++) {
            Pokemon targetPkm = sortedList.get(targetIdx);
            int currentIdx = positions.get(targetPkm);

            if (currentIdx == targetIdx) continue;

            // Send packets
            Pokemon displacedPkm = allSlots.get(targetIdx);
            sendPacket(boxIndex, targetPkm, currentIdx, displacedPkm, targetIdx);

            // Update local state
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

    private static Comparator<Pokemon> createComparator(SortMode mode, boolean reversed, Map<Pokemon, Pokemon> previews) {
        Comparator<Pokemon> eggLast = Comparator.comparing(p -> getPreview(previews, p) instanceof EggDummy);

        Comparator<Pokemon> nonEggOrder = Comparator.<Pokemon, Pokemon>comparing(
                        p -> getPreview(previews, p), mode.comparator(reversed))
                .thenComparingInt(p -> getPreview(previews, p).getLevel());

        Comparator<Pokemon> eggOrder = Comparator.comparingDouble(
                p -> getPreview(previews, p) instanceof EggDummy e ? e.getHatchPercentage() : 0);

        return eggLast.thenComparing((a, b) -> {
            boolean aIsEgg = getPreview(previews, a) instanceof EggDummy;
            boolean bIsEgg = getPreview(previews, b) instanceof EggDummy;
            if (aIsEgg && bIsEgg) return eggOrder.compare(a, b);
            if (!aIsEgg && !bIsEgg) return nonEggOrder.compare(a, b);
            return 0;
        });
    }

    private static Pokemon getPreview(Map<Pokemon, Pokemon> previews, Pokemon p) {
        Pokemon preview = previews.get(p);
        return preview != null ? preview : p;
    }
}