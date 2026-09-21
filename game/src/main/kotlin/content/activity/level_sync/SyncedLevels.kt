package content.activity.level_sync

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

private const val SYNCED_LEVELS = "synced_levels"

private object NoSyncedLevels

var Player.syncedLevels: SyncedLevels?
    get() = this[SYNCED_LEVELS] as? SyncedLevels
    set(value) {
        this[SYNCED_LEVELS] = value ?: NoSyncedLevels
    }

data class SyncedLevels(
    val attack: Int,
    val strength: Int,
    val defence: Int,
    val hitpoints: Int,
    val ranged: Int,
    val magic: Int,
    val prayer: Int,
    val summoning: Int,
)

fun SyncedLevels.level(skill: Skill): Int = when (skill) {
    Skill.Attack -> attack
    Skill.Strength -> strength
    Skill.Defence -> defence
    Skill.Constitution -> hitpoints
    Skill.Ranged -> ranged
    Skill.Magic -> magic
    Skill.Prayer -> prayer
    Skill.Summoning -> summoning
    else -> error("Skill $skill is not a combat skill")
}

fun Player.combatMaxHitpoints(): Int = syncedLevels?.hitpoints ?: levels.getMax(Skill.Constitution)

fun Player.combatCurrentLevel(skill: Skill): Int {
    val synced = syncedLevels ?: return levels.get(skill)

    val normalMax = levels.getMax(skill)
    val normalCurrent = levels.get(skill)
    val boostOrDrain = normalCurrent - normalMax

    return (synced.level(skill) + boostOrDrain).coerceAtLeast(1)
}

fun Player.effectiveLevels(): SyncedLevels = syncedLevels ?: SyncedLevels(
    attack = levels.getMax(Skill.Attack),
    strength = levels.getMax(Skill.Strength),
    defence = levels.getMax(Skill.Defence),
    hitpoints = levels.getMax(Skill.Constitution),
    ranged = levels.getMax(Skill.Ranged),
    magic = levels.getMax(Skill.Magic),
    prayer = levels.getMax(Skill.Prayer),
    summoning = levels.getMax(Skill.Summoning),
)
