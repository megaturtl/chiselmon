package cc.turtl.cobbleaid.feature.gui.pc;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.api.filter.PokemonComparators;
import cc.turtl.cobbleaid.api.neodaycare.NeoDaycareEggData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PcSorter {

    public enum SortType {
        IVS, SIZE
    }

    private static Comparator<Pokemon> getComparator(SortType sortType) {
        return switch (sortType) {
            case SIZE -> PokemonComparators.SIZE_COMPARATOR;
            case IVS -> PokemonComparators.IVS_COMPARATOR;
        };
    }

    public static boolean sortPCBox(ClientPC clientPC, int boxNumber, SortType sortType, boolean reversed) {
        if (clientPC == null || boxNumber < 0) {
            return false;
        }

        List<ClientBox> boxes = clientPC.getBoxes();
        ClientBox currentBox = boxes.get(boxNumber);
        if (currentBox == null) {
            return false;
        }

        List<Pokemon> pokemonList = currentBox.getSlots().stream()
                .filter(pokemon -> pokemon != null)
                .toList();

        if (pokemonList.isEmpty()) {
            return false;
        }

        List<Pokemon> sortedPokemon = sortPokemonList(pokemonList, getComparator(sortType), reversed);
        applySortedOrder(boxNumber, currentBox, sortedPokemon);

        return true;
    }

    private static List<Pokemon> sortPokemonList(List<Pokemon> pokemonList, Comparator<Pokemon> comparator,
            boolean reversed) {
        if (pokemonList.isEmpty()) {
            return pokemonList;
        }

        // 1. Apply reversal to the comparator if requested
        if (reversed) {
            comparator = comparator.reversed();
        }

        // 2. Create a list of objects suitable for comparison
        List<Pokemon> sortableList = new java.util.ArrayList<>(pokemonList.size());
        for (Pokemon pokemon : pokemonList) {
            if (NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
                // Add the representation for sorting purposes
                sortableList.add(NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon());
            } else {
                // Add the original Pokemon object
                sortableList.add(pokemon);
            }
        }

        // 3. Sort the list of sortable representations (or originals)
        sortableList.sort(comparator);

        Comparator<Pokemon> finalComparator = comparator.thenComparing(Pokemon::getUuid); // Add tie-breaker

        Comparator<Pokemon> wrappedComparator = (p1, p2) -> {
            Pokemon actualP1 = NeoDaycareEggData.isNeoDaycareEgg(p1)
                    ? NeoDaycareEggData.createNeoDaycareEggData(p1).createDummyPokemon()
                    : p1;
            Pokemon actualP2 = NeoDaycareEggData.isNeoDaycareEgg(p2)
                    ? NeoDaycareEggData.createNeoDaycareEggData(p2).createDummyPokemon()
                    : p2;

            // Compare the representation objects
            return finalComparator.compare(actualP1, actualP2);
        };

        // Use a mutable copy of the original list for sorting
        List<Pokemon> mutableOriginalList = new java.util.ArrayList<>(pokemonList);
        mutableOriginalList.sort(wrappedComparator);

        return mutableOriginalList;
    }

    private static void applySortedOrder(int boxNumber, ClientBox box, List<Pokemon> sortedPokemon) {
        Map<UUID, Integer> currentPositions = new HashMap<>();

        // Map current positions
        List<Pokemon> slots = box.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            Pokemon pokemon = slots.get(i);
            if (pokemon != null) {
                currentPositions.put(pokemon.getUuid(), i);
            }
        }

        // Send all packets based on the desired final positions
        for (int targetSlot = 0; targetSlot < sortedPokemon.size(); targetSlot++) {
            Pokemon targetPokemon = sortedPokemon.get(targetSlot);
            Integer currentSlot = currentPositions.get(targetPokemon.getUuid());

            if (currentSlot == null || currentSlot == targetSlot) {
                continue;
            }

            Pokemon occupyingTarget = null;
            for (Map.Entry<UUID, Integer> entry : currentPositions.entrySet()) {
                if (entry.getValue() == targetSlot) {
                    occupyingTarget = sortedPokemon.stream()
                            .filter(p -> p.getUuid().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    break;
                }
            }

            if (occupyingTarget == null) {
                new MovePCPokemonPacket(
                        targetPokemon.getUuid(),
                        new PCPosition(boxNumber, currentSlot),
                        new PCPosition(boxNumber, targetSlot)).sendToServer();
                currentPositions.put(targetPokemon.getUuid(), targetSlot);
            } else {
                new SwapPCPokemonPacket(
                        targetPokemon.getUuid(),
                        new PCPosition(boxNumber, currentSlot),
                        occupyingTarget.getUuid(),
                        new PCPosition(boxNumber, targetSlot)).sendToServer();

                currentPositions.put(targetPokemon.getUuid(), targetSlot);
                currentPositions.put(occupyingTarget.getUuid(), currentSlot);
            }
        }
    }
}