package content.entity.npc.schedule

data class ScheduleTransition(
    val hour: Int,
    val action: ScheduleAction
)