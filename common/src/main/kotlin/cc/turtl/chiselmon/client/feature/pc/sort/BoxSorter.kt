package cc.turtl.chiselmon.client.feature.pc.sort

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import cc.turtl.chiselmon.client.feature.eggspy.EggDummy
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.client.storage.ClientPC
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon

object BoxSorter {
    fun sortPCBox(pc: ClientPC, boxIndex: Int, mode: SortMode, reversed: Boolean) {
        val box = pc.boxes[boxIndex]
        val allSlots = box.slots.toMutableList()
        val pokemon = allSlots.filterNotNull()

        val previews = pokemon.associateWith { (it as DuckPreviewPokemon).`chiselmon$getPreview`() }
        val sorted = pokemon.sortedWith(createComparator(mode, reversed, previews))

        val positions = HashMap<Pokemon, Int>()
        allSlots.forEachIndexed { i, p -> if (p != null) positions[p] = i }

        for ((targetIdx, targetPkm) in sorted.withIndex()) {
            val currentIdx = positions[targetPkm]!!
            if (currentIdx == targetIdx) continue

            val displaced = allSlots[targetIdx]
            sendPacket(boxIndex, targetPkm, currentIdx, displaced, targetIdx)

            allSlots[currentIdx] = displaced
            allSlots[targetIdx] = targetPkm

            positions[targetPkm] = targetIdx
            if (displaced != null) positions[displaced] = currentIdx
        }
    }

    private fun sendPacket(box: Int, target: Pokemon, from: Int, displaced: Pokemon?, to: Int) {
        val source = PCPosition(box, from)
        val destination = PCPosition(box, to)

        if (displaced == null) {
            MovePCPokemonPacket(target.uuid, source, destination).sendToServer()
        } else {
            SwapPCPokemonPacket(target.uuid, source, displaced.uuid, destination).sendToServer()
        }
    }

    private fun createComparator(
        mode: SortMode,
        reversed: Boolean,
        previews: Map<Pokemon, Pokemon?>
    ): Comparator<Pokemon> = Comparator { a, b ->
        val pa = previews.preview(a)
        val pb = previews.preview(b)
        val aIsEgg = pa is EggDummy
        val bIsEgg = pb is EggDummy

        when {
            // One egg: Eggs go last
            aIsEgg != bIsEgg -> aIsEgg.compareTo(bIsEgg)
            // Both eggs: sort by hatch percentage, then mode
            aIsEgg && bIsEgg -> {
                pa.hatchPercentage.compareTo(pb.hatchPercentage)
                    .takeIf { it != 0 } ?: mode.comparator(reversed).compare(pa, pb)
            }
            // No eggs: sort by mode, then level
            else -> mode.comparator(reversed).compare(pa, pb)
                .takeIf { it != 0 } ?: pa.level.compareTo(pb.level)
        }
    }
}

private fun Map<Pokemon, Pokemon?>.preview(p: Pokemon): Pokemon = this[p] ?: p