package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

private val LACHTOPHER_HOME = Tile(3231, 3207, 1)
private val LACHTOPHER_HANGOUT = Tile(3227, 3237, 0)
private const val GO_HOME_HOUR = 20
private const val GO_HANGOUT_HOUR = 10
private const val LACHTOPHER_STRING_ID = "lachtopher"

private var lachtopher: NPC? = null

private val DOWNSTAIRS = Tile(3232, 3209, 0)
private val UPSTAIRS = Tile(3229, 3209, 1)
private val EAST_TILE = Tile(3234, 3207, 0)
private val WEST_TILE = Tile(3233, 3207, 0)
private val HOME_DOOR = GameObjects.at(EAST_TILE).first()

class LachtopherSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { lachtopher },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.say("Ugh, I don't walk all the way home.")
                        it.travelTo(EAST_TILE, "lachtopher_to_door") {
                            npcOpenDoor(HOME_DOOR, 100)
                            travelTo(DOWNSTAIRS, "lachtopher_to_stairs_bottom") {
                                tele(UPSTAIRS)
                                walkTo(LACHTOPHER_HOME)
                                setSpawnAndWander(it, LACHTOPHER_HOME)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HANGOUT_HOUR,
                    ScheduleAction.Custom {
                        if (it.tile.level == 0) {
                            return@Custom
                        }
                        it.say("Ugh, I don't want to walk all the way there.")
                        it.travelTo(UPSTAIRS, "lachtopher_to_stairs_top") {
                            tele(DOWNSTAIRS)
                            travelTo(WEST_TILE, "lachtopher_stairs_to_door") {
                                npcOpenDoor(HOME_DOOR, 100)
                                travelTo(LACHTOPHER_HANGOUT, "lachtopher_home_to_hangout") {
                                    setSpawnAndWander(it, LACHTOPHER_HANGOUT)
                                }
                            }
                        }
                    },
                ),
            ),
        )

        npcSpawn(LACHTOPHER_STRING_ID) {
            lachtopher = this
            this["full_pathfinding"] = true
            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(LACHTOPHER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (lachtopher == this) {
                lachtopher = null
            }
        }
    }
}
