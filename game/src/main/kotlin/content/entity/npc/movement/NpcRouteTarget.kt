package content.entity.npc.movement

import world.gregs.voidps.engine.entity.character.npc.NPC

data class NpcRouteTarget(
    val location: NpcLocation,
    val queueName: String,
    val dialogue: String? = null,
    val onArrival: (NPC) -> Unit = {},
)