package content.activity.level_sync

import content.activity.level_sync.effectiveLevels
import content.activity.level_sync.syncedLevels
import content.entity.player.combat.calculateCombatLevel
import content.entity.player.command.find
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.summoningCombatLevel

class LevelSync : Script {

    init {
        playerCommand("unsync") {
            syncedLevels = null

            val levels = effectiveLevels()

            combatLevel = calculateCombatLevel(
                levels,
                summoning = World.members,
            )

            summoningCombatLevel = calculateCombatLevel(
                levels,
                summoning = true,
            )

            message("Your normal combat levels have been restored.")
        }
    }
}

fun levelSync(player: Player, args: List<String>) {
    val targetName = args.getOrNull(0)

    if (targetName == null) {
        player.message("Usage: <red>sync <player_name>")
        return
    }

    val target = Players.find(player, targetName)

    if (target == null) {
        player.message("Player '$targetName' could not be found.")
        return
    }

    val playerLevels = player.levels
    val targetCombatLevel = target.combatLevel
    val maximumDifference = 3

    var synced = SyncedLevels(
        attack = playerLevels.getMax(Skill.Attack),
        strength = playerLevels.getMax(Skill.Strength),
        defence = playerLevels.getMax(Skill.Defence),
        hitpoints = playerLevels.getMax(Skill.Constitution),
        ranged = playerLevels.getMax(Skill.Ranged),
        magic = playerLevels.getMax(Skill.Magic),
        prayer = playerLevels.getMax(Skill.Prayer),
        summoning = playerLevels.getMax(Skill.Summoning),
    )

    fun updateCombatLevel(): Int {
        player.syncedLevels = synced

        val levels = player.effectiveLevels()

        player.combatLevel = calculateCombatLevel(
            levels,
            summoning = World.members,
        )

        player.summoningCombatLevel = calculateCombatLevel(
            levels,
            summoning = true,
        )

        return player.combatLevel
    }

    var combatLevel = updateCombatLevel()

    while (combatLevel > targetCombatLevel + maximumDifference) {
        synced = SyncedLevels(
            attack = (synced.attack - 1).coerceAtLeast(1),
            strength = (synced.strength - 1).coerceAtLeast(1),
            defence = (synced.defence - 1).coerceAtLeast(1),
            hitpoints = (synced.hitpoints - 10).coerceAtLeast(10),
            ranged = (synced.ranged - 1).coerceAtLeast(1),
            magic = (synced.magic - 1).coerceAtLeast(1),
            prayer = (synced.prayer - 1).coerceAtLeast(1),
            summoning = (synced.summoning - 1).coerceAtLeast(1),
        )

        val previousCombatLevel = combatLevel
        combatLevel = updateCombatLevel()

        // Prevent an infinite loop if minimum levels have been reached.
        if (combatLevel == previousCombatLevel &&
            synced.attack == 1 &&
            synced.strength == 1 &&
            synced.defence == 1 &&
            synced.hitpoints == 10 &&
            synced.ranged == 1 &&
            synced.magic == 1 &&
            synced.prayer == 1 &&
            synced.summoning == 1
        ) {
            break
        }
    }

    player.message("Your combat level has been synced to ${player.combatLevel} against ${target.name}'s combat level of $targetCombatLevel.")
    player.message("Your new levels are: ${player.syncedLevels}")
}
