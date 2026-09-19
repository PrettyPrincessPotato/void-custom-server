package content.schedules.area.misthalin.lumbridge

import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.queue.queue as enqueue
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

private var donieNpc: NPC? = null
private var donieSpawnTile: Tile? = null
private const val DONIE_STRING_ID = "donie"

private const val WAKE_UP_HOUR = 9
private const val ADVENTURE_HOUR = 11
private const val GO_HOME_HOUR = 19
private const val GO_TO_BED_HOUR = 22

private val DONIE_DOOR_INSIDE = Tile(3229, 3240, 0)
private val DONIE_DOOR_OUTSIDE = Tile(3228, 3240, 0)
private val RODDECK_STAIRS_BOTTOM = Tile(3232, 3238, 0)
private val RODDECK_STAIRS_TOP = Tile(3232, 3241, 1)
private val RODDECK_BEDROOM_DOOR_OUTSIDE = Tile(3230, 3239, 1)
private val RODDECK_BEDROOM_DOOR_INSIDE = Tile(3230, 3238, 1)
private val DONIE_SLEEP_LOC = Tile(3231, 3237, 1) // Face west
private val DONIE_SIDE_DOOR_OUTSIDE = Tile(3229, 3240, 1)
private val DONIE_SIDE_DOOR_INSIDE = Tile(3228, 3240, 1)
private val DONIE_INDOOR_HANGOUT_LOC = Tile(3230, 3239, 0)

private val HOME_DOOR = GameObjects.at(DONIE_DOOR_OUTSIDE).first()
private val BEDROOM_DOOR = GameObjects.at(RODDECK_BEDROOM_DOOR_INSIDE).first()
private val SIDE_DOOR = GameObjects.at(DONIE_SIDE_DOOR_INSIDE).first()

class DonieSchedule : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()

    init {
        val schedule = NpcScheduleController(
            npcProvider = { donieNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    WAKE_UP_HOUR,
                    ScheduleAction.Custom {
                        travelToSideRoom(it)
                    }
                ),
                ScheduleTransition(
                    ADVENTURE_HOUR,
                    ScheduleAction.Custom {
                        returnToSpawnTile(it)
                    }
                ),
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        travelHome(it)
                    }
                ),
                ScheduleTransition(
                    GO_TO_BED_HOUR,
                    ScheduleAction.Custom {
                        goToBed(it)
                    }
                ),
            )
        )

        npcSpawn(DONIE_STRING_ID) {
            donieNpc = this
            donieSpawnTile = this.tile
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(DONIE_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (donieNpc == this) {
                donieNpc = null
            }
        }
    }
}

private fun travelToSideRoom(npc: NPC) {
    npc.travelTo(RODDECK_BEDROOM_DOOR_INSIDE, "donie_bed_to_door") {
        npcOpenDoor(BEDROOM_DOOR, 3)
        travelTo(DONIE_SIDE_DOOR_OUTSIDE, "donie_bed_to_side") {
            npcOpenDoor(SIDE_DOOR, 3)
            travelTo(DONIE_SIDE_DOOR_INSIDE, "donie_walk_in_side_room") {
                setSpawnAndWander(npc, DONIE_SIDE_DOOR_INSIDE)
            }
        }
    }
}

private fun returnToSpawnTile(npc: NPC) {
    if (npc.tile.level == 0) {
        return
    }
    npc.travelTo(DONIE_SIDE_DOOR_INSIDE, "donie_leave_side_room") {
        npcOpenDoor(SIDE_DOOR, 3)
        travelTo(RODDECK_STAIRS_TOP, "donie_side_room_to_stairs") {
            tele(RODDECK_STAIRS_BOTTOM)
            travelTo(DONIE_DOOR_INSIDE, "donie_stairs_to_out") {
                npcOpenDoor(HOME_DOOR, 3)
                travelTo(donieSpawnTile!!, "donie_to_spawn") {
                    setSpawnAndWander(npc, donieSpawnTile!!)
                }
            }
        }
    }
}

private fun travelHome(npc: NPC) {
    npc.travelTo(DONIE_DOOR_OUTSIDE, "donie_to_home") {
        npcOpenDoor(HOME_DOOR, 3)
        travelTo(DONIE_INDOOR_HANGOUT_LOC, "donie_walk_indoors") {
            setSpawnAndWander(npc, DONIE_INDOOR_HANGOUT_LOC, true)
            enqueue("welcome_home_donie_roddeck") {
                say("Hello, father!")
                pause(3)
                roddeckNpc!!.say("Who're you?")
                pause(3)
                say("Har, har, har. Love you too.")
                pause(5)
                roddeckNpc!!.say("And I love you.")
            }
        }
    }
}

private fun goToBed(npc: NPC) {
    npc.say("I should turn in...")
    npc.travelTo(RODDECK_STAIRS_BOTTOM, "donie_indoors_to_upstairs") {
        tele(RODDECK_STAIRS_TOP)
        travelTo(RODDECK_BEDROOM_DOOR_OUTSIDE, "donie_stairs_to_bedroom") {
            npcOpenDoor(BEDROOM_DOOR, 3)
            travelTo(DONIE_SLEEP_LOC, "donie_to_bed") {
                face(Direction.WEST)
                mode = PauseMode
            }
        }
    }
}