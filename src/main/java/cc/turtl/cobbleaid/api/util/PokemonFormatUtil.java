package cc.turtl.cobbleaid.api.util;

import static cc.turtl.cobbleaid.util.ComponentFormatUtil.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.SimpleSpecies;
import cc.turtl.cobbleaid.api.capture.CaptureChanceEstimator;
import cc.turtl.cobbleaid.api.predicate.MovePredicates;
import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.StringUtils;
import net.minecraft.network.chat.Component;

public final class PokemonFormatUtil {
    private PokemonFormatUtil() {
    }

    private static final Component SLASH_SEPERATOR = separator("/");
    private static final Component SLASH_SPACE_SEPERATOR = separator(" / ");
    private static final Component COMMA_SEPARATOR = separator(", ");

    private static final Component GENDER_UNKNOWN = colored("?", ColorUtil.DARK_GRAY);
    private static final Component MALE = colored("♂", ColorUtil.BLUE);
    private static final Component FEMALE = colored("♀", ColorUtil.PINK);
    private static final Component GENDERLESS = colored("●", ColorUtil.LIGHT_GRAY);

    public static Component genderIcon(Gender gender) {
        if (gender == null)
            return GENDER_UNKNOWN;

        return switch (gender) {
            case MALE -> MALE;
            case FEMALE -> FEMALE;
            case GENDERLESS -> GENDERLESS;
        };
    }

    private static final Map<String, Integer> EGG_GROUP_COLORS = Map.ofEntries(
            Map.entry("monster", 0x97724C),
            Map.entry("water_1", 0x6BD1F9),
            Map.entry("bug", 0xAAC22A),
            Map.entry("flying", 0x90AFF1),
            Map.entry("field", 0xE5BA65),
            Map.entry("fairy", 0xFF9EB9),
            Map.entry("grass", 0x82D25A),
            Map.entry("human_like", 0x47B7AE),
            Map.entry("water_3", 0x2271B4),
            Map.entry("mineral", 0x979067),
            Map.entry("amorphous", 0x9F82CC),
            Map.entry("water_2", 0x4B94ED),
            Map.entry("ditto", 0xB6AAD5),
            Map.entry("dragon", 0x5E57BF),
            Map.entry("undiscovered", ColorUtil.DARK_GRAY));

    public static Component eggGroups(SimpleSpecies species) {
        if (species == null || species.eggGroups.isEmpty())
            return UNKNOWN;

        return buildComponentWithSeparator(
                species.eggGroups,
                SLASH_SPACE_SEPERATOR,
                groupName -> {
                    String key = groupName.toLowerCase();
                    int color = EGG_GROUP_COLORS.getOrDefault(key, ColorUtil.WHITE);

                    String displayName = StringUtils.formatDisplayName(groupName);

                    return colored(displayName, color);
                });
    }

    public static Component hypertrainedIVs(Pokemon pokemon) {
        if (pokemon == null)
            return UNKNOWN;

        IVs ivs = pokemon.getIvs();

        Component statsComponent = buildComponentWithSeparator(
                Stats.Companion.getPERMANENT(),
                SLASH_SEPERATOR,
                stat -> {
                    int value = ivs.getEffectiveBattleIV(stat);
                    int rgb = ColorUtil.getRatioGradientColor((float) value / IVs.MAX_VALUE);
                    return colored(String.valueOf(value), rgb);
                });

        float totalPercent = (float) ivs.getEffectiveBattleTotal() / IVs.MAX_TOTAL;
        int totalRGB = ColorUtil.getRatioGradientColor(totalPercent);

        return Component.empty()
                .append(statsComponent)
                .append(" (")
                .append(colored(StringUtils.formatPercentage(totalPercent), totalRGB))
                .append(")");
    }

    public static Component selfDamagingMoves(Pokemon pokemon) {
        Set<MoveTemplate> possibleMoves = PokemonCalcUtil.getPossibleMoves(pokemon, true);
        List<MoveTemplate> possibleSelfDamagingMoves = possibleMoves.stream()
                .filter(MovePredicates.IS_SELF_DAMAGING)
                .toList();

        return buildComponentWithSeparator(
                possibleSelfDamagingMoves,
                COMMA_SEPARATOR,
                move -> colored(
                        move.getDisplayName(),
                        ColorUtil.RED));
    }

    public static Component types(Pokemon pokemon) {
        if (pokemon == null)
            return UNKNOWN;

        return buildComponentWithSeparator(
                pokemon.getTypes(),
                SLASH_SPACE_SEPERATOR,
                type -> colored(
                        type.getDisplayName(),
                        type.getHue()));
    }

    public static Component evYield(SimpleSpecies species) {
        if (species == null || species.evYield == null || species.evYield.isEmpty())
            return UNKNOWN;

        List<String> yieldingStatKeys = species.evYield.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();

        if (yieldingStatKeys.isEmpty())
            return UNKNOWN;

        return buildComponentWithSeparator(
                yieldingStatKeys,
                COMMA_SEPARATOR,
                statKey -> {
                    int value = species.evYield.get(statKey);
                    String displayName = getStatDisplayName(statKey);

                    return Component.empty()
                            .append(colored(String.valueOf(value), ColorUtil.WHITE))
                            .append(Component.literal(" "))
                            .append(colored(displayName, ColorUtil.WHITE));
                });
    }

    public static Component detailedPokemonName(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        String sizeString = String.format("%.2f", pokemon.getScaleModifier());

        return Component.empty()
                .append(genderIcon(pokemon.getGender()))
                .append(colored(" " + pokemon.getSpecies().getName(), ColorUtil.WHITE))
                .append(colored(" Lv. ", ColorUtil.LIGHT_GRAY))
                .append(colored(String.valueOf(pokemon.getLevel()), ColorUtil.LIGHT_GRAY))
                .append(colored(" (" + sizeString + ")", ColorUtil.TEAL));
    }

    public static Component catchChance(PokemonEntity pokemonEntity, PokeBall ball) {
        float catchChance = CaptureChanceEstimator.estimateCaptureProbability(pokemonEntity, ball);
        int rgb = ColorUtil.getRatioGradientColor(catchChance / 1.0f);

        Component catchChanceComponent = colored(StringUtils.formatPercentage(catchChance), rgb);

        return Component.empty()
                .append(colored("(", ColorUtil.LIGHT_GRAY))
                .append(catchChanceComponent)
                .append(colored(")", ColorUtil.LIGHT_GRAY));
    }

    private static String getStatDisplayName(String internalKey) {
        return switch (internalKey.toLowerCase()) {
            case "hp" -> "HP";
            case "attack" -> "Atk";
            case "defense" -> "Def";
            case "defence" -> "Def";
            case "special_attack" -> "Sp. Atk";
            case "special_defense" -> "Sp. Def";
            case "special_defence" -> "Sp. Def";
            case "speed" -> "Spe";
            default -> internalKey;
        };
    }
}