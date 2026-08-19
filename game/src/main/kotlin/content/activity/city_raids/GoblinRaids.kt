package content.activity.city_raids

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
val debugMe = false
private val GOBLIN_RAID_VARROCK_AREA : Tile = Tile(3175, 3428) //TODO: Change to a different location and get path
private val GOBLIN_RAID_FALADOR_AREA : Tile = Tile(2953, 3407) //TODO: Remove trees when they muster

private val GOBLIN_VILLAGE_TILE : Tile = Tile(2956, 3503) // Arbitrary tile in goblin village to spawn our war goblins.

private val GOBLIN_COUNT_FALADOR : Int = 0 // TODO: Count how many goblins are mustered in camp before making a push into city

private val GOBLIN_IDS = intArrayOf(3264, 3265, 3266, 3267) // TODO: Diversify spawns. Low prio.

// A list of tiles determining a (somewhat) clean path from Goblin Village to just outside Falador.
private val GOBLIN_VILLAGE_TO_FALADOR_CAMP = listOf(
    Tile(2956, 3499),
    Tile(2956, 3491),
    Tile(2955, 3489),
    Tile(2955,3484),
    Tile(2955, 3480),
    Tile(2954,3474),
    Tile(2952, 3468),
    Tile(2948, 3452),
    Tile(2949, 3424),
    Tile(2953, 3407)
)

// Just an example low level goblin that happens to share the string name with the ID. String name is needed.
private const val GOBLIN_ID_TEST = "3264"

class GoblinRaids : Script {
    val waitingForPatrolEnd = mutableSetOf<NPC>()
    init {
        if(debugMe){println("GoblinRaids.kt Loaded...")}
        worldSpawn {
            if(debugMe){println("GoblinRaids - worldSpawn")}
            World.timers.start("goblin_raid_timer")
            World.timers.start("goblin_mode_check_timer")
        }

        worldTimerStart("goblin_raid_timer") { TimeUnit.SECONDS.toTicks(6) }
        worldTimerTick("goblin_raid_timer") {
            if(debugMe){println("spawngobbo")}
            spawnGobbo(pathTo = GOBLIN_VILLAGE_TO_FALADOR_CAMP)
            Timer.CONTINUE
        }

        worldTimerStart("goblin_mode_check_timer") { TimeUnit.MILLISECONDS.toTicks(300) } // check ~3x/sec
        worldTimerTick("goblin_mode_check_timer") {
            // Copy to avoid concurrent modification while removing
            val iteratorSnapshot = waitingForPatrolEnd.toList()

            for (gobboAny in iteratorSnapshot) {
                if (gobboAny.mode == EmptyMode) {
                    // We are at the encampment, now we wait to attack
                    gobboAny.huntMode = "aggressive" // Not working, unsure why.
                    gobboAny.mode = Wander(
                        npc = gobboAny,
                        spawn = GOBLIN_RAID_FALADOR_AREA
                    )
                    waitingForPatrolEnd.remove(gobboAny)
                }
            }
            Timer.CONTINUE
        }
    }
}

fun GoblinRaids.spawnGobbo(pathTo: List<Tile>) {

    if(debugMe){println("Starting goblin raid")}
    val gobbo = NPCs.addRandom(GOBLIN_ID_TEST, GOBLIN_VILLAGE_TILE.toCuboid(5)) ?: NPCs.add(GOBLIN_ID_TEST, GOBLIN_VILLAGE_TILE)

    // Build patrol waypoints
    val waypoints: List<Pair<Tile, Int>> = pathTo.map { tile ->
        tile to 0 // or some delay ticks between tiles
    }

    // Walk over to encampment (pathTo)
    gobbo.mode = Patrol(
        character = gobbo, // depends on how NPC exposes Character/Mode
        waypoints = waypoints,
        loop = false,
        noCollision = false
    )
    // register for global polling
    waitingForPatrolEnd.add(gobbo)
}