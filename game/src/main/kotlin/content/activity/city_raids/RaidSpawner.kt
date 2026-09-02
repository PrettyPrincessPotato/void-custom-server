package content.activity.city_raids

import content.activity.city_raids.goblins.GoblinRaidSystem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

private const val DEBUG = false

private const val GOBLIN_RAID_TIMER = "goblin_raid_timer"

private val raidManager = RaidManager()
private val goblinRaidSystem = GoblinRaidSystem(raidManager)

class RaidSpawner : Script {
    init {
        huntNPC("aggressive_npcs") { target ->
            with(raidManager) {
                if (this@huntNPC.canAttackRaidTarget(target)) {
                    interactNpc(target, "Attack")
                }
            }
        }

        worldSpawn {
            World.timers.start(GOBLIN_RAID_TIMER)
        }
        worldTimerStart(GOBLIN_RAID_TIMER) {
            if(DEBUG){
                TimeUnit.SECONDS.toTicks(5)
            } else {
                TimeUnit.MINUTES.toTicks(5)
            }
        }
        worldTimerTick(GOBLIN_RAID_TIMER){
            goblinRaidSystem.tick()
            Timer.CONTINUE
        }
    }
}

