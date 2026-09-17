package content.area.misthalin.tutorial_island

import content.entity.npc.BANK_CLOSE_TIME
import content.entity.npc.BANK_OPEN_TIME
import content.entity.npc.banksOpen
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
import kotlin.collections.set

// This poor NPC's only purpose is to be a pick pocket target.
// In the morning, if this npc was pickpocketted, he should remark that his purse feels lighter.
class PickPocketParker : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
    private val spawnTile = Tile(3121, 3121, 0)
    private var parker: NPC? = null
    private val pickPocketHintLines = arrayOf(
        "I sure hope I don't get pick-pocketted!",
        "Aw gee... The bank's closed, and I've got all this gold in my pockets!",
        "I can't deposit all this gold at this hour!",
        "I mean, I could use the deposit box... But it's so dirty!",
    )

    private fun spawnParker() {
        parker = NPCs.add("man", spawnTile)
        parker!!["full_pathfinding"] = true
    }

    private val schedule = NpcScheduleController(
        npcProvider = { parker },
        routeExecutor = routeExecutor,
        scheduleTransitions = listOf(
            ScheduleTransition(
                BANK_CLOSE_TIME,
                ScheduleAction.Custom {
                    parker!!.say("The bank is closed!")
                },
            ),
            ScheduleTransition(
                BANK_OPEN_TIME,
                ScheduleAction.Custom {
                    parker!!.say("The bank is open!")
                },
            ),
        ),
    )

    init {
        worldSpawn {
            spawnParker()
        }

        npcSpawn("man") {
            if (this.index != parker?.index) {
                return@npcSpawn
            }
            parker!!.softTimers.start("parker_hint_timer")
            NpcSchedules.registry.register(schedule)
        }

        npcDespawn("man") {
            if (this.index != parker?.index) {
                return@npcDespawn
            }
            parker!!.softTimers.stop("parker_hint_timer")
            NpcSchedules.registry.unregister(schedule)
        }

//        npcApproach("Attack", parker!!.id) {
//            say("No")
//        }

        npcTimerStart("parker_hint_timer") {
            TimeUnit.SECONDS.toTicks(7)
        }
        npcTimerTick("parker_hint_timer") {
            if (!banksOpen) {
                parker?.say(pickPocketHintLines.random())
            }
            Timer.CONTINUE
        }
    }
}
