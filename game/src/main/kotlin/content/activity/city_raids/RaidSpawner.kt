package content.activity.city_raids


import content.activity.city_raids.routes_and_pois.IceMountainArea
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit


private const val GOBLIN_RAID_TIMER = "goblin_raid_timer"
val raidManager = RaidManager()
private val raidController = RaidController(raidManager)

class RaidSpawner : Script {
    init{

        worldSpawn {
            World.timers.start(GOBLIN_RAID_TIMER)
        }
        worldTimerStart(GOBLIN_RAID_TIMER) {
            //TimeUnit.MINUTES.toTicks(5) // Live timer
            TimeUnit.SECONDS.toTicks(5) // Debug timer
        }

        worldTimerTick(GOBLIN_RAID_TIMER) {
            // Initial goblin spawn in goblin village, they then proceed to Falador (to be randomized) where they muster forces nearby
            // Then attack when they have enough forces.
            spawnGoblin(IceMountainArea.GOBLIN_VILLAGE)
            updateGoblinRaids()
            Timer.CONTINUE
        }
    }
    fun spawnGoblin(tile: Tile, gobbo: RaidMember? = null, raidState: RaidState? = null) {
        val npc: NPC
        if(gobbo == null) {
            npc = NPCs.add(goblinIds.random(), tile)
        }
        else{
            val gobboId = gobbo.npc.id
            gobbo.npc.despawn(0) // TODO: RefreshGoblin()?
            npc = NPCs.add(gobboId, tile)
        }

        if(raidState == null && gobbo == null){
            val raid = raidManager.findOrCreateFaladorRaid()
            val member = raidManager.addMember(
                raid = raid,
                npc = npc,
                type = RaidMemberType.GOBLIN
            )

            raidController.transition(member, RaidState.TRAVELLING_TO_CAMP)
        }
        else if(raidState != null && gobbo != null){
            raidController.transition(gobbo, raidState)
        } else{
            println("Somewhere, you really messed up. GoblinRaids.kt spawnGoblin() -- Skipping spawn due to invalid spawn criteria")
            println("This is either due to raidState having null and gobbo isn't, or vice/versa.") // TODO: Separate so these errors aren't possible
        }

    }
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
                        raidController.transition(it, RaidState.TRAVELLING_TO_TOWN)
                    }
                }
            }
    }

}