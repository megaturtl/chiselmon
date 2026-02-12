package cc.turtl.chiselmon.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChiselmonConfigScreen {

    public static Screen createScreen(Screen parent) {
        return ChiselmonConfigHandler.getHandler().generateGui().generateScreen(parent);
    }

    // Option 2: Custom GUI (if you want more control)
    public static Screen createCustomScreen(Screen parent) {
        ChiselmonConfigFields config = ChiselmonConfigHandler.getInstance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Chiselmon Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .tooltip(Component.literal("General Chiselmon settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Basic Settings"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Mod Disabled"))
                                        .description(OptionDescription.of(Component.literal("Disable the entire mod")))
                                        .binding(false, () -> config.modDisabled, value -> config.modDisabled = value)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Thresholds"))
                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Extreme Small"))
                                        .description(OptionDescription.of(Component.literal("Extreme small threshold")))
                                        .binding(0.4F, () -> config.threshold.extremeSmall, value -> config.threshold.extremeSmall = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.1F, 1.0F)
                                                .step(0.05F))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Extreme Large"))
                                        .description(OptionDescription.of(Component.literal("Extreme large threshold")))
                                        .binding(1.6F, () -> config.threshold.extremeLarge, value -> config.threshold.extremeLarge = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(1.0F, 3.0F)
                                                .step(0.1F))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.literal("Max IVs"))
                                        .description(OptionDescription.of(Component.literal("Maximum IVs")))
                                        .binding(5, () -> config.threshold.maxIvs, value -> config.threshold.maxIvs = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 31)
                                                .step(1))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("PC"))
                        .tooltip(Component.literal("PC Configuration"))
                        // Add PC config options here
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Alert"))
                        .tooltip(Component.literal("Alert Configuration"))
                        // Add Alert config options here
                        .build())
                .save(ChiselmonConfigHandler::save)
                .build()
                .generateScreen(parent);
    }
}