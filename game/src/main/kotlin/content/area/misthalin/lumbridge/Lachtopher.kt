package content.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.entity.obj.door.Door
import content.entity.obj.door.Door.replace
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

private val LACHTOPHER_HOME = Tile(3231, 3207, 1)
private val LACHTOPHER_HANGOUT = Tile(3227, 3237, 0)
private const val GO_HOME_HOUR = 20
private const val GO_HANGOUT_HOUR = 10
private const val LACHTOPHER_STRING_ID = "lachtopher"
private var LACHTOPHER: NPC? = null


class Lachtopher(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val downstairs = Tile(3232, 3209, 0)
        val upstairs = Tile(3229, 3209, 1)
        val eastTile = Tile(3234, 3207, 0)
        val westTile = Tile(3233, 3207, 0)
        val homeDoor = GameObjects.at(eastTile).first()

        val schedule = NpcScheduleController(
            npcProvider = { LACHTOPHER },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(eastTile, null, "lachtopher_to_door") {
                            replace(homeDoor, homeDoor.def, "_opened", "_closed", 1, 1, 30, true,
                                Door.revert(homeDoor.def, homeDoor, "open"))
                            travelTo(downstairs, null, "lachtopher_to_stairs_bottom") {
                                tele(upstairs)
                                walkTo(LACHTOPHER_HOME)
                                it["spawn_tile"] = LACHTOPHER_HOME
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HANGOUT_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(upstairs, null, "lachtopher_to_stairs_top") {
                            tele(downstairs)
                            travelTo(westTile, null, "lachtopher_stairs_to_door") {

                            }
                        }
                    },
                ),
            ),
        )

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
