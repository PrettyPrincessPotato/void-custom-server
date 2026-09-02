package content.activity.city_raids

import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidStore {
    private val raids = mutableListOf<Raid>()
    private val membersByNpc = mutableMapOf<NPC, RaidMember>()

    fun addMember(raid: Raid, npc: NPC): RaidMember {
        val member = RaidMember(npc = npc, raid = raid)

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

    fun allRaids(): List<Raid> = raids.toList()

    fun memberOf(npc: NPC): RaidMember? = membersByNpc[npc]

    fun isRaidMember(npc: NPC): Boolean = npc in membersByNpc
}