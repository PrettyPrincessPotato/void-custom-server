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

        if (location.isAt(npc.tile)) {
            target.onArrival(npc)
            return
        }

        target.dialogue?.let(npc::say)

        npc.travelTo(
            destination = location.tile,
            destinationArea = location.arriveArea,
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

        if (graph.findNpcRoute(
                startTile = context.tile,
                output = route,
                target = { tile -> target.isAt(tile) },
            )
        ) {
            log(context, target, route, "found=true")
            return route
        }

        // No graph node within the arrival radius: relax the radius so the
        // graph walks the long way to the nearest node and the native
        // pathfinder only has the last few tiles. Level-strict, because
        // distanceTo returns -1 across levels and would match stair nodes.
        if (target.area == null) {
            for (radius in NpcLocation.ARRIVE_RADIUS + 1..MAX_FINAL_LEG_RADIUS) {
                if (graph.findNpcRoute(
                        startTile = context.tile,
                        output = route,
                        target = { tile ->
                            tile.level == target.tile.level &&
                                tile.distanceTo(target.tile) <= radius
                        },
                    )
                ) {
                    log(context, target, route, "found=true, relaxedRadius=$radius")
                    return route
                }
            }
        }

        log(context, target, route, "found=false")
        return null
    }

    private fun log(
        context: NpcRouteContext,
        target: NpcLocation,
        route: List<Tile>,
        result: String,
    ) {
        println(
            "NPC route search: " +
                "start=${context.tile}, " +
                "level=${context.level}, " +
                "target=${target.id}, " +
                "targetTile=${target.tile}, " +
                "targetArea=${target.area}, " +
                "navTag=${target.navTag}, " +
                "$result, " +
                "routeLength=${route.size}, " +
                "lastTile=${route.lastOrNull()}",
        )
    }

    companion object {
        // The final native leg must stay inside the pathfinder's 128x128
        // search box; 10 also matches nearestNode's 10-tile snap cap.
        private const val MAX_FINAL_LEG_RADIUS = 10
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
        if (target.location.isAt(npc.tile)) {
            target.onArrival(npc)
            return
        }

        val route = finder.find(
            context = NpcRouteContext(npc.tile),
            target = target.location,
        )

        val waypoint = route?.firstOrNull { it != npc.tile } ?: run {
            // The graph can't get closer than where we already are (no route,
            // or we're standing on the nearest node): the native pathfinder
            // takes the final leg from here — a few tiles, by construction.
            NativeNpcRouteExecutor().move(npc, target)
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
            if (target.location.isAt(tile)) {
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
