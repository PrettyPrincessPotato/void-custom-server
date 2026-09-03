package content.entity.npc.schedule

import content.entity.npc.movement.NpcRouteExecutor
import world.gregs.voidps.engine.entity.character.npc.NPC

class NpcScheduleController(
    private val npcProvider: () -> NPC?,
    private val routeExecutor: NpcRouteExecutor,
    scheduleTransitions: List<ScheduleTransition>
) {
    private val transitions = scheduleTransitions.sortedBy { it.hour }
    private var lastTransition = -1

    fun onHourChanged(hour: Int) {
        val transition = transitions
            .filter { it.hour <= hour }
            .maxByOrNull { it.hour } ?: return

        if (transition.hour == lastTransition) return
        lastTransition = transition.hour

        when (val action = transition.action) {
            is ScheduleAction.Travel -> {
                val npc = npcProvider() ?: return
                routeExecutor.move(npc, action.target)
            }
            is ScheduleAction.Custom -> {
                val npc = npcProvider() ?: return
                action.handler(npc)
            }
        }
    }
}