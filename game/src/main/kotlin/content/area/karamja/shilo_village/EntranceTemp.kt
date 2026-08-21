package content.area.karamja.shilo_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

// Purpose: Allow entry into Shilo Village until a proper version of the "Shilo Village" quest is finished.
class EntranceTemp : Script {
    init {
        npcOperate("Talk-to", "mosol_rei") { (target) ->
            npc<Neutral>("Would you like to enter Shilo Village?")
            choice {
                option<Neutral>("Yes please!") {
                    tele(2865, 2952)
                }
                option<Neutral>("No, but thanks for the offer.") {
                    npc<Happy>("That's fine and thanks for your interest.")
                }
            }
        }
    }
}