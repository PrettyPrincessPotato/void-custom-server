package content.entity.npc

import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.GameObject

private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
private val bankBooths = mutableSetOf<GameObject>()



class BankSchedule : Script {
    init {
        objectSpawn("bank_booth_*") {
            bankBooths += this
        }

        val schedule = NpcScheduleController(
            npcProvider = { null },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    BANK_CLOSE_TIME,
                    ScheduleAction.Custom {
                        banksOpen = false
                    }
                ),
                ScheduleTransition(
                    BANK_OPEN_TIME,
                    ScheduleAction.Custom {
                        banksOpen = true
                    }
                )
            )
        )

        NpcSchedules.registry.register(schedule)
    }
}
