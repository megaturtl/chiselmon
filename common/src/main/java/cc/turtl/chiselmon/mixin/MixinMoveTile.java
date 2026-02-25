package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.calc.type.TypeCalcs;
import cc.turtl.chiselmon.api.calc.type.TypingMatchups;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(value = BattleMoveSelection.MoveTile.class, remap = false)
public abstract class MixinMoveTile {

    @Unique
    private static final int TOOLTIP_MAX_WIDTH = 200;

    @Shadow(remap = false)
    private MoveTemplate moveTemplate;

    @Final
    @Shadow(remap = false)
    private ElementalType elementalType;

    @Final
    @Shadow(remap = false)
    private InBattleMove move;

    @Final
    @Shadow(remap = false)
    private BattleMoveSelection moveSelection;

    @Shadow(remap = false)
    public abstract boolean isHovered(double mouseX, double mouseY);

    @Shadow(remap = false)
    public abstract List<Targetable> getTargetList();

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    public void chiselmon$renderMoveTooltip(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ChiselmonConfig config = ChiselmonConfig.get();

        if (config.general.modDisabled
                || !config.general.moveDetail
                || !isHovered(mouseX, mouseY)
                || moveTemplate == null) return;

        List<Component> lines = new ArrayList<>();

        lines.add(chiselmon$createMoveHeader());
        lines.addAll(chiselmon$createMoveDescription());
        lines.addAll(chiselmon$createEffectivenessLines());

        context.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);
    }

    @Unique
    private MutableComponent chiselmon$createMoveHeader() {
        String powerString = moveTemplate.getPower() == 0 ? "—" : String.valueOf((int) moveTemplate.getPower());
        String accuracyString = moveTemplate.getAccuracy() == -1 ? "—" : (int) moveTemplate.getAccuracy() + "%";

        MutableComponent header = Component.empty();

        header.append(moveTemplate.getDisplayName().copy()
                .withColor(elementalType.getHue()));
        header.append(Component.literal(" » ")
                .withColor(ColorUtils.DARK_GRAY.getRGB()));
        header.append(Component.literal("⚡")
                .withStyle(s -> s.withBold(true).withColor(ColorUtils.RED.getRGB())));
        header.append(Component.literal(" " + powerString)
                .withStyle(s -> s.withBold(false)));
        header.append(Component.literal(" • ").withColor(ColorUtils.DARK_GRAY.getRGB()));
        header.append(Component.literal("🎯")
                .withStyle(s -> s.withBold(true).withColor(ColorUtils.GREEN.getRGB())));
        header.append(Component.literal(" " + accuracyString)
                .withStyle(s -> s.withBold(false)));

        // If the move has an effect chance we add just the first for simplicity
        Arrays.stream(moveTemplate.getEffectChances()).findFirst().ifPresent(effectChance -> {
            header.append(Component.literal(" • ").withColor(ColorUtils.DARK_GRAY.getRGB()));
            header.append(Component.literal("⚗")
                    .withStyle(s -> s.withBold(false).withColor(ColorUtils.YELLOW.getRGB())));
            header.append(Component.literal(" " + effectChance.intValue() + "%")
                    .withStyle(s -> s.withBold(false)));
        });

        if (moveTemplate.getPriority() != 0) {
            header.append(Component.literal(" • ").withColor(ColorUtils.DARK_GRAY.getRGB()));
            header.append(Component.literal("⌛")
                    .withStyle(s -> s.withBold(false).withColor(ColorUtils.AQUA.getRGB())));
            header.append(Component.literal(" " + (moveTemplate.getPriority() > 0 ? "+" : "") + moveTemplate.getPriority()))
                    .withStyle(s -> s.withBold(false));
        }

        if (moveTemplate.getCritRatio() != 1) {
            header.append(Component.literal(" • ").withColor(ColorUtils.DARK_GRAY.getRGB()));
            header.append(Component.literal("💥")
                    .withStyle(s -> s.withBold(true).withColor(ColorUtils.PURPLE.getRGB())));
            header.append(Component.literal(" " + moveTemplate.getCritRatio() + "x")
                    .withStyle(s -> s.withBold(false)));
        }

        return header;
    }

    @Unique
    private List<MutableComponent> chiselmon$createMoveDescription() {
        // Split up the description to avoid reeeeallly long tooltips
        return Minecraft.getInstance().font
                .getSplitter()
                .splitLines(moveTemplate.getDescription(), TOOLTIP_MAX_WIDTH, Style.EMPTY)
                .stream()
                .map(line -> Component.literal(line.getString())
                        .withColor(ColorUtils.LIGHT_GRAY.getRGB()))
                .collect(Collectors.toList());
    }

    @Unique
    private List<MutableComponent> chiselmon$createEffectivenessLines() {
        List<Targetable> targets = getTargetList();

        // Spread moves (e.g. Heat Wave) return null from the targetList lambda
        // fall back to getMultiTargetList which handles them correctly
        if (targets == null) {
            Targetable activePokemon = moveSelection.getRequest().getActivePokemon();
            targets = activePokemon.getMultiTargetList(move.getTarget());
        }

        if (targets == null || targets.isEmpty()) return List.of();

        List<MutableComponent> result = targets.stream()
                .filter(Targetable::hasPokemon)
                .filter(t -> t instanceof ActiveClientBattlePokemon abp && abp.getBattlePokemon() != null)
                .map(t -> {
                    // Creates a dummy pokemon with the stats we care about
                    Pokemon defender = ((ActiveClientBattlePokemon) t).getBattlePokemon().getProperties().create();
                    TypingMatchups matchups = TypeCalcs.computeMatchups(defender.getTypes());
                    float multiplier = matchups.multiplierMap().getOrDefault(elementalType, 1.0f);
                    return multiplier == 1.0f ? null : chiselmon$createEffectivenessLine(defender.getSpecies().getName(), multiplier);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (result.isEmpty()) return List.of();

        result.addFirst(Component.empty());
        return result;
    }

    @Unique
    private MutableComponent chiselmon$createEffectivenessLine(String speciesName, float multiplier) {
        int color = switch ((int) (multiplier * 100)) {
            case 400 -> ColorUtils.MAGENTA.getRGB();
            case 200 -> ColorUtils.GREEN.getRGB();
            case 50 -> ColorUtils.ORANGE.getRGB();
            case 25 -> ColorUtils.YELLOW.getRGB();
            case 0 -> ColorUtils.RED.getRGB();
            default -> ColorUtils.WHITE.getRGB();
        };

        MutableComponent effectiveness = Component.empty();
        effectiveness.append(Component.literal("» ")
                .withColor(ColorUtils.DARK_GRAY.getRGB()));
        // cleans up decimal yuckness
        effectiveness.append("Deals " + (multiplier % 1 == 0 ? (int) multiplier : multiplier) + "x to ").withColor(color);
        effectiveness.append(Component.literal(speciesName).withColor(color));

        return effectiveness;
    }
}