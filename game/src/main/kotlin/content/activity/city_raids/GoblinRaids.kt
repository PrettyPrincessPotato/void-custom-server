package content.activity.city_raids

import content.entity.combat.dead
import content.entity.combat.inCombat
import content.entity.combat.target
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

const val debugMe = false

private val GOBLIN_RAID_VARROCK_AREA : Tile = Tile(3175, 3428) //TODO: Change to a different location and get path
private val GOBLIN_CAMP_FALADOR : Tile = Tile(2953, 3407) //TODO: Remove trees when they muster
private val GOBLIN_ATTACK_FALADOR : Tile = Tile(2966, 3394)

private val GOBLIN_VILLAGE_TILE : Tile = Tile(2956, 3503) // Arbitrary tile in goblin village to spawn our war goblins.

private val GOBLIN_IDS = intArrayOf(3264, 3265, 3266, 3267) //TODO: Diversify spawns. Low prio.

// A list of tiles determining a (somewhat) clean path from Goblin Village to just outside Falador.
private val GOBLIN_VILLAGE_TO_FALADOR_CAMP = listOf(
    Tile(2956, 3499),
    Tile(2956, 3491),
    Tile(2955, 3489),
    Tile(2955, 3484),
    Tile(2955, 3480),
    Tile(2954, 3474),
    Tile(2952, 3468),
    Tile(2948, 3452),
    Tile(2949, 3424),
    Tile(2953, 3407)
)

// A list of tiles starting from the Falador Encampment into the gates to where all the guards are.
private val FALADOR_CAMP_TO_FALADOR_GATE = listOf(
    Tile(2955, 3405),
    Tile(2956, 3398),
    Tile(2966, 3398),
    Tile(2966, 3394)
)
// Just an example low level goblin that happens to share the string name with the ID. String name is needed.
private const val GOBLIN_ID_TEST = "3264"

class GoblinRaids : Script {
    val waitingForPatrolEnd = mutableSetOf<NPC>()
    val musteringInFalador = mutableSetOf<NPC>()
    val attackingFalador = mutableSetOf<NPC>()
    val activeGobbos = mutableSetOf<NPC>()

    init {
        if(debugMe){println("GoblinRaids.kt Loaded...")}
        huntNPC("aggressive_npcs") { target ->
            if (id.startsWith("guard_fal") && target.id.startsWith("") && !target.id.startsWith("guard_fal")) {
                interactNpc(target, "Attack")
            }
            if (id == GOBLIN_ID_TEST && target.id.startsWith("") && target.id != GOBLIN_ID_TEST) {
                interactNpc(target, "Attack")
            }
        }

        worldSpawn {
            if(debugMe){println("GoblinRaids - worldSpawn")}
            World.timers.start("goblin_raid_timer")
            World.timers.start("goblin_mode_check_timer")
            World.timers.start("goblin_count_check_timer")
        }

        worldTimerStart("goblin_raid_timer") { TimeUnit.SECONDS.toTicks(15) }
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
                if (gobboAny.mode == EmptyMode && !gobboAny.inCombat ) { //TODO: Figure out a more graceful "is busy" solution
                    // We are at the encampment, now we wait to attack
                    // AI seems to be wonky, so let's make a new goblin.
                    gobboAny.despawn(0)
                    val gobboNew = NPCs.addRandom(GOBLIN_ID_TEST, GOBLIN_CAMP_FALADOR.toCuboid(5)) ?: NPCs.add(GOBLIN_ID_TEST, GOBLIN_CAMP_FALADOR)
                    gobboNew.huntMode = "aggressive_npcs" // Not close enough to city yet so ignore NPCs.
                    //can do gobboNew.interactObject(TreeObject) once we find nearby trees to have the goblins chop one down nearby. No shot they have anims...


                    if(debugMe){
                        println("Applied Hunt Mode: ${gobboNew}, ${gobboNew.huntMode}")
                    }
                    waitingForPatrolEnd.remove(gobboAny) // This might cause issues since I despawn the NPC already. Let's find out!
                    musteringInFalador.add(gobboNew)
                    activeGobbos.remove(gobboAny)
                    activeGobbos.add(gobboNew)
                }
            }
            Timer.CONTINUE
        }
        worldTimerStart("goblin_count_check_timer") { TimeUnit.MILLISECONDS.toTicks(300) } // check ~3x/sec
        worldTimerTick("goblin_count_check_timer") {
            val iteratorSnapshotAttacking = attackingFalador.toList()
            val iteratorSnapshotInCombat = activeGobbos.toList()

            if(musteringInFalador.count() > 3){
                val iteratorSnapshotMustering = musteringInFalador.toList()

                // Tying waypoints from Falador camp to gate where the guards are
                val waypoints: List<Pair<Tile, Int>> = FALADOR_CAMP_TO_FALADOR_GATE.map { tile ->
                    tile to 0 // or some delay ticks between tiles
                }
                // TODO: Shortcut pathing somehow
                for(gobboAny in iteratorSnapshotMustering){
                    gobboAny.mode = Patrol(
                        character = gobboAny,
                        waypoints = waypoints,
                        loop = false,
                        noCollision = true // Enabled here because they get caught and clump easily. This is more like a goblin rush.
                    )
                    attackingFalador.add(gobboAny)
                    musteringInFalador.remove(gobboAny)
                }
            }
            // These are attacking and need to be set to wander again once the patrol is done
            for(gobboAny in iteratorSnapshotAttacking){
                if(debugMe){println(gobboAny.mode)}

                if(gobboAny.mode == EmptyMode && !gobboAny.inCombat ){ //TODO: Figure out a more graceful "is busy" solution
                    gobboAny.despawn(0)
                    val gobboNew = NPCs.add(GOBLIN_ID_TEST, GOBLIN_ATTACK_FALADOR)
                    gobboNew.huntMode = "aggressive_npcs" // AI for multi-combat attacking inside the city, so focused on NPCs //camp_aggressive

                    attackingFalador.remove(gobboAny)
                    activeGobbos.remove(gobboAny)
                    activeGobbos.add(gobboNew)
                }
            }
            // These are all the goblins currently existing through this script, we need to make sure that if they die, they stay dead.
            for(gobboAny in iteratorSnapshotInCombat){
                if(gobboAny.dead){
                    gobboAny.despawn(5)
                    activeGobbos.remove(gobboAny)
                    //Now we need to clean up the other arrays they happen to be in.
                    if(gobboAny in waitingForPatrolEnd){
                        waitingForPatrolEnd.remove(gobboAny)
                    }
                    if(gobboAny in musteringInFalador){
                        musteringInFalador.remove(gobboAny)
                    }
                    if(gobboAny in attackingFalador){
                        attackingFalador.remove(gobboAny)
                    }
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
        character = gobbo,
        waypoints = waypoints,
        loop = false,
        noCollision = false
    )
    // register for global polling
    activeGobbos.add(gobbo)
    waitingForPatrolEnd.add(gobbo)
}