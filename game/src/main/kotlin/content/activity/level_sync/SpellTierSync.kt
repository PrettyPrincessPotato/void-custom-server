package content.activity.level_sync

import content.skill.magic.spell.SpellRunes
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Synced players should deal spell damage as if they were a mage of their synced level.
 *
 * In 2011, table spells deal a fixed max hit regardless of magic level, so a synced
 * level-99 mage casting ice barrage would still hit 300. At load, the modern, ancient
 * and lunar spellbooks are scanned and a tier table is built mapping each magic level
 * to the best max hit among all damage spells that level can cast. Synced players'
 * spell damage is capped at their tier with a min, so low-tier spells are never buffed
 * and equipment bonuses still stack on top.
 */
class SpellTierSync : Script {

    init {
        buildTierTable()
    }

    companion object {
        private val books = listOf("modern_spellbook", "ancient_spellbook", "lunar_spellbook")

        /**
         * Max hits of the damage spells subject to the tier cap.
         */
        private var spellMaxHits: Map<String, Int> = emptyMap()

        /**
         * spellTierCap[Level] = the best max hit among damage spells requiring magic level ≤ L.
         */
        private val spellTierCap = IntArray(100)

        private fun buildTierTable() {
            val bestAtLevel = IntArray(100)
            val maxHits = mutableMapOf<String, Int>()
            for (book in books) {
                val definition = InterfaceDefinitions.getOrNull(book) ?: continue
                for ((_, component) in definition.components ?: continue) {
                    val spell = component.stringId
                    if (spell.isEmpty()) {
                        continue
                    }
                    val level = SpellRunes.magicLevel(book, spell) ?: continue
                    val maxHit = Tables.intOrNull("spells.$spell.max_hit") ?: continue
                    if (maxHit <= 0) {
                        continue
                    }
                    maxHits[spell] = maxOf(maxHits[spell] ?: 0, maxHit)
                    val clamped = level.coerceIn(1, 99)
                    bestAtLevel[clamped] = maxOf(bestAtLevel[clamped], maxHit)
                }
            }
            spellMaxHits = maxHits.toMap()
            // Prefix max: the cap at level L is the strongest spell castable at or below L.
            for (level in 2..99) {
                spellTierCap[level] = maxOf(bestAtLevel[level], spellTierCap[level - 1])
            }
        }

        /**
         * The strongest max hit a mage at [level] could deal with a table spell.
         */
        fun spellTierCap(level: Int): Int = spellTierCap[level.coerceIn(1, 99)]

        /**
         * Caps [damage] for a synced player casting [spell].
         *
         * Only table spells from the three spellbooks are capped; magic dart and other
         * level-scaled damage already use the synced level. Apply this to the base table
         * value so equipment bonuses still stack on top.
         */
        fun capSpellDamage(source: Character, spell: String, damage: Int): Int {
            val player = source as? Player ?: return damage
            val synced = player.syncedLevels ?: return damage
            if (spell == "magic_dart" || spell !in spellMaxHits) {
                return damage
            }
            return minOf(damage, spellTierCap(synced.magic))
        }

        /**
         * Caps a fixed-damage magic attack (e.g. the Saradomin sword charge) for a synced
         * player.
         */
        fun capFixedDamage(source: Character, damage: Int): Int {
            val player = source as? Player ?: return damage
            val synced = player.syncedLevels ?: return damage
            return minOf(damage, spellTierCap(synced.magic))
        }
    }
}
