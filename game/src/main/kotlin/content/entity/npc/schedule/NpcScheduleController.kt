package content.entity.npc.schedule

class NpcScheduleController(
    scheduleTransitions: List<ScheduleTransition>
) {
    private val transitions: List<ScheduleTransition> =
        scheduleTransitions.sortedBy { transition -> transition.hour }

    private var lastTransition = -1

    fun onHourChanged(hour: Int) {
        val index = transitions.indexOfLast { transition ->
            transition.hour <= hour
        }

        if (index < 0 || index == lastTransition) {
            return
        }

        lastTransition = index
        transitions[index].action()
    }
}