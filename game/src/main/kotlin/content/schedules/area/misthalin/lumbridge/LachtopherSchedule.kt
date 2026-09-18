package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

private val LACHTOPHER_HOME = Tile(3231, 3207, 1)
private val LACHTOPHER_HANGOUT = Tile(3227, 3237, 0)
private const val GO_HOME_HOUR = 20
private const val GO_HANGOUT_HOUR = 10
private const val LACHTOPHER_STRING_ID = "lachtopher"
private var LACHTOPHER: NPC? = null

class LachtopherSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val downstairs = Tile(3232, 3209, 0)
        val upstairs = Tile(3229, 3209, 1)
        val eastTile = Tile(3234, 3207, 0)
        val westTile = Tile(3233, 3207, 0)
        val homeDoor = GameObjects.at(eastTile).first()

        val schedule = NpcScheduleController(
            npcProvider = { LACHTOPHER },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.say("Ugh, I don't walk all the way home.")
                        it.travelTo(eastTile, null, "lachtopher_to_door") {
                            npcOpenDoor(homeDoor, 30)
                            travelTo(downstairs, null, "lachtopher_to_stairs_bottom") {
                                tele(upstairs)
                                walkTo(LACHTOPHER_HOME)
                                it["spawn_tile"] = LACHTOPHER_HOME
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HANGOUT_HOUR,
                    ScheduleAction.Custom {
                        if(it.tile.level == 0){
                            return@Custom
                        }
                        it.say("Ugh, I don't want to walk all the way there.")
                        it.travelTo(upstairs, null, "lachtopher_to_stairs_top") {
                            tele(downstairs)
                            travelTo(westTile, null, "lachtopher_stairs_to_door") {
                                npcOpenDoor(homeDoor, 30)
                                travelTo(LACHTOPHER_HANGOUT, null, "lachtopher_home_to_hangout") {
                                    it["spawn_tile"] = LACHTOPHER_HANGOUT
                                }
                            }
                        }
                    },
                ),
            ),
        )

        npcSpawn(LACHTOPHER_STRING_ID) {
            LACHTOPHER = this
            this["full_pathfinding"] = true
            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(LACHTOPHER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if(LACHTOPHER == this){
                LACHTOPHER = null
            }
        }
    }
}