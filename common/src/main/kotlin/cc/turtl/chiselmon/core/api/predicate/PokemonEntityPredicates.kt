package cc.turtl.chiselmon.core.api.predicate

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.function.Predicate

@JvmField
val IS_OWNED: Predicate<PokemonEntity> = Predicate { it.ownerUUID != null }

@JvmField
val FROM_POKESNACK: Predicate<PokemonEntity> = Predicate { it.aspects.contains("poke_snack_crumbed") }

@JvmField
val IS_WILD: Predicate<PokemonEntity> = Predicate { entity ->
    when {
        IS_OWNED.test(entity) -> false
        FROM_POKESNACK.test(entity) -> true
        entity.isNoAi -> false
        entity.pokemon.scaleModifier >= 2 -> false
        entity.pokemon.level > 1 -> true
        else -> {
            val maxHealth = entity.getAttribute(Attributes.MAX_HEALTH)
            maxHealth != null && maxHealth.value != maxHealth.baseValue
        }
    }
}