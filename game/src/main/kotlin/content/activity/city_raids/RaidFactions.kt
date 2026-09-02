package content.activity.city_raids

val relationsMap = mapOf(
    setOf(RaidFaction.GOBLIN_TRIBE, RaidFaction.FALADOR) to FactionRelation.HOSTILE,
    setOf(RaidFaction.FALADOR, RaidFaction.VARROCK) to FactionRelation.ALLIED
)

object RaidFactions {
    private val relations = FactionRelations(relationsMap)

    fun relation(
        source: RaidFaction,
        target: RaidFaction
    ): FactionRelation {
        return relations.relation(source, target)
    }
}