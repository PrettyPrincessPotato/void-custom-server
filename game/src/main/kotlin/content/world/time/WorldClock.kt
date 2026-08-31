package content.world.time

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

const val HOUR_TICK_NAME = "ingame_hour_tick"
class WorldClock : Script {
    private val announcement = WorldTimeAnnouncement()

    init{
        worldSpawn {
            World.timers.start(HOUR_TICK_NAME)
        }

        worldTimerStart(HOUR_TICK_NAME){
            TimeUnit.MINUTES.toTicks(5) // 2 IRL hours = 1 full day in-game.
        }

        worldTimerTick(HOUR_TICK_NAME){
            advanceHour()
            Timer.CONTINUE // restarts the timer
        }

    }

    var hour: Int = 12
        private set

    val timeOfDay: TimeOfDay
        get() = when (hour) {
            in 5..7 -> TimeOfDay.DAWN
            in 8..17 -> TimeOfDay.DAY
            in 18..19 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }

    private fun advanceHour() {
        val previousTimeOfDay = timeOfDay

        hour = (hour + 1) % 24

        val newTimeOfDay = timeOfDay

        if (previousTimeOfDay != newTimeOfDay) {
            announcement.onTimeOfDayChanged(newTimeOfDay)
        }
    }
}
