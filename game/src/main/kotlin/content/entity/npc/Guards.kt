package content.entity.npc

import content.entity.combat.killer
import content.skill.summoning.canFight
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue

// Combat definitions used by generic citizens and ducks; they have combat stats but are not guard targets
private val citizenCombatDefs = setOf("man", "woman", "farmer", "duck_walk", "duck_swim")

class Guards : Script {
    init {
        huntNPC("aggressive_npcs") { target ->
            if (
                id.contains("guard_") &&
                    !target.id.contains("guard") &&
                    !target.isFamiliar &&
                    target.canFight() &&
                    target.def["combat_def", ""] !in citizenCombatDefs
            ) {
                interactNpc(target, "Attack")
            }
        }

        npcDeath("*") {
            val guard = killer as? NPC ?: return@npcDeath
            when {
                guard.id.contains("guard_") -> {
                    guard.queue("rat_killer") {
                        guard.delay(2)
                        guard.anim("eat_drink")
                        guard.levels.set(Skill.Constitution, guard.levels.getMax(Skill.Constitution))
                        guard.walkTo(tile)
                    }
                }
            }
        }
    }
}
