package content.entity.npc.shop.fishing

import content.entity.npc.shop.openShop
import content.entity.npc.shop.time.ShopSchedule
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script

private val fishingShopSchedule = ShopSchedule(
    openingHour = 8,
    closingHour = 18,
)

fun isFishingShopOpen(): Boolean = fishingShopSchedule.isOpen(WorldTime.hour)

class FisheryShop : Script {
    init {
        npcOperate("Trade", "hank") { (target) ->
            if (!isFishingShopOpen()) {
                target.say("The shop is closed for now, come back later.")
                return@npcOperate
            }
            val shop = target.def.getOrNull<String>("shop") ?: return@npcOperate
            openShop(shop)
        }
    }
}
