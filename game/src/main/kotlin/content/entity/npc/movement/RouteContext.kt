package content.entity.npc.movement

import world.gregs.voidps.type.Tile

data class RouteContext(
    val tile: Tile,
    val level: Int,
    val isBot: Boolean
)

interface RouteFinder {
    fun find(context: RouteContext, target: NpcLocation): List<Int>?
}
