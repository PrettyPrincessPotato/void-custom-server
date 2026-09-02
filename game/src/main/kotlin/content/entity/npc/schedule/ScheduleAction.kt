package content.entity.npc.schedule

import content.entity.npc.movement.NpcRouteTarget
import world.gregs.voidps.engine.entity.character.npc.NPC

sealed interface ScheduleAction {
    data class Travel(
        val target: NpcRouteTarget
    ) : ScheduleAction

    data class Custom(
        val handler: (NPC) -> Unit
    ) : ScheduleAction
}