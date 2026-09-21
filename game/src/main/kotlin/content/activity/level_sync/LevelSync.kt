package content.activity.level_sync

import content.entity.player.combat.calculateCombatLevel
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.summoningCombatLevel

class LevelSync : Script {

    init {
        playerCommand("sync") {
            syncedLevels = SyncedLevels(
                attack = 1,
                strength = 1,
                defence = 1,
                hitpoints = 10,
                ranged = 1,
                magic = 1,
                prayer = 1,
                summoning = 1
            )

            val levels = effectiveLevels()

            combatLevel = calculateCombatLevel(
                levels,
                summoning = World.members
            )

            summoningCombatLevel = calculateCombatLevel(
                levels,
                summoning = true
            )

            message("Your combat level has been synchronized to $combatLevel.")
        }

        playerCommand("unsync") {
            syncedLevels = null

            val levels = effectiveLevels()

            combatLevel = calculateCombatLevel(
                levels,
                summoning = World.members
            )

            summoningCombatLevel = calculateCombatLevel(
                levels,
                summoning = true
            )

            message("Your normal combat levels have been restored.")
        }
    }
}