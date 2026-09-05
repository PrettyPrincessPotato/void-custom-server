package content.activity.city_raids.goblins

import content.activity.city_raids.Raid
import content.activity.city_raids.RaidDestination
import content.activity.city_raids.RaidFaction
import content.activity.city_raids.RaidStore

/**
 * Can't write this down so I"m jotting it here:
 * goblins DO NOT GIVE A FUCK if you attack them while they're routing.
 * We also have the ability to use bot navmesh now, so might as well switch it over entirely.
 * See ShopAssistantFalador.kt for an example
 * Could make the goblins chant something like "for the big war god" while they're attacking
 */

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