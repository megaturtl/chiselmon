package cc.turtl.chiselmon.client.config.category

import cc.turtl.chiselmon.api.filter.FilterDefinition
import cc.turtl.chiselmon.api.filter.FiltersUserData
import cc.turtl.chiselmon.api.filter.match.FilterMatcher
import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.turtlshell.api.client.config.OptionFactory
import cc.turtl.turtlshell.api.client.config.custom.HoldToConfirmButton
import cc.turtl.turtlshell.api.core.Priority
import cc.turtl.turtlshell.api.core.format.ColorLib
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.awt.Color
import java.util.*

// Using the config for this just to provide an easy way for users to edit their own filters.
// Filter definitions are serialized and managed by the UserDataRegistry.
class FilterConfig {

    fun buildCategory(parent: Screen?): ConfigCategory {
        val filtersUserData = ChiselmonStorage.FILTERS[Scope.global()]

        val builder = ConfigCategory.createBuilder()
            .name(Component.translatable("chiselmon.config.category.filters"))

        builder.option(
            ButtonOption.createBuilder()
                .name(
                    Component.translatable("chiselmon.config.filters.create")
                        .withColor(ColorLib.GREEN.rgb)
                )
                .description(
                    OptionDescription.of(
                        Component.translatable("chiselmon.config.filters.create.desc")
                    )
                )
                .text(Component.translatable("chiselmon.config.filters.create.button"))
                .action { _, _ ->
                    val newId = "custom_" + UUID.randomUUID().toString().substring(0, 8)
                    filtersUserData.put(
                        newId, FilterDefinition(
                            newId,
                            DEFAULT_DISPLAY_NAME,
                            DEFAULT_COLOR.rgb,
                            DEFAULT_PRIORITY,
                            DEFAULT_CONDITION_STRING
                        )
                    )
                    saveAndReload(parent)
                }
                .build()
        )

        for (filter in filtersUserData.all.values) {
            builder.group(buildFilterGroup(parent, filtersUserData, filter))
        }

        return builder.build()
    }

    private fun buildFilterGroup(
        parent: Screen?,
        filtersUserData: FiltersUserData,
        filter: FilterDefinition
    ): OptionGroup {
        val isDefault = FilterDefinition.DefaultFilters.all().containsKey(filter.id)
        val filterName = createComponent(filter.displayName, filter.rgb)

        val groupBuilder = OptionGroup.createBuilder()
            .name(filterName)
            .description(
                OptionDescription.of(
                    Component.translatable("chiselmon.config.filters.group.desc")
                )
            )

        if (!isDefault) {
            groupBuilder.option(
                OptionFactory.textField(
                    "chiselmon.config.filters.display_name",
                    filter.displayName,
                    { filter.displayName },
                    {
                        filter.displayName = it
                        saveAndReload(parent)
                    })
            )
        }

        groupBuilder.option(
            OptionFactory.colorPicker(
                "chiselmon.config.filters.color",
                Color(filter.rgb),
                { Color(filter.rgb) },
                {
                    filter.rgb = it.rgb
                    saveAndReload(parent)
                })
        )

        groupBuilder.option(
            OptionFactory.enumCycler(
                "chiselmon.config.filters.priority",
                filter.priority,
                { filter.priority },
                {
                    filter.priority = it
                    ChiselmonStorage.FILTERS.save(Scope.global())
                    FilterMatcher.invalidateCache()
                },
                Priority::class.java
            )
        )

        if (!isDefault) {
            groupBuilder.option(
                Option.createBuilder<String>()
                    .name(Component.translatable("chiselmon.config.filters.condition"))
                    .description(
                        OptionDescription.createBuilder()
                            .text(Component.translatable("chiselmon.config.filters.condition.desc"))
                            .text(Component.empty())
                            .text(
                                Component.translatable("chiselmon.config.filters.condition.syntax.header")
                                    .withStyle { it.withUnderlined(true) })
                            .text(Component.literal("◆ shiny AND type=fire"))
                            .text(Component.literal("◆ legendary OR shiny"))
                            .text(Component.literal("◆ NOT species=skitty AND min_size=1.5"))
                            .text(Component.literal("◆ (shiny OR legendary) AND NOT species=magikarp"))
                            .text(Component.empty())
                            .text(
                                Component.translatable("chiselmon.config.filters.condition.tags.header")
                                    .withStyle { it.withUnderlined(true) })
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.shiny"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.legendary"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.species"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.type"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.gender"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.level"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.size"))
                            .build()
                    )
                    .binding(DEFAULT_CONDITION_STRING, { filter.conditionString }, {
                        filter.conditionString = it.trim()
                        ChiselmonStorage.FILTERS.save(Scope.global())
                        FilterMatcher.invalidateCache()
                    })
                    .controller(StringControllerBuilder::create)
                    .build()
            )

            groupBuilder.option(
                HoldToConfirmButton.builder()
                    .name(
                        Component.translatable("chiselmon.config.filters.delete", filter.displayName)
                            .withColor(ColorLib.RED.rgb)
                    )
                    .description(
                        OptionDescription.of(
                            Component.translatable("chiselmon.config.filters.delete.desc")
                        )
                    )
                    .buttonText(Component.translatable("chiselmon.config.filters.delete.button"))
                    .holdingText(Component.translatable("chiselmon.config.filters.delete.held"))
                    .holdTimeTicks(30)
                    .action { _, _ ->
                        filtersUserData.remove(filter.id)
                        saveAndReload(parent)
                    }
                    .build()
            )
        }

        if (isDefault) groupBuilder.collapsed(true)
        return groupBuilder.build()
    }

    private fun saveAndReload(parent: Screen?) {
        ChiselmonStorage.FILTERS.save(Scope.global())
        FilterMatcher.invalidateCache()
        ChiselmonConfig.saveAndReloadScreen(parent, 2)
    }

    companion object {
        val DEFAULT_COLOR: Color = ColorLib.WHITE
        val DEFAULT_PRIORITY: Priority = Priority.NORMAL
        const val DEFAULT_DISPLAY_NAME = "New Custom Filter"
        const val DEFAULT_CONDITION_STRING = "shiny"
    }
}