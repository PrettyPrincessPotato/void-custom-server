package content.schedules.area.misthalin.lumbridge.church

import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.queue.queue as enqueue

private val SERMON_SAYINGS = arrayOf(
    "O, Saradomin! Bless us with your light!",
    "In Saradomin there is goodness; and that is good.",
    "Hey, are you asleep back there? Careful, Saradomin is watching.",
    "... and so Saradomin covered the land in his holy light, blessing the River Salve to keep the undead at bay...",
    "Our god is the most popular one, and that makes ours the best.",
    "Together with Zamorak and Guthix, Saradomin helped create the very land we walk on today.",
    "The legendary warriors known as the Bandos Brothers served under Saradomin, assisting him in conquering southern Forinthry during the God Wars.",
    "... And the Saradominist wizards were then betrayed by the Zamorakian Cultists, destroying the old wizard's tower in the process...",
    "From the wise owl, to the graceful unicorn. Saradomin's touch can be found everywhere.",
    "Go in peace in the name of Saradomin; may his glory shine upon you like the sun.",
    "Protect your self, protect your friends. Mine is the glory that never ends. This is Saradomin's wisdom.",
    "The darkness in life may be avoided, by the light of wisdom shining. This is Saradomin's wisdom.",
    "Show love to your friends, and mercy to your enemies, and know that the wisdom of Saradomin will follow. This is Saradomin's wisdom.",
    "A fight begun, when the cause is just, will prevail over all others. This is Saradomin's wisdom.",
    "The currency of goodness is honour; It retains its value through scarcity. This is Saradomin's wisdom.",
)

private var fatherAereck: NPC? = null

private val AWAKE_HOUR = 3
private val PREPARE_HOUR = 5
private val PREACH_HOUR = 6
private val PREACH_END_HOUR = 8
private val PREPARE_RING_BELL_HOUR = 11
private val RING_BELL_HOUR = 12
private val GO_HOME_HOUR = 16
private val SLEEP_HOUR = 19

private val SERMON_LOC = Tile(3244, 3205, 0)
private val BOTTOM_OF_LADDER_1 = Tile(3242, 3213, 0)
private val TOP_OF_LADDER_1 = Tile(3240, 3213, 1)
private val BOTTOM_OF_LADDER_2 = Tile(3241, 3207, 1)
private val TOP_OF_LADDER_2 = Tile(3242, 3206, 2)
private val AERECK_DOOR_OUTSIDE = Tile(3235, 3199, 0)
private val AERECK_DOOR_INSIDE = Tile(3235, 3198, 0)
private val AERECK_STAIRS_BOTTOM = Tile(3237, 3198, 0)
private val AERECK_STAIRS_TOP = Tile(3237, 3195, 1)
private val AERECK_SLEEP_SPOT = Tile(3232, 3197, 1)

private val HOME_DOOR = GameObjects.at(AERECK_DOOR_OUTSIDE).first()

class FatherAereckSchedule : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()

    init {
        val schedule = NpcScheduleController(
            npcProvider = { fatherAereck },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    AWAKE_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("aereck_to_church") {
                            say("Hope you find the adventurer, Xenia.")
                            walkToDelay(AERECK_STAIRS_TOP)
                            tele(AERECK_STAIRS_BOTTOM)
                            walkToDelay(AERECK_DOOR_INSIDE)
                            npcOpenDoor(HOME_DOOR, 5)
                            walkToDelay(SERMON_LOC)
                            setSpawnAndWander(it, SERMON_LOC)
                        }
                    },
                ),
                ScheduleTransition(
                    PREPARE_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(SERMON_LOC, "aereck_to_sermon") {
                            say("Hurry up Saradomin waits for nobody")
                            face(Direction.NORTH)
                            mode = PauseMode
                        }
                    },
                ),
                ScheduleTransition(
                    PREACH_HOUR,
                    ScheduleAction.Custom {
                        it.softTimers.start("aereck_preaching")
                    },
                ),
                ScheduleTransition(
                    PREACH_END_HOUR,
                    ScheduleAction.Custom {
                        it.softTimers.stop("aereck_preaching")
                        it.say("Okay get the fuck out of my church now, thanks.")
                        it.mode = EmptyMode
                    },
                ),
                ScheduleTransition(
                    PREPARE_RING_BELL_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("aereck_to_bell") {
                            walkToDelay(BOTTOM_OF_LADDER_1)
                            animDelay("climb_up")
                            pause(1)
                            tele(TOP_OF_LADDER_1)
                            walkToDelay(BOTTOM_OF_LADDER_2)
                            animDelay("climb_up")
                            pause(1)
                            tele(TOP_OF_LADDER_2)
                            setSpawnAndWander(this, TOP_OF_LADDER_2)
                        }
                    },
                ),
                ScheduleTransition(
                    RING_BELL_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("aereck_ring_bell_go_downstairs") {
                            say("Ring-a-ding-ding!") // TODO: Find bell sound
                            walkToDelay(TOP_OF_LADDER_2)
                            animDelay("climb_down")
                            pause(1)
                            tele(BOTTOM_OF_LADDER_2)
                            walkToDelay(TOP_OF_LADDER_1)
                            animDelay("climb_down")
                            pause(1)
                            tele(BOTTOM_OF_LADDER_1)
                            setSpawnAndWander(it, SERMON_LOC)
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("aereck_to_home") {
                            walkToDelay(AERECK_DOOR_OUTSIDE)
                            npcOpenDoor(HOME_DOOR, 5)
                            walkToDelay(AERECK_STAIRS_BOTTOM)
                            tele(AERECK_STAIRS_TOP)
                            setSpawnAndWander(it, AERECK_SLEEP_SPOT)
                        }
                    },
                ),
                ScheduleTransition(
                    SLEEP_HOUR,
                    ScheduleAction.Custom {
                        it.enqueue("aereck_to_bed") {
                            walkToDelay(AERECK_SLEEP_SPOT)
                            mode = PauseMode
                            say("zzz...")
                        }
                    },
                ),
            ),
        )

        npcTimerStart("aereck_preaching") {
            random.nextInt(30, 60)
        }
        npcTimerTick("aereck_preaching") {
            this.say(SERMON_SAYINGS.random())
            Timer.CONTINUE
        }

        npcSpawn("father_aereck") {
            fatherAereck = this
            this["full_pathfinding"] = true
            this.collision = CollisionStrategies.Normal

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn("father_aereck") {
            NpcSchedules.registry.unregister(schedule)

            if (fatherAereck == this) {
                fatherAereck = null
            }
        }
    }
}
