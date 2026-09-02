package content.entity.npc.movement

import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

data class NpcLocation(
    val id: String,
    val tile: Tile,
    val area: Area,
    val navTag: String? = null,
)