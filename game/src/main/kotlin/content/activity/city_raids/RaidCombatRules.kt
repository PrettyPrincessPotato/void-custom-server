package content.activity.city_raids

import content.skill.summoning.canFight
import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidCombatRules(
    private val store: RaidStore,
    private val identityResolver: RaidIdentityResolver
) {
    fun canAttackRaidTarget(attacker: NPC, target: NPC): Boolean {
        val attackerIdentity = identityResolver.identityOf(attacker) ?: return false
        val targetIdentity = identityResolver.identityOf(target) ?: return false

        val attackerMember = store.memberOf(attacker)
        val targetMember = store.memberOf(target)

        if (
            attackerMember != null &&
            targetMember != null &&
            attackerMember.raid == targetMember.raid
        ) {
            return false
        }

        val source = attackerIdentity.faction ?: return false
        val destination = targetIdentity.faction ?: return false

        return RaidFactions.relation(source, destination) == FactionRelation.HOSTILE &&
                target.canFight()
    }
}