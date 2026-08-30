package content.activity.city_raids.goblins

import content.activity.city_raids.Raid
import content.activity.city_raids.RaidDestination
import content.activity.city_raids.RaidFaction
import content.activity.city_raids.RaidManager
import content.activity.city_raids.RaidMember
import content.activity.city_raids.RaidMemberType
import content.activity.city_raids.RaidState
import content.activity.city_raids.routes_and_pois.IceMountainArea
import content.entity.combat.dead
import content.skill.summoning.canFight
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

private const val GOBLIN_RAID_TIMER = "goblin_raid_timer"
private val GOBLIN_IDS = setOf("3264", "3265", "3266", "3267")
private const val GOBLIN_ID = "3264" // TODO: Add variety spawning
private val GOBLIN_HUNT_MODE = "aggressive_npcs" //Misnomer: will grab every NPC in the game with "aggressive_npcs"

class GoblinRaids : Script {

    private val raidManager = RaidManager()

    init {
        // TODO: Move this function in a more appropriate place. This is going to decide who can attack whom.
        huntNPC(GOBLIN_HUNT_MODE) { target ->
            when {
                // Goblins and Falador guards attack each other
                // TODO: make Falador a faction
                // TODO: have factions properly identify each-other to create "sides" potentially.
                // Does that mean I have to make normally unattackable NPCs attackable and give them proper stats?
                // TODO: Faction alignment?
                target.isFaladorGuard() && target.canFight() && !this.isFaladorGuard() -> {
                    interactNpc(target, "Attack")
                }

                target.isGoblin() && target.canFight() && !this.isGoblin() -> {
                    interactNpc(target, "Attack")
                }
//                raidManager.isRaidMember(target) && canAttackRaidTarget(target) ->
//                    interactNpc(target, "Attack")
//
//                isFaladorGuard() && raidManager.isRaidMember(target) && target.canFight() ->
//                    interactNpc(target, "Attack")
            }
        }

        worldSpawn {
            World.timers.start(GOBLIN_RAID_TIMER)
        }

        worldTimerStart(GOBLIN_RAID_TIMER) {
            TimeUnit.MINUTES.toTicks(5)
        }

        worldTimerTick(GOBLIN_RAID_TIMER) {
            // Initial goblin spawn in goblin village, they then proceed to Falador (to be randomized) where they muster forces nearby
            // Then attack when they have enough forces.
            spawnGoblin(IceMountainArea.GOBLIN_VILLAGE)
            updateGoblinRaids()
            Timer.CONTINUE
        }
    }

    /**
     * Helper functions for identifying raid targets and members
     * TODO: Move somewhere more central. RaidManager?
     */
    private fun NPC.isFaladorGuard(): Boolean =
        id.contains("guard_falador")

    private fun NPC.isGoblin(): Boolean =
        id in GOBLIN_IDS

    private fun NPC.raidMember(): RaidMember? =
        raidManager.memberOf(this)

    private fun NPC.canAttackRaidTarget(target: NPC): Boolean {
        val attacker = raidMember() ?: return false
        val victim = target.raidMember() ?: return false

        // Prevent members of the same raid from attacking each other.
        if (attacker.raid == victim.raid) {
            return false
        }

        // Prevent allied factions from attacking each other.
        if (attacker.raid.faction == victim.raid.faction) {
            return false
        }

        return target.canFight()
    }

    // Spawn a gobbo at the determined tile
    private fun spawnGoblin(tile: Tile, gobbo: RaidMember? = null, raidState: RaidState? = null) {
        val npc: NPC
        if(gobbo == null) {
            npc = NPCs.add(GOBLIN_ID, tile)
        }
        else{
            gobbo.npc.despawn(0) // TODO: RefreshGoblin()?
            npc = NPCs.add(GOBLIN_ID, tile)
        }

        if(raidState == null && gobbo == null){
            val raid = findOrCreateFaladorRaid()
            val member = raidManager.addMember(
                raid = raid,
                npc = npc,
                type = RaidMemberType.GOBLIN
            )

            transition(member, RaidState.TRAVELLING_TO_CAMP)
        }
        else if(raidState != null && gobbo != null){
            transition(gobbo, raidState)
        } else{
            println("Somewhere, you really messed up. GoblinRaids.kt spawnGoblin() -- Skipping spawn due to invalid spawn criteria")
            println("This is either due to raidState having null and gobbo isn't, or vice/versa.")
        }

    }

    private fun respawnGoblin(member: RaidMember, tile: Tile): NPC {
        member.npc.despawn(0)

        val replacement = NPCs.add(GOBLIN_ID, tile)
        raidManager.replaceMemberNpc(member, replacement)

        return replacement
    }


    private fun findOrCreateFaladorRaid(): Raid =
        raidManager.allRaids().firstOrNull {
            it.faction == RaidFaction.GOBLINS &&
                    it.destination == RaidDestination.FALADOR
        } ?: Raid(
            faction = RaidFaction.GOBLINS,
            destination = RaidDestination.FALADOR
        )

    private fun updateGoblinRaids() {
        raidManager.allRaids()
            .filter {
                it.faction == RaidFaction.GOBLINS &&
                        it.destination == RaidDestination.FALADOR
            }
            .forEach { raid ->
                val mustering = raid.membersInState(RaidState.MUSTERING)
                if (mustering.size >= 5) {
                    mustering.forEach {
                        transition(it, RaidState.TRAVELLING_TO_TOWN)
                    }
                }
            }
    }
    /**
     * End of Helper Functions
     */

    // Essentially goblin brains. Go big war god!
    private fun transition(
        gobbo: RaidMember,
        state: RaidState
    ) {
        gobbo.state = state

        when (state) {
            RaidState.TRAVELLING_TO_CAMP -> {
                val waypoints: List<Pair<Tile, Int>> = IceMountainArea.GOBLIN_VILLAGE_TO_FALADOR_CAMP.map { tile -> //TODO: Make this a variable somehow to expand different camps
                    tile to 0 // or some delay ticks between tiles
                }
                // Walk over to encampment
                gobbo.npc.mode = Patrol(
                    character = gobbo.npc,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = false,
                    onComplete = {
                        if (!gobbo.npc.dead) {
                            respawnGoblin(gobbo, IceMountainArea.FALADOR_CAMP)
                            transition( gobbo, RaidState.MUSTERING )
                        }
                    }
                )
            }

            RaidState.MUSTERING -> {
                //Find the tree object nearby somehow
                //can do gobboNew.interactObject(TreeObject) once we find nearby trees to have the goblins chop one down nearby.
            }

            RaidState.TRAVELLING_TO_TOWN -> {
                val waypoints: List<Pair<Tile, Int>> = IceMountainArea.FALADOR_CAMP_TO_GATE.map { tile ->
                    tile to 0 // or some delay ticks between tiles
                }
                gobbo.npc.mode = Patrol(
                    character = gobbo.npc,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = true, // Enabled here because they get caught and clump easily. This is more like a goblin rush.
                    onComplete = {
                        if (!gobbo.npc.dead) {
                            respawnGoblin( gobbo, IceMountainArea.FALADOR_GATE )
                            transition( gobbo, RaidState.SIEGING_TOWN )
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
    }
}