package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.config.custom.KeyController;
import cc.turtl.chiselmon.mixin.accessor.KeyMappingAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Factory for creating common YACL option types with consistent styling.
 */
public class OptionFactory {
    private OptionFactory() {
    }

    /**
     * Creates a boolean toggle/tickbox option.
     */
    public static Option<Boolean> toggleTick(String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    public static Option<Boolean> toggleOnOff(String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();
    }

    /**
     * Creates a float slider option.
     */
    public static Option<Float> floatSlider(String translationKey, Supplier<Float> getter, Consumer<Float> setter,
                                            float min, float max, float step) {
        return Option.<Float>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(min, max)
                        .step(step))
                .build();
    }

    /**
     * Creates an integer slider option.
     */
    public static Option<Integer> intSlider(String translationKey, Supplier<Integer> getter, Consumer<Integer> setter,
                                            int min, int max, int step) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(min, max)
                        .step(step))
                .build();
    }

    /**
     * Creates an enum cycler option.
     */
    public static <T extends Enum<T>> Option<T> enumCycler(String translationKey, Supplier<T> getter, Consumer<T> setter,
                                                           Class<T> enumClass) {
        return Option.<T>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(enumClass))
                .build();
    }

    /**
     * Creates a string text field option.
     */
    public static Option<String> textField(String translationKey, Supplier<String> getter, Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(StringControllerBuilder::create)
                .build();
    }

    /**
     * Creates a string text field option.
     */
    public static Option<Color> colorPicker(String translationKey, Supplier<Color> getter, Consumer<Color> setter) {
        return Option.<Color>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .controller(ColorControllerBuilder::create)
                .build();
    }

    /**
     * Creates a KeyMapping field option, this means it is synched with Minecraft's key map registry.
     */
    public static Option<InputConstants.Key> keyMappingPicker(String translationKey, KeyMapping keyMapping) {
        return Option.<InputConstants.Key>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(
                        keyMapping.getDefaultKey(),
                        () -> ((KeyMappingAccessor) keyMapping).chiselmon$getKey(),
                        v -> {
                            ChiselmonKeybinds.rebind(keyMapping, v);
                        })
                .customController(KeyController::new)
                .build();
    }

    /**
     * Creates a simple Key field option, this means it is NOT synched with Minecraft's key map registry.
     * e.g for handling specific mouse clicks inside an inventory where key overlaps don't matter.
     */
    public static Option<InputConstants.Key> hotkeyPicker(String translationKey, Supplier<InputConstants.Key> getter, Consumer<InputConstants.Key> setter) {
        return Option.<InputConstants.Key>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(getter.get(), getter, setter)
                .customController(KeyController::new)
                .build();
    }
}