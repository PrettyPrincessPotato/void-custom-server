package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

private var milkSellerNpc: NPC? = null
private const val MILK_SELLER_STRING_ID = "milk_seller"
private var milkSellerSpawnTile: Tile? = null

private const val TRAVEL_HOUR = 5

private val LUMBRIDGE = Tile(3221, 3219, 0)
private val VARROCK = Tile(3209, 3435, 0)
private val FALADOR = Tile(2968, 3377, 0)
private val PORT_SARIM = Tile(3043, 3256, 0)
private val RIMMINGTON = Tile(2959, 3218, 0)

private val SELL_LOCATIONS = arrayOf(
    LUMBRIDGE,
    VARROCK,
    FALADOR,
    PORT_SARIM,
    RIMMINGTON,
)


private var routeIndex = 0

class MilkSellerSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { milkSellerNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    TRAVEL_HOUR,
                    ScheduleAction.Custom {
                        travelSomewhereNew(it)
                    }
                ),
            )
        )

        npcSpawn(MILK_SELLER_STRING_ID) {
            milkSellerNpc = this
            milkSellerSpawnTile = this.tile
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(MILK_SELLER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (milkSellerNpc == this) {
                milkSellerNpc = null
            }
        }
    }
}

private fun travelSomewhereNew(npc: NPC) {
    npc.say("Well Bessie, time we headed out.")
    val destination = nextSellingLocation()
    when(destination){
        LUMBRIDGE -> travelToLumbridge(npc)
        VARROCK -> npc.say("I'd travel to $destination")
        FALADOR -> npc.say("I'd travel to $destination")
        PORT_SARIM -> npc.say("I'd travel to $destination")
        RIMMINGTON -> npc.say("I'd travel to $destination")
    }
}

private fun nextSellingLocation(): Tile {
    val destination = SELL_LOCATIONS[routeIndex]

    routeIndex = (routeIndex + 1) % SELL_LOCATIONS.size

    return destination
}

private fun travelToLumbridge(npc: NPC) {
    npc.travelTo(LUMBRIDGE, "spawn_to_lumbridge")
}