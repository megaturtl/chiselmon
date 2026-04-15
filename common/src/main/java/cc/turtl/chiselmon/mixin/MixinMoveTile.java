package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.core.api.calc.TypingMatchups;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.config.category.GeneralConfig;
import cc.turtl.chiselmon.core.api.calc.TypingMatchupsKt;
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

@Mixin(value = BattleMoveSelection.MoveTile.class)
public abstract class MixinMoveTile {

    @Unique private static final int TOOLTIP_MAX_WIDTH = 200;

    // Palette – mirrors the turtlshell ColorLib values used elsewhere in the project.
    @Unique private static final int COLOR_DARK_GRAY  = 0x555555;
    @Unique private static final int COLOR_RED        = 0xE13538;
    @Unique private static final int COLOR_GREEN      = 0x41D73B;
    @Unique private static final int COLOR_YELLOW     = 0xF9C74F;
    @Unique private static final int COLOR_AQUA       = 0x40E0D0;
    @Unique private static final int COLOR_PURPLE     = 0x6C44C3;
    @Unique private static final int COLOR_LIGHT_GRAY = 0xAAAAAA;
    @Unique private static final int COLOR_MAGENTA    = 0xFF00FF;
    @Unique private static final int COLOR_ORANGE     = 0xF9844A;
    @Unique private static final int COLOR_WHITE      = 0xFFFFFF;

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

    @Inject(method = "render", at = @At("TAIL"), remap = false) // doesn't override MC's render method
    public void chiselmon$renderMoveTooltip(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GeneralConfig config = ChiselmonConfig.INSTANCE.getGeneral();

        if (config.getModDisabled()
                || !config.getMoveDetail()
                || !isHovered(mouseX, mouseY)
                || moveTemplate == null) return;

        List<Component> lines = new ArrayList<>();

        lines.add(chiselmon$createMoveHeader());
        lines.addAll(chiselmon$createMoveDescription());
        lines.addAll(chiselmon$createEffectivenessLines());

        // push the tooltip in front of other tooltips and gui elements
        context.pose().pushPose();
        context.pose().translate(0, 0, 500);

        context.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);

        context.pose().popPose();
    }

    @Unique
    private MutableComponent chiselmon$createMoveHeader() {
        String powerString = moveTemplate.getPower() == 0 ? "—" : String.valueOf((int) moveTemplate.getPower());
        String accuracyString = moveTemplate.getAccuracy() == -1 ? "—" : (int) moveTemplate.getAccuracy() + "%";

        MutableComponent header = Component.empty();

        header.append(moveTemplate.getDisplayName().copy()
                .withColor(elementalType.getHue()));
        header.append(Component.literal(" » ")
                .withColor(COLOR_DARK_GRAY));
        header.append(Component.literal("⚡")
                .withStyle(s -> s.withBold(true).withColor(COLOR_RED)));
        header.append(Component.literal(" " + powerString)
                .withStyle(s -> s.withBold(false)));
        header.append(Component.literal(" • ").withColor(COLOR_DARK_GRAY));
        header.append(Component.literal("🎯")
                .withStyle(s -> s.withBold(true).withColor(COLOR_GREEN)));
        header.append(Component.literal(" " + accuracyString)
                .withStyle(s -> s.withBold(false)));

        // If the move has an effect chance we add just the first for simplicity
        Arrays.stream(moveTemplate.getEffectChances()).findFirst().ifPresent(effectChance -> {
            header.append(Component.literal(" • ").withColor(COLOR_DARK_GRAY));
            header.append(Component.literal("⚗")
                    .withStyle(s -> s.withBold(false).withColor(COLOR_YELLOW)));
            header.append(Component.literal(" " + effectChance.intValue() + "%")
                    .withStyle(s -> s.withBold(false)));
        });

        if (moveTemplate.getPriority() != 0) {
            header.append(Component.literal(" • ").withColor(COLOR_DARK_GRAY));
            header.append(Component.literal("⌛")
                    .withStyle(s -> s.withBold(false).withColor(COLOR_AQUA)));
            header.append(Component.literal(" " + (moveTemplate.getPriority() > 0 ? "+" : "") + moveTemplate.getPriority()))
                    .withStyle(s -> s.withBold(false));
        }

        if (moveTemplate.getCritRatio() != 1) {
            header.append(Component.literal(" • ").withColor(COLOR_DARK_GRAY));
            header.append(Component.literal("💥")
                    .withStyle(s -> s.withBold(true).withColor(COLOR_PURPLE)));
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
                .map(line -> Component.literal(line.getString()).withColor(COLOR_LIGHT_GRAY))
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
                    TypingMatchups matchups = TypingMatchupsKt.computeMatchups(defender.getTypes());
                    float multiplier = matchups.getMultiplierMap().getOrDefault(elementalType, 1.0f);
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
            case 400 -> COLOR_MAGENTA;
            case 200 -> COLOR_GREEN;
            case 50  -> COLOR_ORANGE;
            case 25  -> COLOR_YELLOW;
            case 0   -> COLOR_RED;
            default  -> COLOR_WHITE;
        };

        MutableComponent effectiveness = Component.empty();
        effectiveness.append(Component.literal("» ").withColor(COLOR_DARK_GRAY));
        // cleans up decimal yuckness
        effectiveness.append("Deals " + (multiplier % 1 == 0 ? (int) multiplier : multiplier) + "x to ").withColor(color);
        effectiveness.append(Component.literal(speciesName).withColor(color));

        return effectiveness;
    }
}
