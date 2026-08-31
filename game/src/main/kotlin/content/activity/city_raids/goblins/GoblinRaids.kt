package content.activity.city_raids.goblins

import content.activity.city_raids.RaidManager
import content.skill.summoning.canFight
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc


private const val NPC_HUNT_MODE = "aggressive_npcs" //Misnomer: will grab every NPC in the game with "aggressive_npcs"

class GoblinRaids : Script {

    private val raidManager = RaidManager()

    init {
        // TODO: Move this function in a more appropriate place. This is going to decide who can attack whom.
        huntNPC(NPC_HUNT_MODE) { target ->
            when {
                // Goblins and Falador guards attack each other
                // TODO: make Falador a faction
                // TODO: have factions properly identify each-other to create "sides" potentially.
                // Does that mean I have to make normally unattackable NPCs attackable and give them proper stats?
                // TODO: Faction alignment?
                raidManager.isFaladorGuard(target) && target.canFight() && !raidManager.isFaladorGuard(this) -> {
                    interactNpc(target, "Attack")
                }

                raidManager.isGoblin(target) && target.canFight() && !raidManager.isGoblin(this) -> {
                    interactNpc(target, "Attack")
                }
//                raidManager.isRaidMember(target) && canAttackRaidTarget(target) ->
//                    interactNpc(target, "Attack")
//
//                isFaladorGuard() && raidManager.isRaidMember(target) && target.canFight() ->
//                    interactNpc(target, "Attack")
            }
        }


    }

    /**
     * Helper functions for identifying raid targets and members
     */

    // Spawn a gobbo at the determined tile

    /**
     * End of Helper Functions
     */
}