package content.activity.city_raids

import content.skill.summoning.canFight
import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidManager {

    private val raids = mutableListOf<Raid>()
    private val membersByNpc = mutableMapOf<NPC, RaidMember>()

    fun addMember(
        raid: Raid,
        npc: NPC,
        type: RaidMemberType
    ): RaidMember {
        val member = RaidMember(
            npc = npc,
            raid = raid,
            type = type
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

    fun allRaids(): List<Raid> =
        raids.toList()

    fun memberOf(npc: NPC): RaidMember? =
        membersByNpc[npc]

    fun isRaidMember(npc: NPC): Boolean =
        npc in membersByNpc

    fun isFaladorGuard(npc: NPC): Boolean =
        npc.id.contains("guard_falador")

    fun isGoblin(npc: NPC): Boolean =
        npc.id in goblinIds

    fun findOrCreateFaladorRaid(): Raid =
        allRaids().firstOrNull {
            it.faction == RaidFaction.GOBLINS &&
                    it.destination == RaidDestination.FALADOR
        } ?: Raid(
            faction = RaidFaction.GOBLINS,
            destination = RaidDestination.FALADOR
        )
    fun NPC.raidMember(): RaidMember? =
        memberOf(this)

    fun NPC.canAttackRaidTarget(target: NPC): Boolean {
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
}