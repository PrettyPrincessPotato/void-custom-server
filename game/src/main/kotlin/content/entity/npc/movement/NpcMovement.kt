package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

fun NPC.travelTo(
    destination: Tile,
    destinationArea: Area?,
    queueName: String,
    onArrival: NPC.() -> Unit = {},
) {
    collision = CollisionStrategies.Normal

    enqueue(queueName) {
        walkTo(destination)

        while (
            tile != destination &&
            destinationArea?.contains(tile) != true &&
            mode != EmptyMode
        ) {
            delay()
        }

        if (tile == destination || destinationArea?.contains(tile) == true) {
            onArrival()
        }
    }
}