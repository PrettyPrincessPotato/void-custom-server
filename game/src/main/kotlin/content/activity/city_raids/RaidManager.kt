package content.activity.city_raids

import content.activity.city_raids.goblins.GoblinRaidFactory
import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidManager(
    private val store: RaidStore = RaidStore(),
    private val identityResolver: RaidIdentityResolver = RaidIdentityResolver(goblinIds),
    private val combatRules: RaidCombatRules = RaidCombatRules(store, identityResolver),
    private val raidFactory: GoblinRaidFactory = GoblinRaidFactory()
) {
    fun addMember(raid: Raid, npc: NPC): RaidMember = store.addMember(raid, npc)
    fun replaceMemberNpc(member: RaidMember, replacement: NPC) = store.replaceMemberNpc(member, replacement)
    fun removeMember(member: RaidMember) = store.removeMember(member)

    fun allRaids(): List<Raid> = store.allRaids()
    fun memberOf(npc: NPC): RaidMember? = store.memberOf(npc)
    fun isRaidMember(npc: NPC): Boolean = store.isRaidMember(npc)

    fun identityOf(npc: NPC): RaidIdentity? = identityResolver.identityOf(npc)
    fun findOrCreateFaladorRaid(): Raid = raidFactory.findOrCreateFaladorRaid(store)

    fun canAttackRaidTarget(attacker: NPC, target: NPC): Boolean =
        combatRules.canAttackRaidTarget(attacker, target)
}