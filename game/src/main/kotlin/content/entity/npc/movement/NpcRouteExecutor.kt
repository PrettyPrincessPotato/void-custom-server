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

        target.dialogue?.let(npc::say)

        npc.travelTo(
            destination = location.tile,
            destinationArea = location.area,
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
        moveInternal(
            npc = npc,
            target = target,
            announce = true,
        )
    }

    private fun moveInternal(
        npc: NPC,
        target: NpcRouteTarget,
        announce: Boolean,
    ) {
        if (npc.tile in target.location.area) {
            target.onArrival(npc)
            return
        }

        val route = finder.find(
            context = NpcRouteContext(npc.tile),
            target = target.location,
        ) ?: run {
            // Native executor handles the dialogue if graph routing fails.
            NativeNpcRouteExecutor().move(npc, target)
            return
        }

        val waypoint = route.firstOrNull { it != npc.tile } ?: run {
            target.onArrival(npc)
            return
        }

        if (announce) {
            target.dialogue?.let(npc::say)
        }

        npc.travelTo(
            destination = waypoint,
            destinationArea = null,
            queueName = target.queueName,
        ) {
            if (tile in target.location.area) {
                target.onArrival(this)
            } else {
                moveInternal(
                    npc = this,
                    target = target,
                    announce = false,
                )
            }
        }
    }
}
