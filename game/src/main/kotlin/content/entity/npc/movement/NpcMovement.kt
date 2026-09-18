package content.entity.npc.movement

import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

fun NPC.travelTo(
    destination: Tile,
    destinationArea: Area? = null,
    queueName: String,
    noClip: Boolean = false,
    onArrival: NPC.() -> Unit = {},
) {
    collision = CollisionStrategies.Normal

    enqueue(queueName) {
        walkTo(destination, noCollision = noClip)

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

fun setSpawnAndWander(npc: NPC, spawnTile: Tile) {
    npc["spawn_tile"] = spawnTile
    npc.mode = Wander(npc, spawnTile)
}