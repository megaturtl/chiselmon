package cc.turtl.chiselmon.client.feature.pc.sort

import cc.turtl.chiselmon.core.api.PokemonComparators
import cc.turtl.chiselmon.core.util.modResource
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation

enum class SortMode(
    val id: String,
    val displayName: String,
    val comparator: Comparator<Pokemon?>,
    val showInUI: Boolean
) {
    SIZE("size", "Size", PokemonComparators.SIZE_COMPARATOR, true),
    IVS("ivs", "IVs", PokemonComparators.IVS_COMPARATOR, true),
    LEVEL("level", "Level", PokemonComparators.LEVEL_COMPARATOR, false),
    POKEDEX_NUMBER("pokedex", "Pokédex Number", PokemonComparators.POKEDEX_COMPARATOR, false);

    val tooltipKey: String = "ui.sort.$id"
    val labelKey: String = "sort_$id"
    val icon: ResourceLocation = modResource("textures/gui/pc/pc_button_sort_$id.png")
    val iconReversed: ResourceLocation = modResource("textures/gui/pc/pc_button_sort_${id}_reverse.png")

    fun comparator(reversed: Boolean): Comparator<Pokemon?> =
        if (reversed) comparator.reversed() else comparator

    override fun toString() = displayName
}