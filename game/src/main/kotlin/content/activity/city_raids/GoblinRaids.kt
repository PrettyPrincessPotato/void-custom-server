package content.activity.city_raids

import content.entity.combat.dead
import content.entity.combat.inCombat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

const val DEBUG = false

//IDEA: What if other creatures don't like each-other? Like a dragon and a goblin get assigned to the same camp, there's now a turf war to see who wins. (Just make them fight and whoever lives wins)

private const val GOBLIN_HUNT_MODE = "aggressive_npcs"
private const val RAID_TIMER = "goblin_raid_timer"
private const val POLL_TIMER = "goblin_raid_poll"

/**
 * Identifying states for the LSM
 */
private data class NPCProgress(
    val state: RaidState,
    val destination: RaidDestination
)
enum class RaidState{
    TRAVELLING_TO_CAMP,
    MUSTERING,
    TRAVELLING_TO_TOWN,
    SIEGING_TOWN
}
enum class RaidDestination {
    FALADOR,
    VARROCK
}

/**
 * Locations
 */
private val GOBLIN_RAID_VARROCK_AREA : Tile = Tile(3175, 3428) //TODO: Change to a different location and get path
private val GOBLIN_CAMP_FALADOR : Tile = Tile(2953, 3407) //TODO: Remove trees when they muster
private val GOBLIN_ATTACK_FALADOR : Tile = Tile(2966, 3394)
private val GOBLIN_VILLAGE_TILE : Tile = Tile(2956, 3503) // Arbitrary tile in goblin village to spawn our war goblins.

/**
 * Mob IDs
 */
//These IDS happen to have the same name through string.
private val GOBLIN_IDS = setOf("3264", "3265", "3266", "3267")
// Just an example low level goblin that happens to share the string name with the ID. String name is needed.
private const val GOBLIN_ID_TEST = "3264"

/**
 * Routes - list of tiles from a to b.
 * TODO: Find out if there's an easier way to get paths so we only need the destination tile.
 */
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

data class RaidMember(
    val npc: NPC,
    var state: RaidState
)

class Raid(
    val destination: RaidDestination,
    val members: MutableList<RaidMember> = mutableListOf()
)
class GoblinRaids : Script {
    private val npcProgress = mutableMapOf<NPC, NPCProgress>()

    init {
        if(DEBUG){println("GoblinRaids.kt Loaded...")}

        /**
         * Declaring who can target what.
         */
        huntNPC("aggressive_npcs") { target ->
            when{
                isRaidGoblin() && !target.isRaidGoblin() -> interactNpc(target, "attack")
                isFaladorGuard() && target.isRaidGoblin() -> interactNpc(target, "attack") //TODO: find all raid members and not just goblins
            }
        }

        worldSpawn {
            if(DEBUG){println("GoblinRaids - worldSpawn")}
            World.timers.start(RAID_TIMER)
            World.timers.start(POLL_TIMER)
        }

        /**
         * Goblin Spawning timer
         */
        worldTimerStart(RAID_TIMER) { TimeUnit.SECONDS.toTicks(5) }
        worldTimerTick(RAID_TIMER) {
            // Spawn our lil gobbo and send them out, repeat every time unit above ^
            if(DEBUG){println("spawngobbo")}
            val gobbo = NPCs.addRandom(GOBLIN_ID_TEST, GOBLIN_VILLAGE_TILE.toCuboid(5)) ?: NPCs.add(GOBLIN_ID_TEST, GOBLIN_VILLAGE_TILE)
            setRaidState(gobbo, RaidState.TRAVELLING_TO_CAMP, RaidDestination.FALADOR)
            Timer.CONTINUE
        }

        /**
         * Polling timer. ~3x a second.
         */
        worldTimerStart(POLL_TIMER) { TimeUnit.MILLISECONDS.toTicks(300) }
        worldTimerTick(POLL_TIMER) {
            // Despawning all dead mobs so the world isn't flooded with gobbos
            cleanUpDeadMembers() // We can move this to a more global script later since this should be affecting EVERYONE that this script spawns

            val trackedGoblins = npcProgress.toList()
            val musteringGoblins = goblinsInState(RaidState.MUSTERING)
            val travelingToCampGoblins = goblinsInState(RaidState.TRAVELLING_TO_CAMP)
            val travellingToTownGoblins = goblinsInState(RaidState.TRAVELLING_TO_TOWN)


            //Count how many goblins are mustering. Will need to consider location here, too.
            //TODO: Expand to accept multiple possible camps.
            if(musteringGoblins.size >= 5){
                for((gobbo, progress) in npcProgress){
                    if(progress.state == RaidState.MUSTERING && progress.destination == RaidDestination.FALADOR){
                        setRaidState(gobbo, RaidState.TRAVELLING_TO_TOWN, RaidDestination.FALADOR)
                    }
                }
            }

            /**
             * Check up on our traveling goblins.
             */
            for(gobbo in travelingToCampGoblins){
                if (gobbo.mode == EmptyMode && !gobbo.inCombat){
                    // We are at the encampment, now we wait to attack
                    // For some reason I can't set a new tile as spawn, and I'm unable to re-set huntMode, so let's make a new goblin to clean up whatever's going on there.
                    gobbo.despawn(0)
                    val gobboNew = NPCs.add(GOBLIN_ID_TEST, GOBLIN_CAMP_FALADOR)
                    gobboNew.huntMode = "aggressive_npcs"
                    setRaidState(gobboNew, RaidState.MUSTERING, RaidDestination.FALADOR)
                }
            }
            for(gobbo in travellingToTownGoblins){
                if(gobbo.mode == EmptyMode && !gobbo.inCombat ) { //TODO: Figure out a more graceful "is busy" solution
                    gobbo.despawn(0)
                    val gobboNew = NPCs.add(GOBLIN_ID_TEST, GOBLIN_ATTACK_FALADOR)
                    gobboNew.huntMode = "aggressive_npcs"
                    setRaidState(gobboNew, RaidState.SIEGING_TOWN, RaidDestination.FALADOR)
                }
            }

            Timer.CONTINUE
        }
    }

    /**
     * Helper Functions
     */
    // Clean up any spawned NPC from the raid
    private fun cleanUpDeadMembers() {
        for ((gobbo, progress) in npcProgress) {
            if (gobbo.dead) {
                npcProgress.remove(gobbo)
                gobbo.despawn(0) // For some reason despawn doesn't work properly unless it's 0. Maybe because they're dead? They might not properly drop loot until this is fixed.
                continue
            }
        }
    }

    // Change what state the goblin is in and state logic
    private fun setRaidState(
        gobbo: NPC,
        state: RaidState,
        destination: RaidDestination
    ) {
        npcProgress[gobbo] = NPCProgress(state, destination)
        /**
         * WHEN STATEMENTS, make sure these don't loop into each-other.
         */
        when (state) {
            RaidState.TRAVELLING_TO_CAMP -> {
                if(DEBUG){println("Spawned goblin, going to camp")}

                val waypoints: List<Pair<Tile, Int>> = GOBLIN_VILLAGE_TO_FALADOR_CAMP.map { tile -> //TODO: Make this a variable somehow to expand different camps
                    tile to 0 // or some delay ticks between tiles
                }
                // Walk over to encampment
                gobbo.mode = Patrol(
                    character = gobbo,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = false,
                    onComplete = {
                        if (!gobbo.dead) {
                            setRaidState(gobbo, RaidState.MUSTERING, RaidDestination.FALADOR) // Destination needs to be a variable later
                        }
                    }
                )
            }

            RaidState.MUSTERING -> {
                //Find the tree object nearby somehow
                //can do gobboNew.interactObject(TreeObject) once we find nearby trees to have the goblins chop one down nearby. No shot they have anims...
            }

            RaidState.TRAVELLING_TO_TOWN -> {
                val waypoints: List<Pair<Tile, Int>> = FALADOR_CAMP_TO_FALADOR_GATE.map { tile ->
                    tile to 0 // or some delay ticks between tiles
                }
                gobbo.mode = Patrol(
                    character = gobbo,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = true, // Enabled here because they get caught and clump easily. This is more like a goblin rush.
                    onComplete = {
                        if (!gobbo.dead) {
                            setRaidState(gobbo, RaidState.SIEGING_TOWN, RaidDestination.FALADOR)
                        }
                    }
                )
            }

            RaidState.SIEGING_TOWN -> {
                /**
                 * Nothing really special needed here, they're in town. Maybe add something fancy to do here. It'd be *really* cool
                 * if the various mobs can take an entire town through some minigame, king of the hill or something, but I can see
                 * some issues with that from a gameplay stance.
                 * That also may be difficult with this engine.
                 */

            }
        }
        /**
         * End of WHEN STATEMENTS
         */
    }

    //Count how many NPCs are in the selected state.
    private fun goblinsInState(state: RaidState): List<NPC> {
        return npcProgress
            .filterValues { progress ->
                progress.state == state
            }
            .keys
            .toList()
    }

    /**
     * Who is whoms't?
     */
    private fun NPC.isRaidGoblin(): Boolean =
        id in GOBLIN_IDS

    private fun NPC.isFaladorGuard(): Boolean =
        id.startsWith("guard_fal")
}