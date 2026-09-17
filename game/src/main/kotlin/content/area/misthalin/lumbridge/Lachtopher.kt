package content.area.misthalin.lumbridge

import content.area.asgarnia.falador.SHOP_AREA
import content.area.asgarnia.falador.SHOP_TILE
import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcLocation
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

private val LACHTOPHER_HOME = Tile(3233, 3207, 0)
private val LACHTOPHER_DOOR_OUTSIDE = Tile(3234, 3207, 0)
private val LACHTOPHER_HANGOUT = Tile(3227, 3237, 0)
private const val GO_HOME_HOUR = 20
private const val GO_HANGOUT_HOUR = 6
private const val LACHTOPHER_STRING_ID = "lachtopher"
private var LACHTOPHER: NPC? = null


class Lachtopher(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    private val schedule = NpcScheduleController(
        npcProvider = { LACHTOPHER },
        routeExecutor = routeExecutor,
        scheduleTransitions = listOf(
            ScheduleTransition(
                GO_HOME_HOUR,
                ScheduleAction.Custom {
                    LACHTOPHER?.travelTo(LACHTOPHER_DOOR_OUTSIDE, null, "lachtopher_to_door") {

                        objectOperate("Open", "Door") {
                            walkToDelay(LACHTOPHER_HOME)
                            objectOperate("Close", "Door") {}
                        }
                    }
                },
            ),
            ScheduleTransition(
                GO_HANGOUT_HOUR,
                ScheduleAction.Custom {
                    LACHTOPHER?.say("This is when I go back!")
                },
            ),
        ),
    )

    init {
        npcSpawn(LACHTOPHER_STRING_ID) {
            LACHTOPHER = this
            this["full_pathfinding"] = true
            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(LACHTOPHER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if(LACHTOPHER == this){
                LACHTOPHER = null
            }
        }

        npcOperate("Talk-to", "lachtopher") {
            player<Happy>("Hello there.")
            npc<Bored>("Hello, I suppose. I'm Lachtopher. Could you lend me some money?")
            player<Quiz>("Lend you money? I really don't think so. Don't you have any of your own?")
            npc<Bored>("I spent it all and I can't be bothered to earn any more.")
            player<Quiz>("Right, and you want my hard-earned money instead? No chance!")
            npc<Bored>("You're just like my sister, Victoria. She won't give me any money.")
            player<Happy>("Your sister sounds like she has the right idea.")
            npc<Bored>("Yeah, I've heard it all before. 'Oh,' she says, 'It's easy to make money: just complete Tasks for cash.")
            player<Happy>("Well, if you want to make money...")
            npc<Bored>("That's just it. I don't want to make money. I just want to have money.")
            player<Angry>("I've had it with you! I don't think I've come across a less worthwhile person.")
            player<Angry>("I think I'll call you Lazy Lachtopher, from now on.")
        }
    }
}
