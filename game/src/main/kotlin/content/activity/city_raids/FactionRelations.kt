package content.activity.city_raids


class FactionRelations(
    private val relations: Map<Set<RaidFaction>, FactionRelation>
) {
    fun relation(
        source: RaidFaction,
        target: RaidFaction
    ): FactionRelation {
        if (source == target) {
            return FactionRelation.ALLIED
        }
        return relations[setOf(source, target)]
            ?: FactionRelation.NEUTRAL
    }
}

enum class FactionRelation {
    ALLIED,
    NEUTRAL,
    HOSTILE
}

