package content.entity.npc.routine
interface HourlyRoutine {
    fun isActive(): Boolean
    fun onHourChanged(previousHour: Int, currentHour: Int)
}

object NpcDailyRoutines {
    private val routines = mutableListOf<HourlyRoutine>()

    fun onHourChanged(previousHour: Int, currentHour: Int) {
        routines.removeIf { routine ->
            if (!routine.isActive()) {
                true
            } else {
                routine.onHourChanged(previousHour, currentHour)
                false
            }
        }
    }
}
