package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

fun NPC.travelTo(
    destination: Tile,
    destinationArea: Area,
    dialogue: String? = null,
    queueName: String,
    onArrival: NPC.() -> Unit = {},
) {
    this["spawn_tile"] = destination

    dialogue?.let(::say)

    collision = CollisionStrategies.Normal // Calling in case the collision is indoors, will handle on a per-NPC basis on if they should have indoors afterwards or not.

    enqueue(queueName) {
        walkTo(destination)

        while (tile !in destinationArea && mode != EmptyMode) {
            delay()
        }

        if (tile in destinationArea) {
            onArrival()
        }
    }
}