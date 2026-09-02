package content.activity.city_raids.goblins

import content.activity.city_raids.RaidDestination
import content.activity.city_raids.RaidFaction
import content.activity.city_raids.RaidManager
import content.activity.city_raids.RaidMember
import content.activity.city_raids.RaidState
import content.activity.city_raids.goblinIds
import content.activity.city_raids.routes_and_pois.IceMountainArea
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile



class GoblinRaidSystem(
    private val raidManager: RaidManager
) {

    private val controller = GoblinRaidController(
        raidManager = raidManager,
        goblinIds = goblinIds.toList()
    )

    fun tick() {
        spawnGoblin(IceMountainArea.GOBLIN_VILLAGE)
        updateGoblinRaids()
    }

    private fun spawnGoblin(
        tile: Tile,
        gobbo: RaidMember? = null,
        raidState: RaidState? = null
    ) {
        val npc: NPC

        if (gobbo == null) {
            npc = NPCs.add(goblinIds.random(), tile)
        } else {
            val gobboId = gobbo.npc.id
            gobbo.npc.despawn(0)
            npc = NPCs.add(gobboId, tile)
        }

        when {
            raidState == null && gobbo == null -> {
                val raid = raidManager.findOrCreateFaladorRaid()
                val member = raidManager.addMember(
                    raid = raid,
                    npc = npc
                )
                controller.transition(member, RaidState.TRAVELLING_TO_CAMP)
            }

            raidState != null && gobbo != null -> {
                controller.transition(gobbo, raidState)
            }

            else -> {
                println("Somewhere, you really messed up. GoblinRaidSystem.spawnGoblin() -- Skipping spawn due to invalid spawn criteria")
                println("This is either due to raidState being null and gobbo not being null, or vice versa.")
            }
        }
    }

    private fun updateGoblinRaids() {
        raidManager.allRaids()
            .filter {
                it.faction == RaidFaction.GOBLIN_TRIBE &&
                    it.destination == RaidDestination.FALADOR
            }
            .forEach { raid ->
                val mustering = raid.membersInState(RaidState.MUSTERING)
                if (mustering.size >= 5) {
                    mustering.forEach {
                        controller.transition(it, RaidState.TRAVELLING_TO_TOWN)
                    }
                }
            }
    }
}