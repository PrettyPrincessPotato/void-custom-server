package content.area.misthalin.tutorial_island

import content.entity.npc.BANK_CLOSE_TIME
import content.entity.npc.BANK_OPEN_TIME
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

// This poor NPC's only purpose is to be a pick pocket target.
// In the morning, if this npc was pickpocketted, he should remark that his purse feels lighter.
class PickPocketParker : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
    private val spawnTile = Tile(3121, 3121, 0)



    init {
        worldSpawn {
            var parker: NPC = NPCs.add("man", spawnTile)
            parker["full_pathfinding"] = true

            val schedule = NpcScheduleController(
                npcProvider = { parker },
                routeExecutor = routeExecutor,
                scheduleTransitions = listOf(
                    ScheduleTransition(
                        BANK_CLOSE_TIME,
                        ScheduleAction.Custom {
                            parker.say("I sure hope I don't get pick-pocketted!")
                        }
                    ),
                    ScheduleTransition(
                        BANK_OPEN_TIME,
                        ScheduleAction.Custom {
                            parker.say("The bank is open!")
                        }
                    )
                )
            )

            NpcSchedules.registry.register(schedule)
        }
    }
}