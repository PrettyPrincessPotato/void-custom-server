package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcCloseDoor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

private const val SHOP_CLOSE_HOUR = 18
private const val BOB_PRAY_TIME = 5
private const val BOB_SHOP_OPEN_TIME = 8

private val DOOR_TILE_INSIDE = Tile(3233, 3203, 0)
private val DOOR_TILE_OUTSIDE = Tile(3234, 3203, 0)
private val BOTTOM_STAIRS_TILE = Tile(3232, 3205, 0)
private val TOP_STAIRS_TILE = Tile(3229, 3205, 1)
private val BOBS_SPAWN_POINT = Tile(3228, 3203, 0)
private val CHURCH_TILE = Tile(3243, 3209, 0)
private val shopDoor = GameObjects.at(DOOR_TILE_INSIDE).first()
private val DOOR_CLOSE_TIME = TimeUnit.HOURS.toTicks(2)

private var bob: NPC? = null

class BobSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { bob },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    SHOP_CLOSE_HOUR,
                    ScheduleAction.Custom {
                        it.say("Time to close up.")
                        it.travelTo(destination = DOOR_TILE_INSIDE, queueName = "bob_to_close_door") {
                            npcCloseDoor(shopDoor, DOOR_CLOSE_TIME, 0, 3)
                            it.travelTo(BOTTOM_STAIRS_TILE, queueName = "bob_door_to_stairs") {
                                tele(TOP_STAIRS_TILE)
                                setSpawnAndWander(it, TOP_STAIRS_TILE)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    BOB_SHOP_OPEN_TIME,
                    ScheduleAction.Custom {
                        it.travelTo(destination = DOOR_TILE_OUTSIDE, queueName = "bob_to_close_door") {
                            val shopDoorClosed = GameObjects.at(DOOR_TILE_OUTSIDE).first()
                            it.say("Another day another coin.")
                            npcOpenDoor(shopDoorClosed, DOOR_CLOSE_TIME)
                            it.travelTo(BOBS_SPAWN_POINT, queueName = "bob_door_to_desk") {
                                it.collision = CollisionStrategies.Indoors
                                setSpawnAndWander(it, BOBS_SPAWN_POINT)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    BOB_PRAY_TIME,
                    ScheduleAction.Custom {
                        val shopDoorClosed = GameObjects.at(DOOR_TILE_OUTSIDE).first()
                        it.travelTo(TOP_STAIRS_TILE, queueName = "bob_bed_to_stairs") {
                            tele(BOTTOM_STAIRS_TILE)
                            it.travelTo(DOOR_TILE_INSIDE, queueName = "bob_stairs_to_door") {
                                npcOpenDoor(shopDoorClosed, 5)
                                it.travelTo(CHURCH_TILE, queueName = "bob_to_church") {
                                    it.collision = CollisionStrategies.Indoors
                                    setSpawnAndWander(it, CHURCH_TILE)
                                }
                            }
                        }
                    },
                ),
            ),
        )
        npcSpawn("bob") {
            bob = this
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn("bob") {
            NpcSchedules.registry.unregister(schedule)

            if (bob == this) {
                bob = null
            }
        }
    }
}
