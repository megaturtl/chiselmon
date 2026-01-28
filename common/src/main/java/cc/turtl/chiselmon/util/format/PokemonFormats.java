package cc.turtl.chiselmon.util.format;

import cc.turtl.chiselmon.api.calc.PokemonCalcs;
import cc.turtl.chiselmon.api.calc.capture.CaptureChanceEstimator;
import cc.turtl.chiselmon.api.data.type.TypeEffectivenessCache;
import cc.turtl.chiselmon.api.data.species.ClientSpecies;
import cc.turtl.chiselmon.api.predicate.MoveTemplatePredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static cc.turtl.chiselmon.util.format.ComponentUtils.*;

/**
 * High-level formatter for turning Pokemon data into styled Components.
 */
public final class PokemonFormats {
    private PokemonFormats() {}

    // --- Icons & Constants ---
    private static final Component ICON_MALE = literal("♂", ColorUtils.BLUE);
    private static final Component ICON_FEMALE = literal("♀", ColorUtils.PINK);
    private static final Component ICON_GENDERLESS = literal("●", ColorUtils.LIGHT_GRAY);

    private static final Map<String, Integer> EGG_GROUP_COLORS = Map.ofEntries(
            Map.entry("monster", 0x97724C), Map.entry("water_1", 0x6BD1F9),
            Map.entry("bug", 0xAAC22A), Map.entry("flying", 0x90AFF1),
            Map.entry("field", 0xE5BA65), Map.entry("fairy", 0xFF9EB9),
            Map.entry("grass", 0x82D25A), Map.entry("human_like", 0x47B7AE),
            Map.entry("water_3", 0x2271B4), Map.entry("mineral", 0x979067),
            Map.entry("amorphous", 0x9F82CC), Map.entry("water_2", 0x4B94ED),
            Map.entry("ditto", 0xB6AAD5), Map.entry("dragon", 0x5E57BF)
    );

    // --- Identification ---

    public static Component genderIcon(Gender gender) {
        if (gender == null) return literal("?", ColorUtils.DARK_GRAY);
        return switch (gender) {
            case MALE -> ICON_MALE;
            case FEMALE -> ICON_FEMALE;
            default -> ICON_GENDERLESS;
        };
    }

    public static Component detailedName(Pokemon pokemon) {
        if (pokemon == null) return UNKNOWN;

        MutableComponent base = Component.empty()
                .append(genderIcon(pokemon.getGender()))
                .append(" ").append(literal(pokemon.getSpecies().getName(), ColorUtils.WHITE))
                .append(" ").append(literal("Lv. " + pokemon.getLevel(), ColorUtils.LIGHT_GRAY));

        if (pokemon.getShiny()) base.append(literal(" ★", ColorUtils.GOLD));

        String size = String.format("%.2f", pokemon.getScaleModifier());
        return base.append(literal(" (" + size + ")", ColorUtils.TEAL));
    }

    public static Component form(Pokemon pokemon) {
        if (pokemon == null) return UNKNOWN;
        return literal(pokemon.getForm().getName(), ColorUtils.WHITE);
    }

    // --- Combat & Stats ---

    public static Component types(Pokemon pokemon) {
        if (pokemon == null) return UNKNOWN;
        return join(pokemon.getTypes(), " / ", type -> type.getDisplayName().withColor(type.getHue()));
    }

    public static Component effectiveTypesAgainst(Pokemon pokemon) {
        if (pokemon == null) return UNKNOWN;
        List<ElementalType> superEffective = TypeEffectivenessCache.getSuperEffective(pokemon.getTypes());
        if (superEffective.isEmpty()) return UNKNOWN;

        return join(superEffective, " / ", type -> type.getDisplayName().withColor(type.getHue()));
    }

    public static Component ivSummary(Pokemon pokemon) {
        if (pokemon == null) return UNKNOWN;
        IVs ivs = pokemon.getIvs();

        Component stats = join(Stats.Companion.getPERMANENT(), "/", stat -> {
            int val = ivs.getEffectiveBattleIV(stat);
            float ratio = (float) val / IVs.MAX_VALUE;
            return literal(val, ColorUtils.getGradient(ratio, ColorUtils.RED, ColorUtils.YELLOW, ColorUtils.GREEN));
        });

        float totalRatio = (float) ivs.getEffectiveBattleTotal() / IVs.MAX_TOTAL;
        int totalColor = ColorUtils.getGradient(totalRatio, ColorUtils.RED, ColorUtils.YELLOW, ColorUtils.GREEN);

        return Component.empty().append(stats).append(" ")
                .append(literal("(" + StringFormats.formatPercentage(totalRatio) + ")", totalColor));
    }

    public static Component selfDamagingMoves(Pokemon pokemon) {
        var moves = PokemonCalcs.getPossibleMoves(pokemon, true).stream()
                .filter(MoveTemplatePredicates.IS_SELF_DAMAGING)
                .toList();

        if (moves.isEmpty()) return UNKNOWN;
        return join(moves, ", ", move -> move.getDisplayName().withColor(ColorUtils.RED));
    }

    // --- Breeding & Species ---

    public static Component eggGroups(ClientSpecies species) {
        if (species == null || species.eggGroups().isEmpty()) return UNKNOWN;
        return join(species.eggGroups(), " / ", group -> {
            int color = EGG_GROUP_COLORS.getOrDefault(group.toLowerCase(), ColorUtils.WHITE);
            return literal(StringFormats.formatSnakeCase(group), color);
        });
    }

    public static Component evYield(ClientSpecies species) {
        if (species == null || species.evYield().isEmpty()) return UNKNOWN;

        var yields = species.evYield().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .toList();

        return join(yields, ", ", entry -> Component.empty()
                .append(literal(entry.getValue(), ColorUtils.WHITE))
                .append(" ")
                .append(literal(getStatDisplayName(entry.getKey()), ColorUtils.WHITE)));
    }

    // --- Capture ---

    public static Component catchRate(ClientSpecies species) {
        return (species == null) ? UNKNOWN : literal(species.catchRate(), ColorUtils.WHITE);
    }

    public static Component catchChance(PokemonEntity entity, PokeBall ball) {
        var estimator = new CaptureChanceEstimator();
        float chance = estimator.estimateCaptureProbability(entity, ball);
        int color = ColorUtils.getGradient(chance, ColorUtils.RED, ColorUtils.YELLOW, ColorUtils.GREEN);

        return Component.empty()
                .append(literal("(", ColorUtils.LIGHT_GRAY))
                .append(literal(StringFormats.formatPercentage(chance), color))
                .append(literal(")", ColorUtils.LIGHT_GRAY));
    }

    // --- Misc ---

    public static Component rideStyles(Pokemon pokemon) {
        if (!PokemonPredicates.IS_RIDEABLE.test(pokemon)) return UNKNOWN;
        var behaviours = pokemon.getRiding().getBehaviours();
        if (behaviours == null) return UNKNOWN;

        return join(behaviours.keySet(), ", ", style -> {
            int color = switch (style) {
                case LAND -> ColorUtils.GREEN;
                case LIQUID -> ColorUtils.AQUA;
                case AIR -> ColorUtils.LAVENDER;
            };
            return literal(StringFormats.capitalize(style.name()), color);
        });
    }

    public static Component marks(Pokemon pokemon) {
        if (!PokemonPredicates.IS_MARKED.test(pokemon)) return UNKNOWN;

        return join(pokemon.getMarks(), ", ", mark -> {
            try {
                Field f = Mark.class.getDeclaredField("name");
                f.setAccessible(true);
                String key = (String) f.get(mark);
                int color = Integer.parseInt(mark.getTitleColour(), 16);

                return literal(Component.translatable(key).getString(), color)
                        .append(literal(" (" + StringFormats.formatPercentage(mark.getChance()) + ")", ColorUtils.DARK_GRAY));
            } catch (Exception e) {
                return UNKNOWN;
            }
        });
    }

    public static Component dexStatus(Species species) {
        ClientPokedexManager playerDexData = CobblemonClient.INSTANCE.getClientPokedexData();
        ResourceLocation speciesId = species.getResourceIdentifier();
        PokedexEntryProgress knowledge = playerDexData.getHighestKnowledgeForSpecies(speciesId);

        return switch (knowledge) {
            case CAUGHT -> literal("Caught", ColorUtils.GREEN);
            case ENCOUNTERED -> literal("Encountered", ColorUtils.WHITE);
            default -> literal("Unknown", ColorUtils.DARK_GRAY);
        };
    }

    private static String getStatDisplayName(String internalKey) {
        return switch (internalKey.toLowerCase()) {
            case "hp" -> "HP";
            case "attack" -> "Atk";
            case "defense", "defence" -> "Def";
            case "special_attack" -> "SpA";
            case "special_defense", "special_defence" -> "SpD";
            case "speed" -> "Spe";
            default -> StringFormats.capitalize(internalKey);
        };
    }
}