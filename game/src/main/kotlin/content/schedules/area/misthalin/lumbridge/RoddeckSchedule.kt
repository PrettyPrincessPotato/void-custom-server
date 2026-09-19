package content.schedules.area.misthalin.lumbridge

import content.entity.npc.movement.NativeNpcRouteExecutor
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
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

var roddeckNpc: NPC? = null
private const val RODDECK_STRING_ID = "roddeck"

private const val WAKE_UP_HOUR = 8
private const val FEED_DUCKS_HOUR = 10
private const val MUSIC_HOUR = 14
private const val GO_HOME_HOUR = 18
private const val GO_TO_BED_HOUR = 21

private val RODDECK_SPAWN = Tile(3232, 3237, 0)
private val RODDECK_DOOR_INSIDE = Tile(3230, 3236, 0)
private val RODDECK_DOOR_OUTSIDE = Tile(3230, 3235, 0)
private val RODDECK_DUCK_LOC = Tile(3235, 3241, 0)
private val RODDECK_MUSIC_LOC = Tile(3233, 3235, 0)
private val RODDECK_STAIRS_BOTTOM = Tile(3232, 3238, 0)
private val RODDECK_STAIRS_TOP = Tile(3232, 3241, 1)
private val RODDECK_BEDROOM_DOOR_OUTSIDE = Tile(3230, 3239, 1)
private val RODDECK_BEDROOM_DOOR_INSIDE = Tile(3230, 3238, 1)
private val RODDECK_SLEEP_LOC = Tile(3230, 3236, 1) // Face north

private val HOME_DOOR = GameObjects.at(RODDECK_DOOR_OUTSIDE).first()
private val BEDROOM_DOOR = GameObjects.at(RODDECK_BEDROOM_DOOR_INSIDE).first()

class RoddeckSchedule : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
    init {
        val schedule = NpcScheduleController(
            npcProvider = { roddeckNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    FEED_DUCKS_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(RODDECK_DOOR_INSIDE, "roddeck_spawn_to_door") {
                            npcOpenDoor(HOME_DOOR, 7)
                            it.travelTo(RODDECK_DUCK_LOC, "roddeck_door_to_ducks") {
                                it.say("Ah... The ducks are so lovely this time of day.")
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    MUSIC_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(RODDECK_MUSIC_LOC, "roddeck_ducks_to_music") {
                            it.say("Music sounds wonderful, as always.")
                            it.face(Direction.SOUTH)
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(RODDECK_DOOR_OUTSIDE, "roddeck_music_to_door") {
                            npcOpenDoor(HOME_DOOR, 7)
                            it.travelTo(RODDECK_SPAWN, "roddeck_door_to_spawn") {
                                it.face(Direction.WEST)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    GO_TO_BED_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(RODDECK_STAIRS_BOTTOM, "roddeck_spawn_to_stairs") {
                            tele(RODDECK_STAIRS_TOP)
                            it.travelTo(RODDECK_BEDROOM_DOOR_OUTSIDE, "roddeck_stairs_to_bed") {
                                npcOpenDoor(BEDROOM_DOOR, 7)
                                it.travelTo(RODDECK_SLEEP_LOC, "roddeck_door_to_bed") {
                                    it.face(Direction.NORTH)
                                }
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    WAKE_UP_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(RODDECK_BEDROOM_DOOR_INSIDE, "roddeck_bed_to_door") {
                            npcOpenDoor(BEDROOM_DOOR, 7)
                            it.travelTo(RODDECK_STAIRS_TOP, "roddeck_bed_to_stairs") {
                                tele(RODDECK_STAIRS_BOTTOM)
                                it.travelTo(RODDECK_SPAWN, "roddeck_stairs_to_spawn") {
                                    it.face(Direction.WEST)
                                }
                            }
                        }
                    },
                ),
            ),
        )

        npcSpawn(RODDECK_STRING_ID) {
            roddeckNpc = this
            this["full_pathfinding"] = true
            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(RODDECK_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (roddeckNpc == this) {
                roddeckNpc = null
            }
        }
    }
}
