package content.area.asgarnia.falador

import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

private const val ASSISTANT_LEAVE_HOUR = 17
private const val ASSISTANT_RETURN_HOUR = 8
private const val ASSISTANT_STRING_ID = "shop_assistant_falador"

/**
 * // Main shopkeep will eventually use these
 * // TODO: Move somewhere more central
 */
val TAVERN_AREA = Areas["rising_sun_inn"]
val SHOP_AREA = Areas["falador_general_store"]

val TAVERN_TILE = Tile(2957, 3372)
val SHOP_TILE = Tile(2956, 3389)
/**
 * End of TODO
 */

class ShopAssistantFalador : Script {
    private var shopkeeper: NPC? = null

    init {
        val schedule = NpcScheduleController(
            npcProvider = { shopkeeper },
            scheduleTransitions = listOf(
                ScheduleTransition(ASSISTANT_RETURN_HOUR, ScheduleAction.Travel(
                    destination = SHOP_TILE,
                    area = SHOP_AREA,
                    dialogue = "Ugh... I think I drank too much...",
                    queueName = "travel_to_shop",
                    onArrival = { npc ->
                        npc.collision = CollisionStrategies.Indoors
                    }
                )),
                ScheduleTransition(ASSISTANT_LEAVE_HOUR, ScheduleAction.Travel(
                    destination = TAVERN_TILE,
                    area = TAVERN_AREA,
                    dialogue = "Ahh, finally clock-out time...",
                    queueName = "travel_to_tavern",
                    onArrival = { npc ->
                        npc.collision = CollisionStrategies.Indoors
                    }
                ))
            )
        )

        npcSpawn(ASSISTANT_STRING_ID) {
            shopkeeper = this
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }

        npcDespawn(ASSISTANT_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (shopkeeper === this) {
                shopkeeper = null
            }
        }
    }
}

