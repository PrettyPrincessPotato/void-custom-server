package content.entity.npc.movement

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.navigation.NavigationGraph
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

/**
 * One segment of a graph route: the tiles to walk in order (ending at the segment's
 * end tile), plus the edge's actions, which run before the segment is walked.
 */
data class NpcRouteSegment(
    val tiles: List<Tile>,
    val actions: List<BotAction>,
)

/**
 * Find the route
 */
interface NpcRouteFinder {
    fun find(
        context: NpcRouteContext,
        target: NpcLocation,
    ): List<NpcRouteSegment>?
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
    ): List<NpcRouteSegment>? {
        val route = mutableListOf<NpcRouteSegment>()

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
            for (radius in NpcLocation.ARRIVE_RADIUS + 1..MAX_FINAL_SEGMENT_RADIUS) {
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
        route: List<NpcRouteSegment>,
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
                "lastTile=${route.lastOrNull()?.tiles?.lastOrNull()}",
        )
    }

    companion object {
        // The final native segment must stay inside the pathfinder's 128x128
        // search box; 10 also matches nearestNode's 10-tile snap cap.
        private const val MAX_FINAL_SEGMENT_RADIUS = 10
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

        val segments = finder.find(
            context = NpcRouteContext(npc.tile),
            target = target.location,
        )

        if (segments.isNullOrEmpty()) {
            // No graph route from here (or we're standing on the nearest node):
            // the native pathfinder takes the final segment from here — a few
            // tiles, by construction.
            NativeNpcRouteExecutor().move(npc, target)
            return
        }

        if (announce) {
            target.dialogue?.let(npc::say)
        }

        walkSegments(npc, target, segments)
    }

    /**
     * Run the segment's edge actions (door/gate opens — the toml lists them as
     * the edge's first step), then walk the segment's tiles in order.
     */
    private fun walkSegments(
        npc: NPC,
        target: NpcRouteTarget,
        segments: List<NpcRouteSegment>,
    ) {
        val segment = segments.first()

        npc.executeEdgeActions(segment.actions)

        walkTiles(npc, target, segments, segment.tiles)
    }

    private fun walkTiles(
        npc: NPC,
        target: NpcRouteTarget,
        segments: List<NpcRouteSegment>,
        tiles: List<Tile>,
    ) {
        npc.travelTo(
            destination = tiles.first(),
            destinationArea = null,
            queueName = target.queueName,
        ) {
            if (tiles.size > 1) {
                walkTiles(this, target, segments, tiles.drop(1))
            } else if (segments.size > 1) {
                walkSegments(this, target, segments.drop(1))
            } else if (target.location.isAt(tile)) {
                target.onArrival(this)
            } else {
                // Route ended short of the arrival zone (relaxed radius):
                // re-find from here and let the native pathfinder finish
                // the last segment.
                moveInternal(this, target, announce = false)
            }
        }
    }
}
