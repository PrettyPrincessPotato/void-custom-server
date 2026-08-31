package content.world

import content.world.time.DarknessLevel
import content.world.time.HourChangeListener
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

class WorldDarkness : Script {
    private val hourChangeListener = HourChangeListener { _, _ ->
        updateAllPlayers()
    }

    init {
        worldSpawn {
            WorldTime.subscribeToHourChanges(hourChangeListener)
            updateAllPlayers()
        }

        playerSpawn {
            updatePlayer(this)
        }
        moved {
            updatePlayer(this)
        }
    }

    private fun updateAllPlayers() {
        for (player in Players) {
            updatePlayer(player)
        }
    }

    private fun updatePlayer(player: Player) {
        val overworld = Areas["overworld"]
        if(overworld.contains(player.tile)){
            setDarkness(player, WorldTime.darknessLevel)
        } else {
            clearDarkness(player)
        }
    }

    private fun setDarkness(player: Player, level: DarknessLevel) {
        clearDarkness(player)

        when (level) {
            DarknessLevel.NONE -> Unit
            DarknessLevel.ONE -> player.open("level_one_darkness")
            DarknessLevel.TWO -> player.open("level_two_darkness")
        }
    }

    private fun clearDarkness(player: Player) {
        player.close("level_one_darkness")
        player.close("level_two_darkness")
    }
}

