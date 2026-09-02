package content.activity.city_raids.goblins

import content.activity.city_raids.RaidController
import content.activity.city_raids.RaidManager
import content.activity.city_raids.RaidMember
import content.activity.city_raids.RaidState
import content.activity.city_raids.routes_and_pois.IceMountainArea
import content.entity.combat.dead
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class GoblinRaidController(
    private val raidManager: RaidManager,
    private val goblinIds: List<String>
) : RaidController {

    override fun transition(
        member: RaidMember,
        state: RaidState
    ) {
        member.state = state

        when (state) {
            RaidState.TRAVELLING_TO_CAMP ->
                travelToCamp(member)

            RaidState.MUSTERING ->
                muster(member)

            RaidState.TRAVELLING_TO_TOWN ->
                travelToTown(member)

            RaidState.SIEGING_TOWN ->
                siegeTown(member)
        }
    }

    private fun travelToCamp(member: RaidMember) {
        patrol(
            member = member,
            route = IceMountainArea.GOBLIN_VILLAGE_TO_FALADOR_CAMP,
            noCollision = false
        ) {
            replaceAt(
                member = member,
                tile = IceMountainArea.FALADOR_CAMP,
                nextState = RaidState.MUSTERING
            )
        }
    }

    private fun muster(member: RaidMember) {
        // The goblins are chilling for now
        // Maybe see if we can chop trees or something later.
    }

    private fun travelToTown(member: RaidMember) {
        patrol(
            member = member,
            route = IceMountainArea.FALADOR_CAMP_TO_GATE,
            noCollision = true
        ) {
            replaceAt(
                member = member,
                tile = IceMountainArea.FALADOR_GATE,
                nextState = RaidState.SIEGING_TOWN
            )
        }
    }

    private fun siegeTown(member: RaidMember) {
        // Future:
        // - attack gate
        // - target guards
        // - capture points
        // - spawn reinforcements
    }

    private fun patrol(
        member: RaidMember,
        route: List<Tile>,
        noCollision: Boolean,
        onComplete: () -> Unit
    ) {
        member.npc.mode = Patrol(
            character = member.npc,
            waypoints = route.map { it to 0 },
            loop = false,
            noCollision = noCollision,
            onComplete = {
                if (!member.npc.dead) {
                    onComplete()
                }
            }
        )
    }

    private fun replaceAt(
        member: RaidMember,
        tile: Tile,
        nextState: RaidState
    ) {
        member.npc.despawn(0)

        val replacement = NPCs.add(goblinIds.random(), tile)
        raidManager.replaceMemberNpc(member, replacement)

        transition(member, nextState)
    }
}