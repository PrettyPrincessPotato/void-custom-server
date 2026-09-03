package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

/**
 *
 */
data class NpcLocation(
    val id: String,
    val tile: Tile,
    val area: Area,
    val navTag: String? = null,
    val collision: CollisionStrategy = CollisionStrategies.Normal
)