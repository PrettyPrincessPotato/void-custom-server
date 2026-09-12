package content.area.kandarin.tree_gnome_stronghold

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Yes
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

private const val ROMETTI_STRING_NAME = "rometti_grand_tree"
private const val ROMETTIS_SHOP_STRING_NAME = "romettis_fine_fashions"

/**
 * Dialogue Source: https://oldschool.runescape.wiki/w/Transcript:Rometti
 * Identical between RS3 and OSRS.
 */

class Rometti : Script {
    init {
        npcOperate("Trade", ROMETTI_STRING_NAME) {
            openShop(ROMETTIS_SHOP_STRING_NAME)
        }
        npcOperate("Talk-to", ROMETTI_STRING_NAME) {
            player<Neutral>("Hello.")
            npc<Happy>("Hello traveller. Have a look at my latest range of gnome fashion. Rometti is the ultimate label in gnome high society.")
            player<Neutral>("Really.")
            npc<Neutral>("Pastels are all the rage this season.")
            choice {
                option<Neutral>("I've no time for fashion.") { npc<Neutral>("Hmm... I did wonder.") }
                option<Yes>("OK then, let's have a look.") { openShop(ROMETTIS_SHOP_STRING_NAME) }
            }
        }
    }
}
