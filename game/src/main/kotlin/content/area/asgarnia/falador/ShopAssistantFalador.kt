package content.area.asgarnia.falador

import content.world.time.HourChangeListener
import content.world.time.WorldTime
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

private const val ASSISTANT_LEAVE_HOUR = 17
private const val ASSISTANT_RETURN_HOUR = 8
private const val ASSISTANT_STRING_ID = "shop_assistant_falador"

/**
 * // Main shopkeep will eventually use these
 * // TODO: Move somewhere more central
 */
val TAVERN_AREA = Areas["rising_sun_inn"]
val SHOP_AREA = Areas["falador_general_store"]

val TAVERN_TILE = Tile(2957, 3372)
val SHOP_TILE = Tile(2956, 3389)
/**
 * End of TODO
 */

private enum class AssistantState {
    AT_SHOP,
    GOING_TO_TAVERN,
    AT_TAVERN,
    GOING_TO_SHOP
}

class ShopAssistantFalador : Script, HourChangeListener {
    private var shopkeeper: NPC? = null
    private var state = AssistantState.AT_SHOP

    init {
        WorldTime.subscribeToHourChanges(this)

        npcSpawn(ASSISTANT_STRING_ID) {
            shopkeeper = this
            this["full_pathfinding"] = true
        }

    }

    override fun onHourChanged(previousHour: Int, currentHour: Int) {
        when (state) {
            AssistantState.AT_SHOP -> {
                if (currentHour == ASSISTANT_LEAVE_HOUR) {
                    shopkeeper?.collision = CollisionStrategies.Normal
                    leaveForTavern()
                }
            }

            AssistantState.GOING_TO_TAVERN -> {
                if (hasReachedTavern()) {
                    shopkeeper?.collision = CollisionStrategies.Indoors
                    state = AssistantState.AT_TAVERN
                }
            }

            AssistantState.AT_TAVERN -> {
                if (currentHour == ASSISTANT_RETURN_HOUR) {
                    shopkeeper?.collision = CollisionStrategies.Normal
                    returnToShop()
                }
            }

            AssistantState.GOING_TO_SHOP -> {
                if (hasReachedShop()) {
                    shopkeeper?.collision = CollisionStrategies.Indoors
                    state = AssistantState.AT_SHOP
                }
            }
        }
    }

    private fun leaveForTavern() {
        val destination = TAVERN_AREA.random()

        shopkeeper?.let { npc ->
            npc["spawn_tile"] = TAVERN_TILE
            npc.say("Ah, finally. Clock-out time!")
            npc.walkTo(destination)
        }

        state = AssistantState.GOING_TO_TAVERN
    }

    private fun returnToShop() {
        val destination = SHOP_AREA.random()

        shopkeeper?.let { npc ->
            npc.say("Ugh... I think I drank too much...")
            npc["spawn_tile"] = SHOP_TILE
            npc.walkTo(destination)
        }

        state = AssistantState.GOING_TO_SHOP
    }

    private fun hasReachedTavern(): Boolean =
        shopkeeper?.tile in TAVERN_AREA
    private fun hasReachedShop(): Boolean =
        shopkeeper?.tile in SHOP_AREA
}
