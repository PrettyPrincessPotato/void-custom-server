package content.entity.npc.schedule

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

sealed interface ScheduleAction {
    data class Travel(
        val destination: Tile,
        val area: Area,
        val dialogue: String,
        val queueName: String,
        val onArrival: (NPC) -> Unit = {}
    ) : ScheduleAction

    data class Custom(
        val handler: (NPC) -> Unit
    ) : ScheduleAction
}