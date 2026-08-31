package content.world.time

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Players


class WorldTimeAnnouncement {
    fun onTimeOfDayChanged(timeOfDay: TimeOfDay) {
        when (timeOfDay) {
            TimeOfDay.DAWN -> announce("The sun rises over ${Settings["server.name"]}.")
            TimeOfDay.DAY -> announce("The day is now in full swing.")
            TimeOfDay.DUSK -> announce("The sun begins to set.")
            TimeOfDay.NIGHT -> announce("Night falls across the land.")
        }
    }
    fun announce(message: String){
        if (Settings["world.messages", true]) {
            for (player in Players) {
                player.message("${Colours.DARK_RED.toTag()} $message")
            }
        }
    }
}
