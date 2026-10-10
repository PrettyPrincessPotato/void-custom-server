package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import kotlin.math.abs
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.engine.queue.queue as enqueue

fun NPC.travelTo(
    destination: Tile,
    queueName: String,
    destinationArea: Area? = null,
    noClip: Boolean = false,
    maxRetries: Int = 10,
    onGiveUp: (NPC) -> Unit = {
        println("travelTo give-up: ${it.id} at ${it.tile}, target $destination")
    },
    onArrival: NPC.() -> Unit = {},
) {
    collision = CollisionStrategies.Normal

    enqueue(queueName) {
        val stamped = mutableListOf<Tile>()
        fun clearStamps() {
            for (t in stamped) {
                Collisions.remove(t.x, t.y, t.level, CollisionFlag.OBJECT_ROUTE_BLOCKER)
            }
            stamped.clear()
        }
        fun stampBlockers() {
            clearStamps()
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) {
                        continue
                    }
                    val t = tile.add(dx, dy)
                    if (Collisions.check(t, CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS)) {
                        Collisions.add(t.x, t.y, t.level, CollisionFlag.OBJECT_ROUTE_BLOCKER)
                        stamped.add(t)
                    }
                }
            }
        }

        // Border gates are decorative — the player mechanism in Movement.kt
        // force-walks across the passage with no collision instead of "opening"
        // the gate. Grant the same window when this hop crosses a passage.
        val noClip = noClip || hopCrossesBorder(tile, destination)

        var retries = 0
        try {
            walkTo(destination, noCollision = noClip)

            while (tile != destination && destinationArea?.contains(tile) != true) {
                delay()
                val movement = mode as? Movement
                if (movement != null) {
                    if (steps.isEmpty()) {
                        stampBlockers()
                        movement.recalculate()
                    } else {
                        // Walk is progressing — stamps have served their purpose.
                        clearStamps()
                    }
                    continue
                }
                if (++retries >= maxRetries) {
                    break
                }
                walkTo(destination, noCollision = noClip)
            }

            if (tile == destination || destinationArea?.contains(tile) == true) {
                onArrival()
            } else {
                onGiveUp(this)
                tele(destination)
                onArrival()
            }
        } finally {
            clearStamps()
        }
    }
}

fun setSpawnAndWander(npc: NPC, spawnTile: Tile, indoorCollision: Boolean = false) {
    if (indoorCollision) {
        npc.collision = CollisionStrategies.Indoors
    }
    npc["spawn_tile"] = spawnTile
    npc.mode = Wander(npc, spawnTile)
}

/**
 * Border guard passages (`tags = ["border"]`) are decorative gates: the player
 * mechanism in `Movement.kt` force-walks across them with no collision instead
 * of "opening" the gate. Routed NPCs get the same window — a hop that crosses
 * a passage walks it collision-blind, through the lowered gate.
 */
private val borderPassages: List<Rectangle> by lazy {
    Areas.tagged("border").map { it.area as Rectangle }
}

private fun hopCrossesBorder(start: Tile, end: Tile): Boolean {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val steps = maxOf(abs(dx), abs(dy))
    for (i in 0..steps) {
        val t = Tile(start.x + (dx * i) / steps, start.y + (dy * i) / steps)
        if (t.level == 0 && borderPassages.any { it.contains(t) }) {
            return true
        }
    }
    return false
}
