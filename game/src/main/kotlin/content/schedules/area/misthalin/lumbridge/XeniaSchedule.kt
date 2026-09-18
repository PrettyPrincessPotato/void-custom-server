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

private var xenia: NPC? = null
private const val XENIA_STRING_ID = "xenia"

private const val GO_HOME_HOUR = 21
private const val GO_HANGOUT_HOUR = 4

private val XENIA_HANGOUT = Tile(3244, 3198, 0) // Doesn't wander here
private val XENIA_DOOR_OUTSIDE = Tile(3235, 3199, 0)
private val XENIA_DOOR_INSIDE = Tile(3235, 3198, 0)
private val XENIA_STAIRS_BOTTOM = Tile(3237, 3198, 0)
private val XENIA_STAIRS_TOP = Tile(3237, 3195, 1)
private val XENIA_SLEEP_SPOT = Tile(3235, 3197, 1)

private val HOME_DOOR = GameObjects.at(XENIA_DOOR_OUTSIDE).first()

class XeniaSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { xenia },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(XENIA_DOOR_OUTSIDE, "xenia_to_home") {
                            npcOpenDoor(HOME_DOOR, 3)
                            it.travelTo(XENIA_STAIRS_BOTTOM, "xenia_to_bedroom") {
                                tele(XENIA_STAIRS_TOP)
                                walkTo(XENIA_SLEEP_SPOT)
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
                        it.travelTo(XENIA_STAIRS_TOP, "xenia_bed_to_stairs") {
                            tele(XENIA_STAIRS_BOTTOM)
                            it.travelTo(XENIA_DOOR_INSIDE, "xenia_stairs_to_door") {
                                npcOpenDoor(HOME_DOOR, 3)
                                it.travelTo(XENIA_HANGOUT, "xenia_home_to_hangout")
                            }
                        }
                    },
                ),
            ),
        )

        npcSpawn(XENIA_STRING_ID) {
            xenia = this
            this["full_pathfinding"] = true
            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(XENIA_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (xenia == this) {
                xenia = null
            }
        }
    }
}
