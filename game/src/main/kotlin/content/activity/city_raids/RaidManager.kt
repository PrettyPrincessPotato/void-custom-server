package content.activity.city_raids

import content.skill.summoning.canFight
import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidManager {

    private val raids = mutableListOf<Raid>()
    private val membersByNpc = mutableMapOf<NPC, RaidMember>()

    fun addMember(
        raid: Raid,
        npc: NPC,
    ): RaidMember {
        val member = RaidMember(
            npc = npc,
            raid = raid,
        )

        if (raid !in raids) {
            raids += raid
        }

        raid.members += member
        membersByNpc[npc] = member

        return member
    }

    fun replaceMemberNpc(member: RaidMember, replacement: NPC) {
        val oldNpc = member.npc

        membersByNpc.remove(oldNpc)

        member.npc = replacement
        membersByNpc[replacement] = member
    }


    fun removeMember(member: RaidMember) {
        member.raid.members.remove(member)
        membersByNpc.remove(member.npc)

        if (member.raid.isEmpty()) {
            raids.remove(member.raid)
        }
    }

    fun identityOf(npc: NPC): RaidIdentity? {
        memberOf(npc)?.let { member ->
            return RaidIdentity(
                race = RaidRace.GOBLIN,
                faction = member.raid.faction
            )
        }

        return when {
            isFaladorGuard(npc) -> RaidIdentity(
                race = RaidRace.HUMAN,
                faction = RaidFaction.FALADOR
            )

            isVarrockGuard(npc) -> RaidIdentity(
                race = RaidRace.HUMAN,
                faction = RaidFaction.VARROCK
            )

            isGoblin(npc) -> RaidIdentity(
                race = RaidRace.GOBLIN,
                faction = RaidFaction.GOBLIN_TRIBE
            )

            else -> null
        }
    }

    fun allRaids(): List<Raid> =
        raids.toList()

    fun memberOf(npc: NPC): RaidMember? =
        membersByNpc[npc]

    fun isRaidMember(npc: NPC): Boolean =
        npc in membersByNpc

    fun isFaladorGuard(npc: NPC): Boolean =
        npc.id.contains("guard_falador")

    fun isVarrockGuard(npc: NPC): Boolean =
        npc.id.contains("guard_varrock")

    fun isGoblin(npc: NPC): Boolean =
        npc.id in goblinIds

    fun findOrCreateFaladorRaid(): Raid =
        allRaids().firstOrNull {
            it.faction == RaidFaction.GOBLIN_TRIBE &&
                    it.destination == RaidDestination.FALADOR
        } ?: Raid(
            faction = RaidFaction.GOBLIN_TRIBE,
            destination = RaidDestination.FALADOR
        )

    /**
     * TODO: Move these to a more appropriate spot
     */
    fun NPC.raidMember(): RaidMember? =
        memberOf(this)

    fun NPC.canAttackRaidTarget(target: NPC): Boolean {
        val attackerIdentity = identityOf(this) ?: return false
        val targetIdentity = identityOf(target) ?: return false

        val attackerMember = memberOf(this)
        val targetMember = memberOf(target)

        // Members of the same raid never attack each other.
        if (
            attackerMember != null &&
            targetMember != null &&
            attackerMember.raid == targetMember.raid
        ) {
            return false
        }

        val source = attackerIdentity.faction ?: return false
        val destination = targetIdentity.faction ?: return false

        return RaidFactions.relation(source, destination) ==
                FactionRelation.HOSTILE &&
                target.canFight()
    }
}