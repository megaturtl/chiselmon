package cc.turtl.chiselmon.api.util;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.capture.CaptureChanceEstimator;
import cc.turtl.chiselmon.api.data.SimpleSpecies;
import cc.turtl.chiselmon.api.data.TypeEffectivenessCache;
import cc.turtl.chiselmon.api.predicate.MovePredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.StringFormats;
import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.riding.RidingStyle;
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings;
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
import java.util.*;
import java.util.stream.Collectors;

import static cc.turtl.chiselmon.util.ComponentUtil.*;

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

                    String displayName = Arrays.stream(groupName.split("_"))
                            .filter(part -> !part.isEmpty())
                            .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                            .collect(Collectors.joining(" "));
                    ;

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
                .append(colored(StringFormats.formatPercentage(totalPercent), totalRGB))
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

    public static Component effectiveTypesAgainst(Pokemon pokemon) {
        if (pokemon == null)
            return UNKNOWN;

        List<ElementalType> superEffectiveTypes = TypeEffectivenessCache.getSuperEffectiveTypes(pokemon.getTypes());

        if (superEffectiveTypes.isEmpty()) {
            return UNKNOWN;
        }

        return buildComponentWithSeparator(
                superEffectiveTypes,
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

        MutableComponent nameComponent = Component.empty();
        nameComponent.append(genderIcon(pokemon.getGender()));
        nameComponent.append(colored(" " + pokemon.getSpecies().getName(), ColorUtil.WHITE));
        nameComponent.append(colored(" Lv. " + pokemon.getLevel(), ColorUtil.LIGHT_GRAY));
        nameComponent.append(colored(" (" + sizeString + ")", ColorUtil.TEAL));

        if (pokemon.getShiny()) {
            nameComponent.append(colored(" ★", ColorUtil.GOLD));
        }

        return nameComponent;
    }

    public static Component catchRate(SimpleSpecies species) {
        if (species == null)
            return UNKNOWN;

        return colored(String.valueOf(species.catchRate), ColorUtil.WHITE);
    }

    public static Component catchChance(PokemonEntity pokemonEntity, PokeBall ball) {
        float catchChance = CaptureChanceEstimator.estimateCaptureProbability(pokemonEntity, ball);
        int rgb = ColorUtil.getRatioGradientColor(catchChance / 1.0f);

        Component catchChanceComponent = colored(StringFormats.formatPercentage(catchChance), rgb);

        return Component.empty()
                .append(colored("(", ColorUtil.LIGHT_GRAY))
                .append(catchChanceComponent)
                .append(colored(")", ColorUtil.LIGHT_GRAY));
    }

    public static Component rideStyles(Pokemon pokemon) {
        if (!PokemonPredicates.IS_RIDEABLE.test(pokemon)) {
            return UNKNOWN;
        }

        Map<RidingStyle, RidingBehaviourSettings> rideStyleBehaviours = pokemon.getRiding().getBehaviours();

        if (rideStyleBehaviours == null) {
            return UNKNOWN;
        }

        List<Component> rideStyleComponents = createRideStyleComponents(rideStyleBehaviours);

        return buildComponentWithSeparator(
                rideStyleComponents,
                COMMA_SEPARATOR,
                rideStyle -> rideStyle);
    }

    public static Component marks(Pokemon pokemon) {
        if (!PokemonPredicates.IS_MARKED.test(pokemon)) {
            return UNKNOWN;
        }

        Set<Mark> marks = pokemon.getMarks();

        return buildComponentWithSeparator(
                marks,
                COMMA_SEPARATOR,
                mark -> {
                    try {
                        Field nameField = Mark.class.getDeclaredField("name");
                        nameField.setAccessible(true);
                        String translationKey = (String) nameField.get(mark);

                        MutableComponent nameComponent = Component.translatable(translationKey);
                        nameComponent
                                .append(Component.literal(" (" + StringFormats.formatPercentage(mark.getChance()) + ")"));

                        return colored(nameComponent, Integer.parseInt(mark.getTitleColour(), 16));
                    } catch (Exception e) {
                        Chiselmon.getLogger().error("Failed to access Mark name field: {}", e.getMessage());
                        return UNKNOWN;
                    }
                });
    }

    public static Component dexStatus(Species species) {
        ClientPokedexManager playerDexData = CobblemonClient.INSTANCE.getClientPokedexData();
        ResourceLocation speciesId = species.getResourceIdentifier();
        PokedexEntryProgress knowledge = playerDexData.getHighestKnowledgeForSpecies(speciesId);

        return switch (knowledge) {
            case PokedexEntryProgress.CAUGHT -> colored("Caught", ColorUtil.GREEN);
            case PokedexEntryProgress.ENCOUNTERED -> colored("Encountered", ColorUtil.WHITE);
            case PokedexEntryProgress.NONE -> colored("Unknown", ColorUtil.DARK_GRAY);
            default -> UNKNOWN;
        };
    }

    private static List<Component> createRideStyleComponents(Map<RidingStyle, RidingBehaviourSettings> behaviours) {

        List<Component> components = new ArrayList<>();

        behaviours.forEach((style, settings) -> {
            int color = switch (style) {
                case LAND -> ColorUtil.GREEN;
                case LIQUID -> ColorUtil.AQUA;
                case AIR -> ColorUtil.LAVENDER;
            };

            String key = settings.getKey().toLanguageKey();
            String label = key.substring(key.lastIndexOf("/") + 1);

            components.add(colored(StringFormats.formatTitleCase(label), color));
        });
        return components;
    }

    private static String getStatDisplayName(String internalKey) {
        return switch (internalKey.toLowerCase()) {
            case "hp" -> "HP";
            case "attack" -> "Atk";
            case "defense" -> "Def";
            case "defence" -> "Def";
            case "special_attack" -> "SpA";
            case "special_defense" -> "SpD";
            case "special_defence" -> "SpD";
            case "speed" -> "Spe";
            default -> internalKey;
        };
    }
}