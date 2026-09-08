package content.entity.npc

import content.entity.gfx.areaGfx
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script

const val BANK_CLOSE_TIME = 20
const val BANK_OPEN_TIME = 5
const val BANKER_STRING_ID = "banker*" // NPC Schedules doesn't appear to support regex? Multiple NPCs? Not sure what's going on here.

private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
var banksOpen = true // Always starts true, server starts at noon.

class BankerSchedule : Script {
    init {
        npcSpawn(BANKER_STRING_ID) {
            val banker = this

            val schedule = NpcScheduleController(
                npcProvider = { banker },
                routeExecutor = routeExecutor,
                scheduleTransitions = listOf(
                    ScheduleTransition(
                        BANK_CLOSE_TIME,
                        ScheduleAction.Custom {
                            banksOpen = false
                            areaGfx("imp_puff", banker.tile)
                            banker.hide = true
                        },
                    ),
                    ScheduleTransition(
                        BANK_OPEN_TIME,
                        ScheduleAction.Custom {
                            banksOpen = true
                            areaGfx("imp_puff", banker.tile)
                            banker.hide = false
                        },
                    ),
                ),
            )
            NpcSchedules.registry.register(schedule)
        }
    }
}
