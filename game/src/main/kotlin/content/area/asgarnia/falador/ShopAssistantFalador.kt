package content.area.asgarnia.falador

import content.world.time.HourChangeListener
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC

private const val ASSISTANT_LEAVE_HOUR = 17
private const val ASSISTANT_RETURN_HOUR = 8
private const val ASSISTANT_ID = 527

private enum class AssistantState {
    AT_SHOP,
    GOING_TO_TAVERN,
    AT_TAVERN,
    GOING_TO_SHOP
}

class ShopAssistantFalador : Script, HourChangeListener {
    private var state = AssistantState.AT_SHOP

    init {
        WorldTime.subscribeToHourChanges(this)
    }

    override fun onHourChanged(previousHour: Int, currentHour: Int) {
        when (state) {
            AssistantState.AT_SHOP -> {
                if (currentHour == ASSISTANT_LEAVE_HOUR) {
                    leaveForTavern()
                }
            }

            AssistantState.GOING_TO_TAVERN -> {
                if (hasReachedTavern()) {
                    state = AssistantState.AT_TAVERN
                }
            }

            AssistantState.AT_TAVERN -> {
                if (currentHour == ASSISTANT_RETURN_HOUR) {
                    returnToShop()
                }
            }

            AssistantState.GOING_TO_SHOP -> {
                if (hasReachedShop()) {
                    state = AssistantState.AT_SHOP
                }
            }
        }
    }

    private fun leaveForTavern() {
        state = AssistantState.GOING_TO_TAVERN
        // Start movement.
    }

    private fun returnToShop() {
        state = AssistantState.GOING_TO_SHOP
        // Start movement.
    }

    private fun hasReachedTavern(): Boolean = false
    private fun hasReachedShop(): Boolean = false
}
