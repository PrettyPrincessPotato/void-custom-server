package content.entity.npc.schedule

import content.entity.npc.movement.travelTo
import world.gregs.voidps.engine.entity.character.npc.NPC

class NpcScheduleController(
    private val npcProvider: () -> NPC?,
    scheduleTransitions: List<ScheduleTransition>
) {
    private val transitions: List<ScheduleTransition> =
        scheduleTransitions.sortedBy { transition -> transition.hour }

    private var lastTransition = -1

    fun onHourChanged(hour: Int) {
        val transition = transitions.filter { it.hour <= hour }.maxByOrNull { it.hour } ?: return
        if (transition.hour == lastTransition) return
        lastTransition = transition.hour

        val npc = npcProvider() ?: return
        when (val action = transition.action) {
            is ScheduleAction.Travel -> {
                if (npc.tile in action.area) {
                    action.onArrival(npc)
                    return
                }
                npc.travelTo(
                    destination = action.destination,
                    destinationArea = action.area,
                    dialogue = action.dialogue,
                    queueName = action.queueName,
                ) {
                    action.onArrival(npc)
                }
            }
            is ScheduleAction.Custom -> action.handler(npc)
        }
    }
}