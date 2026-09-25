package content.entity.death

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.area.wilderness.inMultiCombat
import content.entity.combat.damageDealers
import content.entity.combat.killer
import content.entity.player.inv.item.tradeable
import content.entity.player.logEvent
import content.skill.slayer.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.queue.queue

/**
 * personal-flavor: every contributor to a kill gets their own full loot roll and slayer
 * credit. Contributor drops land on each contributor's own tile instead of the kill tile,
 * so nobody can sweep the others' loot. Registered through the npcDeath hook rather than
 * editing NPCDeath so upstream updates to that file can't clobber this; the engine's
 * dropLoot still handles the killer's roll and the dungeoneering / clan loot-share paths.
 */
class ContributorLoot(private val tables: DropTables) : Script {

    private val logger = InlineLogger()

    init {
        npcDeath("*") { onDeath ->
            var killer = killer
            if (killer is NPC && killer.contains("owner_index")) {
                killer = Players.indexed(killer["owner_index", -1])
            }
            val npc = this
            val player = killer as? Player ?: return@npcDeath
            queue(name = "contributor_loot", 1) {
                val contributors = npc.damageDealers.keys
                    .filterIsInstance<Player>()
                    .filter { it != player }
                for (contributor in contributors) {
                    slay(contributor, npc)
                }
                if (!onDeath.dropItems || player.inDungeoneering || (npc.inMultiCombat && player["loot_share", false])) {
                    return@queue
                }
                val table = tables.get("${npc.def["drop_table", npc.id]}_drop_table") ?: return@queue
                val maximumRoll = if (npc.def.combat > 0) npc.def.combat * 10 else -1
                val multiplier = Settings["world.itemDropRate", 1.0]
                for (contributor in contributors) {
                    val drops = table.roll(maximumRoll = maximumRoll, player = contributor, multiplier = multiplier)
                        .filterNot { it.id == "nothing" }
                        .map { it.toItem() }
                        .filter { World.members || !it.def.members }
                    AuditLog.event(npc, "dropped", *drops.toTypedArray())
                    for (item in drops) {
                        if (item.id.contains("clue_scroll") || item.amount <= 0) {
                            continue
                        }
                        if (item.def.stackable == 0 && item.amount > 1) {
                            for (i in 0 until item.amount) {
                                FloorItems.add(contributor.tile, item.id, 1, charges = item.charges(), revealTicks = if (item.tradeable) 60 else FloorItems.NEVER, disappearTicks = 120, owner = contributor)
                            }
                        } else {
                            FloorItems.add(contributor.tile, item.id, item.amount, charges = item.charges(), revealTicks = if (item.tradeable) 60 else FloorItems.NEVER, disappearTicks = 120, owner = contributor)
                        }
                        logItems(contributor, item, npc)
                    }
                }
            }
        }
    }

    private fun slay(player: Player, npc: NPC) {
        if (player.slayerTask == "nothing" || !npc.categories.contains(player.slayerTask)) {
            return
        }
        val slayerExp = npc.def["slayer_xp", 0.0]
        if (slayerExp == 0.0) {
            logger.warn { "No slayer exp found for slain monster: $npc" }
            return
        }
        player.exp(Skill.Slayer, slayerExp)
        player.slayerTaskRemaining--
        if (player.slayerTaskRemaining == 0) {
            player.slayerStreak++
            var points = when (player.slayerMaster) {
                "mazchna" -> 15
                "vannaka" -> 60
                "chaeldar" -> 150
                "sumona" -> 180
                "duradel", "lapalok" -> 225
                "kuradal" -> 270
                else -> 0
            }
            when {
                player.slayerStreak.rem(50) == 0 -> {}
                player.slayerStreak.rem(10) == 0 -> points /= 3
                else -> points /= 15
            }
            player.slayerPoints += points
            player.inc("slayer_tasks_completed")
            player.clear("slayer_target")
            player.message("You've completed ${player.slayerStreak} tasks in a row and gain $points points. Return to a Slayer Master.")
        } else if (player.slayerTaskRemaining.rem(10) == 0) {
            player.message("You still need to kill ${player.slayerTaskRemaining} monsters to completed your current Slayer assignment.", ChatType.Filter)
        }
    }

    private fun logItems(player: Player, item: Item, npc: NPC) {
        Rows.getOrNull("log_item.${item.id}") ?: return
        player.logEvent("I found${item.def.name.an()} ${item.def.name}", "After killing${npc.def.name.an()} ${npc.def.name}, it dropped${if (item.def.name.endsWith("boots", ignoreCase = true)) " a pair of" else item.def.name.an()} ${item.def.name}")
    }
}
