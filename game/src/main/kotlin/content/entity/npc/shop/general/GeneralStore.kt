package content.entity.npc.shop.general

import content.area.asgarnia.falador.ASSISTANT_LEAVE_HOUR
import content.area.asgarnia.falador.ASSISTANT_RETURN_HOUR
import content.area.asgarnia.falador.ASSISTANT_STRING_ID
import content.entity.npc.shop.openShop
import content.entity.npc.shop.time.ShopSchedule
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC

class GeneralStore : Script {
    private val generalStoreSchedule = ShopSchedule(
        openingHour = 8,
        closingHour = 18
    )

    fun isGeneralStoreOpen(): Boolean {
        return generalStoreSchedule.isOpen(WorldTime.hour)
    }

    fun checkFaladorAssistant(target: NPC): Boolean {
        val isAway =
            target.id == ASSISTANT_STRING_ID &&
                    WorldTime.hour !in ASSISTANT_RETURN_HOUR..<ASSISTANT_LEAVE_HOUR

        if (isAway) {
            target.say("I'm off the clock.")
            return true
        }

        return false
    }


    init {
        fun openGeneralStore(
            target: NPC,
            open: (String) -> Unit
        ) {

            if (checkFaladorAssistant(target)) {
                return
            }

            val shop = target.def.getOrNull<String>("shop") ?: return
            if (isGeneralStoreOpen()) {
                open(shop)
            } else {
                target.say("Sorry, the shop is closed for now. Please come back later.")
            }
        }

        npcOperate("Trade", "shopkeeper*,shop_assistant*") { (target) ->
            openGeneralStore(target) { shop ->
                openShop(shop)
            }
        }

        npcOperate("Talk-to", "shopkeeper*") { (target) ->
            npc<Idle>("Can I help you at all?")
            choice {
                option("Yes please. What are you selling?") {
                    openGeneralStore(target) { shop ->
                        openShop(shop)
                    }
                }
                option("How should I use your shop?") {
                    if (target.id.endsWith("lumbridge")) {
                        npc<Neutral>("I'm glad you ask! The shop has two sections to it: 'Main stock' and 'Free sample items'.")
                        npc<Idle>("From 'Main Stock' you can buy as many of the stocked items as you wish. I also offer free samples to help get you started and to keep you coming back.")
                        npc<Idle>("Once you take a free sample, I won't give you another for about half an hour. I'm not make of money, you know!")
                        npc<Idle>("You can also sell most items to the shop.")
                        player<Happy>("Thank you.")
                    } else {
                        npc<Neutral>("I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
                        player<Happy>("Thank you.")
                    }
                }
                option("No thanks.")
            }
        }

        npcOperate("Talk-to", "shop_assistant*") { (target) ->
            if (target.id.endsWith("musa_point")) {
                npc<Happy>("It's a beautiful day today, no? Can I do anything for you?")
            } else {
                npc<Happy>("Can I help you at all?")
            }
            choice {
                option("Yes please. What are you selling?") {
                    openGeneralStore(target) { shop ->
                        openShop(shop)
                    }
                }
                option("How should I use your shop?") {
                    npc<Neutral>("I'm glad you ask! You can buy as many of the items stocked as you wish. You can also sell most items to the shop.")
                    player<Happy>("Thank you.")
                }
                option<Idle>("No thanks.")
            }
        }
    }
}
