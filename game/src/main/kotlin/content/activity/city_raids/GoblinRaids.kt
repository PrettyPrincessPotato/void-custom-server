package content.activity.city_raids

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

private val GOBLIN_RAID_VARROCK_AREA : Tile = Tile(3175, 3428)
private val GOBLIN_RAID_MOVEMENT_TILE_TEST : Tile = Tile(3168, 3433)
private val GOBLIN_IDS = intArrayOf(3264, 3265, 3266, 3267)

private const val GOBLIN_ID_TEST = "3264"
private const val RUN_AMOUNT = 5

class GoblinRaids : Script {
    init{
        println("GoblinRaids.kt Loaded...")
        worldSpawn {
            println("GoblinRaids - worldSpawn")
            World.timers.start("goblin_raid_timer")
        }

        worldTimerStart("goblin_raid_timer") { TimeUnit.SECONDS.toTicks(6) }
        worldTimerTick("goblin_raid_timer") {
                println("spawngobbo")
                spawnGobbo()
                Timer.CONTINUE
            }
    }
}

fun spawnGobbo() {
    println("Starting goblin raid")
    val gobbo = NPCs.addRandom(GOBLIN_ID_TEST, GOBLIN_RAID_VARROCK_AREA.toCuboid(10)) ?: NPCs.add(GOBLIN_ID_TEST, GOBLIN_RAID_VARROCK_AREA)
    gobbo.huntMode = "aggressive"
    gobbo.walkTo(GOBLIN_RAID_MOVEMENT_TILE_TEST)
    println(gobbo)
    println(NPCs.at(GOBLIN_RAID_VARROCK_AREA))
    println("End of goblin raid spawning")
}
