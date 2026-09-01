package content.entity.npc.schedule

import content.world.time.HourChangeListener
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script

class ScheduleRegistry : Script, HourChangeListener {
    private val schedules = mutableSetOf<NpcScheduleController>()

    init {
        NpcSchedules.registry = this
        WorldTime.subscribeToHourChanges(this)
    }

    fun register(schedule: NpcScheduleController) {
        schedules += schedule
    }

    fun unregister(schedule: NpcScheduleController) {
        schedules -= schedule
    }

    override fun onHourChanged(previousHour: Int, currentHour: Int) {
        schedules.toList().forEach { schedule ->
            schedule.onHourChanged(currentHour)
        }
    }
}

object NpcSchedules {
    lateinit var registry: ScheduleRegistry
}