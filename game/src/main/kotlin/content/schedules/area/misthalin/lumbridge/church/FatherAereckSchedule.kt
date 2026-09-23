package content.schedules.area.misthalin.lumbridge.church

import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

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

private var fatherAereck : NPC? = null

private val AWAKE_HOUR = 3
private val PREPARE_HOUR = 5
private val PREACH_HOUR = 6
private val PREACH_END_HOUR = 8
private val RING_BELL_HOUR = 12
private val SLEEP_HOUR = 19

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
                        it.say("Wakie wakie eggs and bakie")
                    }
                ),
                ScheduleTransition(
                    PREPARE_HOUR,
                    ScheduleAction.Custom {
                        it.say("Hurry up Saradomin waits for nobody")
                    }
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
                        it.say("Okay get the fuck out of my church now thanks")
                    }
                ),
                ScheduleTransition(
                    RING_BELL_HOUR,
                    ScheduleAction.Custom {
                        it.say("Ring-a-ding-ding!")
                    }
                ),
                ScheduleTransition(
                    SLEEP_HOUR,
                    ScheduleAction.Custom {
                        it.say("zzz... Not to be confused with zenless zone zero")
                    }
                ),
            ),
        )

        npcTimerStart("aereck_preaching") {
            random.nextInt(30, 150)
        }
        npcTimerTick("aereck_preaching") {
            this.say(SERMON_SAYINGS.random())
            Timer.CONTINUE
        }

        npcSpawn("father_aereck") {
            fatherAereck = this
            this["full_pathfinding"] = true

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