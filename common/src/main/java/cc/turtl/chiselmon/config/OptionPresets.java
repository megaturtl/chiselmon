package cc.turtl.chiselmon.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class OptionPresets {
    public static Option<Boolean> tickBox(String key, boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(modTranslatable(key))
                .binding(def, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    public static Option<Float> floatSlider(String key, float def, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
        return Option.<Float>createBuilder()
                .name(modTranslatable(key))
                .binding(def, getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }

    public static Option<Integer> intSlider(String key, int def, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max, int step) {
        return Option.<Integer>createBuilder()
                .name(modTranslatable(key))
                .binding(def, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }
}