package content.entity.npc.movement

import content.bot.behaviour.BotWorld
import content.bot.behaviour.navigation.NavigationGraph
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

interface NpcRouteExecutor {
    fun move(npc: NPC, target: NpcRouteTarget)
}

class NativeNpcRouteExecutor : NpcRouteExecutor {
    override fun move(npc: NPC, target: NpcRouteTarget) {
        val location = target.location

        if (npc.tile in location.area) {
            target.onArrival(npc)
            return
        }

        npc.travelTo(
            destination = location.tile,
            destinationArea = location.area,
            dialogue = target.dialogue,
            queueName = target.queueName,
        ) {
            target.onArrival(this)
        }
    }
}
class BotNavMeshRouteFinder(
    private val graph: NavigationGraph
) : RouteFinder {
    override fun find(context: RouteContext, target: NpcLocation): List<Tile>? {
        // if target has navTag / area, use graph logic
        // else return null
    }
}
class BotNavMeshRouteExecutor(
    private val finder: RouteFinder
) : NpcRouteExecutor {
    override fun move(npc: NPC, target: NpcRouteTarget) {
        val route = finder.find(
            RouteContext(
                tile = npc.tile,
                level = npc.tile.level,
                areaTags = target.location.navTag?.let { setOf(it) } ?: emptySet()
            ),
            target.location
        ) ?: run {
            NativeNpcRouteExecutor().move(npc, target)
            return
        }

        // execute the returned route tiles with a walk sequence
        // or queue the needed steps/actions
    }
}