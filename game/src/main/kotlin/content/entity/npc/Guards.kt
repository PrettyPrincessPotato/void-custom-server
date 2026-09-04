package content.entity.npc

import content.entity.combat.killer
import content.skill.summoning.canFight
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue

class Guards : Script {
    init{
        // Blanket catch to have all guards attack random NPCs. Not clean yet but good enough.
        huntNPC("aggressive_npcs") { target ->
            if (id.contains("guard_") && !target.id.contains("guard") && !target.isFamiliar && target.canFight()) {
                interactNpc(target, "Attack")
            }
        }
        npcDeath("*") {
            val guard = killer as? NPC ?: return@npcDeath
            when {
                // Copy/Pasted from Lumbridge Guards, paramaters changed. Still unsure what val guard does fully other than somehow grabs the needed NPC.
                guard.id.contains("guard_") -> {
                    // Name theoretically looks like it can be anything I want.
                    guard.queue("rat_killer") {
                        guard.delay(2)
                        guard.anim("eat_drink")
                        // From wiki: Every time they kill a monster, they eat a piece of food which heals them to full health, and walk over to the square of their death as to collect a drop.
                        guard.levels.set(Skill.Constitution, guard.levels.getMax(Skill.Constitution))
                        guard.walkTo(tile)
                    }
                }
            }
        }
    }

}