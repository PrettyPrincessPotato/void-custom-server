package content.activity.city_raids.goblins

import content.activity.city_raids.Raid
import content.activity.city_raids.RaidDestination
import content.activity.city_raids.RaidFaction
import content.activity.city_raids.RaidStore

/**
 * Can't write this down as a proper issue, so I'm jotting it here:
 * goblins DO NOT GIVE A FUCK if you attack them while they're routing.
 * Sometimes they'll attack back, but it's rare
 * We also have the ability to use bot navmesh now, so might as well switch it over entirely.
 * See ShopAssistantFalador.kt for an example
 * Could make the goblins chant something like "for the big war god" while they're attacking
 *
 * In the issues page I wrote that falador guards and goblins aggro is broken, that is no longer true:
 * I fixed guards with Guards.kt to resolve aggro there.
 *
 * Only semi-related but the reason guards broke was because GuardFalador seems to have gotten overwritten
 * in a git merge from main. I did spot a new text blurb that made me think... We should have a guard run
 * back and forth between Falador and whever falador_guard_7 is to provide supplies and also give the player
 * a hint that someone is over there.
 *
 * Also, side-note, look at black and white armor sets/weapons and how they're acquired. That might need
 * tweaking.
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