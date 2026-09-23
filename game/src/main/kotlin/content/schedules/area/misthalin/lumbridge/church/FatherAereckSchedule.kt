package content.schedules.area.misthalin.lumbridge.church

import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcCloseDoor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.schedules.area.misthalin.lumbridge.BOBS_SPAWN_POINT
import content.schedules.area.misthalin.lumbridge.BOB_PRAY_TIME
import content.schedules.area.misthalin.lumbridge.BOB_SHOP_OPEN_TIME
import content.schedules.area.misthalin.lumbridge.BOTTOM_STAIRS_TILE
import content.schedules.area.misthalin.lumbridge.CHURCH_TILE
import content.schedules.area.misthalin.lumbridge.DOOR_CLOSE_TIME
import content.schedules.area.misthalin.lumbridge.DOOR_TILE_INSIDE
import content.schedules.area.misthalin.lumbridge.DOOR_TILE_OUTSIDE
import content.schedules.area.misthalin.lumbridge.SHOP_CLOSE_HOUR
import content.schedules.area.misthalin.lumbridge.TOP_STAIRS_TILE
import content.schedules.area.misthalin.lumbridge.bob
import content.schedules.area.misthalin.lumbridge.shopDoor
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects

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

private val PREACH_HOUR = 5
private val RING_BELL_HOUR = 12

class FatherAereckSchedule : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()

    init {
        val schedule = NpcScheduleController(
            npcProvider = { fatherAereck },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    PREACH_HOUR,
                    ScheduleAction.Custom {
                        it.say("Preachy preachy")
                    },
                ),
                ScheduleTransition(
                    RING_BELL_HOUR,
                    ScheduleAction.Custom {
                        it.say("Ring-a-ding-ding!")
                    }
                ),
            ),
        )
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