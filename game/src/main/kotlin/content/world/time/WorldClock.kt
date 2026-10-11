package content.world.time

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.boolArg
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private var WORLD_CLOCK_DEBUG = false

const val HOUR_TICK_NAME = "ingame_hour_tick"
class WorldClock : Script {
    private val announcement = WorldTimeAnnouncement()
    private val debugInt = 15 // fast cycling for debugging
    private val liveInt = 5 // 2 IRL hours = 1 full day in-game

    private fun toggleDebug() {
        WORLD_CLOCK_DEBUG = !WORLD_CLOCK_DEBUG
        World.timers.stop(HOUR_TICK_NAME)
        World.timers.start(HOUR_TICK_NAME)
    }

    init {
        adminCommand("debug_clock", desc = "Toggles true/false the debug day/night timer.", handler = { toggleDebug() })

        worldSpawn {
            World.timers.start(HOUR_TICK_NAME)
        }

        worldTimerStart(HOUR_TICK_NAME) {
            if (WORLD_CLOCK_DEBUG) {
                TimeUnit.SECONDS.toTicks(debugInt)
            } else {
                TimeUnit.MINUTES.toTicks(liveInt)
            }
        }

        worldTimerTick(HOUR_TICK_NAME) {
            val previousTimeOfDay = WorldTime.timeOfDay

            WorldTime.advanceHour()

            val currentTimeOfDay = WorldTime.timeOfDay

            if (currentTimeOfDay != previousTimeOfDay) {
                announcement.onTimeOfDayChanged(currentTimeOfDay)
            }

            if (WORLD_CLOCK_DEBUG) {
                TimeUnit.SECONDS.toTicks(debugInt)
            } else {
                TimeUnit.MINUTES.toTicks(liveInt)
            } // restarts the timer
        }
    }
}
