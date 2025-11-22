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
        SIZE_SMALLEST_TO_LARGEST,
        SIZE_LARGEST_TO_SMALLEST,
        IV_TOTAL_HIGH_TO_LOW,
        IV_TOTAL_LOW_TO_HIGH,
        POKEDEX_ASCENDING,
        POKEDEX_DESCENDING,
        NAME_A_TO_Z,
        NAME_Z_TO_A
    }

    public static boolean sortCurrentBox(ClientPC clientPC, int boxNumber, SortType sortType) {
        if (clientPC == null || boxNumber < 0) {
            return false;
        }

        List<ClientBox> boxes = clientPC.getBoxes();
        if (boxNumber >= boxes.size()) {
            return false;
        }

        ClientBox box = boxes.get(boxNumber);
        if (box == null) {
            return false;
        }

        // 1. Extract non-null Pokemon and remember occupied slot indices
        List<Pokemon> allSlots = box.getSlots();
        List<Integer> occupiedSlotIndices = new java.util.ArrayList<>();
        List<Pokemon> pokemonList = new java.util.ArrayList<>();

        for (int i = 0; i < allSlots.size(); i++) {
            Pokemon pokemon = allSlots.get(i);
            if (pokemon != null) {
                pokemonList.add(pokemon);
                occupiedSlotIndices.add(i);
            }
        }

        if (pokemonList.isEmpty()) {
            return false;
        }

        // 2. Sort the list of non-null Pokemon
        sortPokemonList(pokemonList, sortType);

        // 3. Apply the new order using the normal PC interaction packets
        applySortedOrder(boxNumber, box, pokemonList, occupiedSlotIndices);

        return true;
    }

    private static void sortPokemonList(List<Pokemon> pokemonList, SortType sortType) {
        Comparator<Pokemon> comparator = getComparator(sortType);
        if (comparator != null) {
            pokemonList.sort(comparator);
        }
    }

    private static Comparator<Pokemon> getComparator(SortType sortType) {
        return switch (sortType) {
            case SIZE_SMALLEST_TO_LARGEST -> Comparator.nullsLast(
                    Comparator.comparingDouble(
                            (Pokemon pokemon) -> pokemon.getScaleModifier()));
            case SIZE_LARGEST_TO_SMALLEST -> Comparator.nullsLast(
                    Comparator.comparingDouble(
                            (Pokemon pokemon) -> pokemon.getScaleModifier()).reversed());

            case IV_TOTAL_HIGH_TO_LOW -> Comparator.nullsLast(
                    Comparator.comparing(
                            StatCalculator::calculateTotalIVs,
                            Integer::compare).reversed()
                );
            case IV_TOTAL_LOW_TO_HIGH -> Comparator.nullsLast(
                    Comparator.comparing(
                            StatCalculator::calculateTotalIVs,
                            Integer::compare));

            case POKEDEX_ASCENDING -> Comparator.nullsLast(
                    Comparator.comparingInt(
                            (Pokemon pokemon) -> pokemon.getSpecies().getNationalPokedexNumber()));
            case POKEDEX_DESCENDING -> Comparator.nullsLast(
                    Comparator.comparingInt(
                            (Pokemon pokemon) -> pokemon.getSpecies().getNationalPokedexNumber()).reversed());

            case NAME_A_TO_Z -> Comparator.nullsLast(
                    Comparator.comparing(
                            (Pokemon pokemon) -> pokemon.getDisplayName(false).getString().toLowerCase()));
            case NAME_Z_TO_A -> Comparator.nullsLast(
                    Comparator.comparing(
                            (Pokemon pokemon) -> pokemon.getDisplayName(false).getString().toLowerCase()).reversed());
        };
    }

    private static void applySortedOrder(int boxNumber, ClientBox box, List<Pokemon> sortedPokemon,
            List<Integer> slotIndices) {
        List<Pokemon> workingSlots = new java.util.ArrayList<>(box.getSlots());
        Map<UUID, Integer> currentPositions = new HashMap<>();

        for (int i = 0; i < workingSlots.size(); i++) {
            Pokemon pokemon = workingSlots.get(i);
            if (pokemon != null) {
                currentPositions.put(pokemon.getUuid(), i);
            }
        }

        for (int i = 0; i < sortedPokemon.size(); i++) {
            Pokemon targetPokemon = sortedPokemon.get(i);
            if (targetPokemon == null) {
                continue;
            }

            int targetSlot = slotIndices.get(i);
            Integer currentSlot = currentPositions.get(targetPokemon.getUuid());

            if (currentSlot == null || currentSlot == targetSlot) {
                continue;
            }

            Pokemon occupyingTarget = workingSlots.get(targetSlot);

            if (occupyingTarget == null) {
                movePokemon(workingSlots, currentPositions, targetPokemon, boxNumber, currentSlot, targetSlot);
            } else {
                swapPokemon(workingSlots, currentPositions, targetPokemon, occupyingTarget, boxNumber, currentSlot,
                        targetSlot);
            }
        }
    }

    private static void movePokemon(
            List<Pokemon> workingSlots,
            Map<UUID, Integer> positions,
            Pokemon pokemon,
            int boxNumber,
            int fromSlot,
            int toSlot) {
        new MovePCPokemonPacket(
                pokemon.getUuid(),
                new PCPosition(boxNumber, fromSlot),
                new PCPosition(boxNumber, toSlot)).sendToServer();

        workingSlots.set(toSlot, pokemon);
        workingSlots.set(fromSlot, null);

        positions.put(pokemon.getUuid(), toSlot);
    }

    private static void swapPokemon(
            List<Pokemon> workingSlots,
            Map<UUID, Integer> positions,
            Pokemon movingPokemon,
            Pokemon occupyingTarget,
            int boxNumber,
            int fromSlot,
            int targetSlot) {
        new SwapPCPokemonPacket(
                movingPokemon.getUuid(),
                new PCPosition(boxNumber, fromSlot),
                occupyingTarget.getUuid(),
                new PCPosition(boxNumber, targetSlot)).sendToServer();

        workingSlots.set(fromSlot, occupyingTarget);
        workingSlots.set(targetSlot, movingPokemon);

        positions.put(movingPokemon.getUuid(), targetSlot);
        positions.put(occupyingTarget.getUuid(), fromSlot);
    }
}