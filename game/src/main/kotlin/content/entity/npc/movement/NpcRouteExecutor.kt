package content.entity.npc.movement

import content.bot.behaviour.navigation.NavigationGraph
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

/**
 * Find the route
 */
interface NpcRouteFinder {
    fun find(
        context: NpcRouteContext,
        target: NpcLocation,
    ): List<Tile>?
}

data class NpcRouteContext(
    val tile: Tile,
    val level: Int = tile.level,
)

/**
 * Execute the route
 */
interface NpcRouteExecutor {
    fun move(npc: NPC, target: NpcRouteTarget)
}

class NativeNpcRouteExecutor : NpcRouteExecutor {
    override fun move(npc: NPC, target: NpcRouteTarget) {
        val location = target.location

        if (npc.tile in location.area) {
            target.onArrival(npc)
            return
        }

        npc.travelTo(
            destination = location.tile,
            destinationArea = location.area,
            dialogue = target.dialogue,
            queueName = target.queueName,
        ) {
            target.onArrival(this)
        }
    }
}

class NpcNavMeshRouteFinder(
    private val graph: NavigationGraph,
) : NpcRouteFinder {

    override fun find(
        context: NpcRouteContext,
        target: NpcLocation,
    ): List<Tile>? {
        val route = mutableListOf<Tile>()

        val found = graph.findNpcRoute(
            startTile = context.tile,
            output = route,
            target = { tile ->
                tile in target.area
            },
        )
        println(
            "NPC route search: " +
                    "start=${context.tile}, " +
                    "level=${context.level}, " +
                    "target=${target.id}, " +
                    "targetTile=${target.tile}, " +
                    "targetArea=${target.area}, " +
                    "navTag=${target.navTag}, " +
                    "found=$found, " +
                    "routeLength=${route.size}, " +
                    "lastTile=${route.lastOrNull()}"
        )

        return route.takeIf { found }
    }
}


class GraphNpcRouteExecutor(
    private val finder: NpcRouteFinder,
) : NpcRouteExecutor {

    override fun move(npc: NPC, target: NpcRouteTarget) {
        if (npc.tile in target.location.area) {
            target.onArrival(npc)
            return
        }

        val route = finder.find(
            context = NpcRouteContext(
                tile = npc.tile,
            ),
            target = target.location,
        ) ?: run {
            println(
                "NPC ${npc.id} failed to find route from ${npc.tile} " +
                        "to ${target.location.id}"
            )
            NativeNpcRouteExecutor().move(npc, target)
            return
        }

        val waypoint = route.firstOrNull { it != npc.tile } ?: run {
            target.onArrival(npc)
            return
        }

        npc.travelTo(
            destination = waypoint,
            destinationArea = target.location.area,
            dialogue = target.dialogue,
            queueName = target.queueName,
        ) {
            move(npc, target)
        }
    }
}