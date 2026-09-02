package content.activity.city_raids.goblins

import content.activity.city_raids.Raid
import content.activity.city_raids.RaidDestination
import content.activity.city_raids.RaidFaction
import content.activity.city_raids.RaidStore

class GoblinRaidFactory {
    fun findOrCreateFaladorRaid(store: RaidStore): Raid =
        store.allRaids().firstOrNull {
            it.faction == RaidFaction.GOBLIN_TRIBE &&
                    it.destination == RaidDestination.FALADOR
        } ?: Raid(
            faction = RaidFaction.GOBLIN_TRIBE,
            destination = RaidDestination.FALADOR
        )
}