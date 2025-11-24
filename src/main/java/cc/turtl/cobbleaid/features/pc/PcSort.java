package cc.turtl.cobbleaid.features.pc;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cc.turtl.cobbleaid.api.StatCalculator;

public final class PcSort {

    public enum SortType {
        IVS, SIZE
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

        List<Pokemon> sortedPokemon = sortPokemonList(pokemonList, sortType, reversed);
        applySortedOrder(boxNumber, currentBox, sortedPokemon);

        return true;
    }

    private static List<Pokemon> sortPokemonList(List<Pokemon> pokemonList, SortType sortType, boolean reversed) {
        Comparator<Pokemon> comparator = getComparator(sortType);
        if (comparator != null) {
            if (reversed) {
                comparator = comparator.reversed();
            }
            pokemonList = new java.util.ArrayList<>(pokemonList);
            pokemonList.sort(comparator);
        }
        return pokemonList;
    }

    private static Comparator<Pokemon> getComparator(SortType sortType) {
        return switch (sortType) {
            case SIZE -> Comparator.nullsLast(
                    Comparator.comparingDouble(Pokemon::getScaleModifier));
            case IVS -> Comparator.nullsLast(
                    Comparator.comparing(
                            StatCalculator::calculateTotalIVs,
                            Integer::compare));
        };
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