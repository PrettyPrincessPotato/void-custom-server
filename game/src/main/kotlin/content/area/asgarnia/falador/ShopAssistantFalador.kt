package content.area.asgarnia.falador

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcLocation
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.NpcRouteTarget
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

private const val ASSISTANT_LEAVE_HOUR = 17
private const val ASSISTANT_RETURN_HOUR = 8
private const val ASSISTANT_STRING_ID = "shop_assistant_falador"


/**
 * Main shopkeep will eventually use these
 * TODO: Move somewhere more central
 */
val TAVERN_AREA = Areas["rising_sun_inn"]
val SHOP_AREA = Areas["falador_general_store"]

val TAVERN_TILE = Tile(2957, 3372)
val SHOP_TILE = Tile(2956, 3389)

private val tavern = NpcLocation(
    id = "rising_sun_inn",
    tile = TAVERN_TILE,
    area = TAVERN_AREA,
    navTag = "rising_sun_inn",
    collision = CollisionStrategies.Indoors,
)

private val shop = NpcLocation(
    id = "falador_general_store",
    tile = SHOP_TILE,
    area = SHOP_AREA,
    navTag = "falador_general_store",
    collision = CollisionStrategies.Indoors,
)
/**
 * End of TODO
 */


class ShopAssistantFalador(graph: NavigationGraph) : Script {
    val useBotNav = false // Set this per NPC since it's sometimes overkill to use the bot's navmesh for not even a chunk over.

    private val routeExecutor: NpcRouteExecutor =
        if (useBotNav) {
            GraphNpcRouteExecutor(
                NpcNavMeshRouteFinder(graph)
            )
        } else {
            NativeNpcRouteExecutor()
        }
    private var shopkeeper: NPC? = null

    init {
        val schedule = NpcScheduleController(
            npcProvider = { shopkeeper },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    ASSISTANT_RETURN_HOUR,
                    ScheduleAction.Travel(
                        NpcRouteTarget(
                            location = shop,
                            queueName = "travel_to_shop",
                            dialogue = "Ugh... I think I drank too much...",
                            onArrival = { npc ->
                                npc.collision = shop.collision
                            },
                        )
                    )
                ),
                ScheduleTransition(
                    ASSISTANT_LEAVE_HOUR,
                    ScheduleAction.Travel(
                        NpcRouteTarget(
                            location = tavern,
                            queueName = "travel_to_tavern",
                            dialogue = "Ahh, finally clock-out time...",
                            onArrival = { npc ->
                                npc.collision = tavern.collision
                            },
                        )
                    )
                )
            )
        )

        npcSpawn(ASSISTANT_STRING_ID) {
            shopkeeper = this
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }

        npcDespawn(ASSISTANT_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (shopkeeper === this) {
                shopkeeper = null
            }
        }
    }
}

