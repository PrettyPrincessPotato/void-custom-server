package content.world.time

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

const val DEBUG = false

const val HOUR_TICK_NAME = "ingame_hour_tick"
class WorldClock : Script {
    private val announcement = WorldTimeAnnouncement()

    init{
        worldSpawn {
            World.timers.start(HOUR_TICK_NAME)
        }

        worldTimerStart(HOUR_TICK_NAME){
            if(DEBUG){
                TimeUnit.SECONDS.toTicks(5) // fast cycling for debugging
            } else {
                TimeUnit.MINUTES.toTicks(5) // 2 IRL hours = 1 full day in-game.
            }
        }

        worldTimerTick(HOUR_TICK_NAME){
            val previousTimeOfDay = WorldTime.timeOfDay

            WorldTime.advanceHour()

            val currentTimeOfDay = WorldTime.timeOfDay

            if (currentTimeOfDay != previousTimeOfDay) {
                announcement.onTimeOfDayChanged(currentTimeOfDay)
            }

            Timer.CONTINUE // restarts the timer
        }

    }
}
