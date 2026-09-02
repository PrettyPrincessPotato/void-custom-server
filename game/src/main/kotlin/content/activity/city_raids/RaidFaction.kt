package content.activity.city_raids

val goblinIds = setOf("3264", "3265", "3266", "3267")

data class RaidIdentity(
    val race: RaidRace,
    val faction: RaidFaction?
)

enum class RaidRace {
    HUMAN,
    GOBLIN,
    DRAGON,
    DWARF,
    OGRE
}

enum class RaidFaction {
    GOBLIN_TRIBE,
    DRAGONS,
    FALADOR,
    VARROCK,
    BARBARIAN_SETTLEMENT,
    DORGESHUUN
}

val goblin = RaidIdentity(
    race = RaidRace.GOBLIN,
    faction = RaidFaction.GOBLIN_TRIBE,
)

val dorgeshuunGoblin = RaidIdentity(
    race = RaidRace.GOBLIN,
    faction = RaidFaction.DORGESHUUN,
)

val faladorGuard = RaidIdentity(
    race = RaidRace.HUMAN,
    faction = RaidFaction.FALADOR,
)

val varrockGuard = RaidIdentity(
    race = RaidRace.HUMAN,
    faction = RaidFaction.VARROCK,
)