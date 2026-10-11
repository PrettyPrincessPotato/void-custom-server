package content.schedules.area.misthalin.lumbridge.fishing

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
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

private var hankNpc: NPC? = null
private var hankSpawn: Tile? = null
private const val HANK_STRING_ID = "hank"

private const val WAKE_UP_HOUR = 5
private const val GO_FISHING_HOUR = 6
private const val BEHIND_COUNTER_HOUR = 8
private const val CLEAN_UP_HOUR = 17
private const val CLOSE_SHOP_HOUR = 18

private val HANK_STAIRS_BOTTOM = Tile(3192, 3255, 0)
private val HANK_STAIRS_TOP = Tile(3195, 3255, 1)
private val HANK_BEDROOM_DOOR = Tile(3195, 3252, 1)
private val HANK_BEDROOM_DOOR_OUTSIDE = Tile(3195, 3252, 1)
private val HANK_SLEEP_SPOT = Tile(3192, 3254, 1)
private val HANK_FISHING_SPOT = Tile(3258, 3204, 0)

private fun openBedroomDoor() {
    val door = GameObjects.at(HANK_BEDROOM_DOOR).firstOrNull()
    if (door != null) {
        npcOpenDoor(door, 15)
    }
}

class HankSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { hankNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    WAKE_UP_HOUR,
                    ScheduleAction.Custom {
                        openBedroomDoor()
                        it.travelTo(HANK_STAIRS_TOP, "hank_bed_to_stairs") {
                            tele(HANK_STAIRS_BOTTOM)
                            travelTo(hankSpawn!!, "hank_stairs_to_shop") {
                                setSpawnAndWander(it, hankSpawn!!, true)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    GO_FISHING_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(HANK_FISHING_SPOT, "hank_shop_to_fishing") {
                            face(Direction.EAST)
                            anim("fish_rod") // No fishing rod, but does the animation, surprisingly. Doesn't loop animation, though.
                            mode = PauseMode
                        }
                    },
                ),
                ScheduleTransition(
                    BEHIND_COUNTER_HOUR,
                    ScheduleAction.Custom {
                        it.clearAnim()
                        it.travelTo(hankSpawn!!, "hank_fishing_to_shop") {
                            setSpawnAndWander(it, hankSpawn!!, true)
                            it.say("Smells like fish in here.")
                        }
                    },
                ),
                ScheduleTransition(
                    CLEAN_UP_HOUR,
                    ScheduleAction.Custom{
                        it.say("Better start cleaning up...")
                    }
                ),
                ScheduleTransition(
                    CLOSE_SHOP_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("hank_to_bed") {
                            walkToDelay(HANK_STAIRS_BOTTOM)
                            tele(HANK_STAIRS_TOP)
                            walkToDelay(HANK_BEDROOM_DOOR_OUTSIDE)
                            openBedroomDoor()
                            walkToDelay(HANK_SLEEP_SPOT)
                            face(Direction.NORTH)
                            mode = PauseMode
                            it.say("zzz...")
                        }
                    },
                ),
            ),
        )

        npcSpawn(HANK_STRING_ID) {
            hankNpc = this
            hankSpawn = this.tile
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(HANK_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (hankNpc == this) {
                hankNpc = null
            }
        }
    }
}
