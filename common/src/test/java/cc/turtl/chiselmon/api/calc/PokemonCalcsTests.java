package cc.turtl.chiselmon.api.calc;

import cc.turtl.chiselmon.junit.SetupTestDependencies;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SetupTestDependencies
@DisplayName("PokemonCalcs")
class PokemonCalcsTest {

    @Mock private Pokemon mockPokemon;

    @Nested
    @DisplayName("countPerfectIVs")
    class CountPerfectIVsTests {

        @Test
        @DisplayName("should return 0 when no IVs are perfect")
        void shouldReturnZeroWhenNoIVsArePerfect() {
            IVs ivs = IVs.createRandomIVs(0);
            Stats.Companion.getPERMANENT().forEach(stat -> ivs.set(stat, 0));
            when(mockPokemon.getIvs()).thenReturn(ivs);

            assertEquals(0, PokemonCalcs.countPerfectIVs(mockPokemon));
        }

        @Test
        @DisplayName("should return 0 when all IVs are just below maximum")
        void shouldReturnZeroWhenJustBelowMaximum() {
            IVs ivs = IVs.createRandomIVs(0);
            Stats.Companion.getPERMANENT().forEach(stat -> ivs.set(stat, IVs.MAX_VALUE - 1));
            when(mockPokemon.getIvs()).thenReturn(ivs);

            assertEquals(0, PokemonCalcs.countPerfectIVs(mockPokemon));
        }

        @Test
        @DisplayName("should return 6 when all IVs are naturally perfect")
        void shouldReturnSixWhenAllNaturallyPerfect() {
            IVs ivs = IVs.createRandomIVs(0);
            Stats.Companion.getPERMANENT().forEach(stat -> ivs.set(stat, IVs.MAX_VALUE));
            when(mockPokemon.getIvs()).thenReturn(ivs);

            assertEquals(6, PokemonCalcs.countPerfectIVs(mockPokemon));
        }

        @Test
        @DisplayName("should return 6 when all IVs are hypertrained to perfect")
        void shouldReturnSixWhenFullyHypertrained() {
            IVs ivs = IVs.createRandomIVs(0);
            Stats.Companion.getPERMANENT().forEach(stat -> ivs.setHyperTrainedIV(stat, IVs.MAX_VALUE));
            when(mockPokemon.getIvs()).thenReturn(ivs);

            assertEquals(6, PokemonCalcs.countPerfectIVs(mockPokemon));
        }

        @Test
        @DisplayName("should count mixed natural and hypertrained perfect IVs correctly")
        void shouldCountMixedNaturalAndHypertrained() {
            IVs ivs = IVs.createRandomIVs(0);
            ivs.set(Stats.HP, IVs.MAX_VALUE);
            ivs.set(Stats.ATTACK, IVs.MAX_VALUE);
            ivs.set(Stats.DEFENCE, 20);
            ivs.setHyperTrainedIV(Stats.DEFENCE, IVs.MAX_VALUE);
            ivs.set(Stats.SPECIAL_ATTACK, 0);
            ivs.set(Stats.SPECIAL_DEFENCE, 0);
            ivs.set(Stats.SPEED, 0);
            when(mockPokemon.getIvs()).thenReturn(ivs);

            assertEquals(3, PokemonCalcs.countPerfectIVs(mockPokemon));
        }
    }

    @Nested
    @DisplayName("countUniqueAbilities")
    class CountUniqueAbilitiesTests {

        @Mock(answer = Answers.RETURNS_DEEP_STUBS)
        private FormData mockForm;

        @BeforeEach
        void setUp() {
            when(mockPokemon.getForm()).thenReturn(mockForm);
        }

        @Test
        @DisplayName("should return 0 when ability map is empty")
        void shouldReturnZeroWhenNoAbilities() {
            when(mockForm.getAbilities().getMapping()).thenReturn(Collections.emptyMap());

            assertEquals(0, PokemonCalcs.countUniqueAbilities(mockPokemon));
        }

        @Test
        @DisplayName("should return 1 when multiple abilities share the same template")
        void shouldReturnOneWhenSingleUniqueAbility() {
            AbilityTemplate template = mock(AbilityTemplate.class);
            PotentialAbility ability1 = mock(PotentialAbility.class);
            PotentialAbility ability2 = mock(PotentialAbility.class);
            when(ability1.getTemplate()).thenReturn(template);
            when(ability2.getTemplate()).thenReturn(template);
            when(mockForm.getAbilities().getMapping()).thenReturn(Map.of(Priority.NORMAL, List.of(ability1, ability2)));

            assertEquals(1, PokemonCalcs.countUniqueAbilities(mockPokemon));
        }

        @Test
        @DisplayName("should return 2 when abilities have two distinct templates")
        void shouldReturnTwoWhenTwoUniqueAbilities() {
            PotentialAbility ability1 = mock(PotentialAbility.class);
            PotentialAbility ability2 = mock(PotentialAbility.class);
            when(ability1.getTemplate()).thenReturn(mock(AbilityTemplate.class));
            when(ability2.getTemplate()).thenReturn(mock(AbilityTemplate.class));
            when(mockForm.getAbilities().getMapping()).thenReturn(Map.of(Priority.NORMAL, List.of(ability1, ability2)));

            assertEquals(2, PokemonCalcs.countUniqueAbilities(mockPokemon));
        }
    }

    @Nested
    @DisplayName("getPossibleMoves")
    class GetPossibleMovesTests {

        @Mock(answer = Answers.RETURNS_DEEP_STUBS)
        private FormData mockForm;

        @Mock private MoveTemplate move1, move2, move3, move4, move5, move6;

        @BeforeEach
        void setUp() {
            when(mockPokemon.getForm()).thenReturn(mockForm);
        }

        @Test
        @DisplayName("should return all moves when preferLatest is false")
        void shouldReturnAllMovesWhenPreferLatestIsFalse() {
            Set<MoveTemplate> allMoves = new LinkedHashSet<>(List.of(move1, move2, move3, move4, move5));
            when(mockForm.getMoves().getLevelUpMovesUpTo(anyInt())).thenReturn(allMoves);
            when(mockPokemon.getLevel()).thenReturn(10);

            Set<MoveTemplate> result = PokemonCalcs.getPossibleMoves(mockPokemon, false);

            assertEquals(5, result.size());
            assertTrue(result.containsAll(allMoves));
        }

        @Test
        @DisplayName("should return last 4 moves when preferLatest is true and more than 4 moves available")
        void shouldReturnLastFourMovesWhenPreferLatestIsTrue() {
            Set<MoveTemplate> allMoves = new LinkedHashSet<>(List.of(move1, move2, move3, move4, move5, move6));
            when(mockForm.getMoves().getLevelUpMovesUpTo(anyInt())).thenReturn(allMoves);
            when(mockPokemon.getLevel()).thenReturn(20);

            Set<MoveTemplate> result = PokemonCalcs.getPossibleMoves(mockPokemon, true);

            assertEquals(4, result.size());
            assertFalse(result.contains(move1));
            assertFalse(result.contains(move2));
            assertTrue(result.contains(move3));
            assertTrue(result.contains(move4));
            assertTrue(result.contains(move5));
            assertTrue(result.contains(move6));
        }

        @Test
        @DisplayName("should preserve move order when returning last 4 moves")
        void shouldPreserveMoveOrderWhenReturningLastFour() {
            Set<MoveTemplate> allMoves = new LinkedHashSet<>(List.of(move1, move2, move3, move4, move5));
            when(mockForm.getMoves().getLevelUpMovesUpTo(anyInt())).thenReturn(allMoves);
            when(mockPokemon.getLevel()).thenReturn(15);

            List<MoveTemplate> result = new ArrayList<>(PokemonCalcs.getPossibleMoves(mockPokemon, true));

            assertEquals(4, result.size());
            assertEquals(move2, result.get(0));
            assertEquals(move3, result.get(1));
            assertEquals(move4, result.get(2));
            assertEquals(move5, result.get(3));
        }
    }
}