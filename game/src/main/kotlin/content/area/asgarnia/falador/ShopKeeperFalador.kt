package content.area.asgarnia.falador

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.NpcRouteTarget
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.world.time.WorldTime
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.queue.queue as enqueue

private const val SHOP_KEEPER_STRING_ID = "shopkeeper_falador"

class ShopKeeperFalador(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = NativeNpcRouteExecutor()
    private var shopkeeper: NPC? = null

    init {
        val schedule = NpcScheduleController(
            npcProvider = { shopkeeper },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    ASSISTANT_LEAVE_HOUR,
                    ScheduleAction.Custom{
                        shopkeeper?.enqueue("assistant_leaving"){
                            shopkeeper?.delay(5)
                            shopkeeper?.say("Don't let the door hit you on your way out...")
                        }
                    }
                )
            )
        )

        npcSpawn(SHOP_KEEPER_STRING_ID) {
            shopkeeper = this
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }

        npcDespawn(SHOP_KEEPER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (shopkeeper === this) {
                shopkeeper = null
            }
        }
    }
}