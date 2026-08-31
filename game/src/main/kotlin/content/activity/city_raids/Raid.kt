package content.activity.city_raids

class Raid(
    val faction: RaidFaction,
    val destination: RaidDestination,
    val members: MutableList<RaidMember> = mutableListOf()
) {
    fun membersInState(state: RaidState): List<RaidMember> =
        members.filter { it.state == state }

    fun isEmpty(): Boolean =
        members.isEmpty()
}