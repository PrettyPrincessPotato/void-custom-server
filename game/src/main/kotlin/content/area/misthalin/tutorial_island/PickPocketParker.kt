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
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
import world.gregs.voidps.engine.queue.queue as enqueue

// This poor NPC's only purpose is to be a pick pocket target.
// In the morning, if this npc was pickpocketted, he should remark that his purse feels lighter.
class PickPocketParker : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
    private val spawnTile = Tile(3121, 3121, 0)
    private val bankTile = Tile(3122, 3123, 0) // Face north
    private var parker: NPC? = null
    private var banker: NPC? = null
    private var pickpocketted = false
    private val bankerTile = Tile(3122, 3125, 0)
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
                    parker!!.enqueue("parker_to_bank") {
                        say("The bank is open!")
                        walkToDelay(bankTile)
                        face(Direction.NORTH)
                        pause(1)
                        mode = PauseMode
                        say("Hello, I'd like to deposit my gold.")
                        pause(3)
                        if (pickpocketted) {
                            pickpocketted = false
                            say("Huh? That's strange, my purse feels lighter than normal.")
                            pause(3)
                            banker?.say("That's what you get for not using the bank deposit.")
                            pause(3)
                            say("It's not my fault they're so filthy!")
                        } else {
                            banker?.say("Of course, just enter your pin please.")
                            pause(3)
                            say("Oh... Uh... What was it again?")
                            pause(3)
                            banker?.say("Oh bother.")
                        }
                        pause(1)
                        mode = EmptyMode
                    }
                },
            ),
        ),
    )

    init {
        worldSpawn {
            spawnParker()
        }

        npcSpawn("banker_tutorial_island") {
            if (this.tile != bankerTile) {
                return@npcSpawn
            }
            banker = this
        }

        npcDespawn("banker_tutorial_island") {
            if (this.tile != bankerTile) {
                return@npcDespawn
            }
            banker = null
        }

        npcSpawn("man") {
            if (this.index != parker?.index) {
                return@npcSpawn
            }
            parker!!.softTimers.start("parker_hint_timer")
            NpcSchedules.registry.register(schedule)
        }

        npcOperate("Pickpocket", "man") {
            if (it.target.index != parker?.index) {
                return@npcOperate
            }
            pickpocketted = true
        }

        npcDespawn("man") {
            if (this.index != parker?.index) {
                return@npcDespawn
            }
            parker!!.softTimers.stop("parker_hint_timer")
            NpcSchedules.registry.unregister(schedule)
        }

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
