package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

/**
 *
 */
data class NpcLocation(
    val id: String,
    val tile: Tile,
    // Optional because most destinations are single tiles; a named area is only
    // needed for genuinely large arrival zones (shop floors, taverns).
    val area: Area? = null,
    val navTag: String? = null,
    val collision: CollisionStrategy = CollisionStrategies.Normal,
) {
    // Arrival zone for native walking: the named area when one exists,
    // otherwise a radius around the tile. A cuboid is level-bounded, so —
    // unlike a distanceTo check, which returns -1 across levels — it can
    // never match a tile on another level.
    val arriveArea: Area
        get() = area ?: tile.toCuboid(ARRIVE_RADIUS)

    fun isAt(tile: Tile): Boolean = tile in arriveArea

    companion object {
        // Arrival tolerance for area-less locations. Graph routes only terminate
        // on graph nodes, so exact tile equality would silently fail (and fall
        // back to native pathfinding) whenever the destination isn't a node.
        const val ARRIVE_RADIUS = 2
    }
}
