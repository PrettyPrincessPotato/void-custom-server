package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.entity.npc.shop.general.isGeneralStoreOpen
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

// SAL is shorthand for shop_assistant_lumbridge
private var sALNpc: NPC? = null
private var sALSpawn: Tile? = null
private const val SAL_STRING_ID = "shop_assistant_lumbridge"

private const val GO_TO_CHURCH_HOUR = 5
private const val MAN_STORE_HOUR = 8
private const val GO_HOME_HOUR = 18
private const val GO_TO_BED_HOUR = 22

private val SAL_STAIRS_BOTTOM = Tile(3217, 3239, 0)
private val SAL_STAIRS_TOP = Tile(3214, 3239, 1)
private val SAL_SLEEP_SPOT = Tile(3211, 3240, 1)
private val CHURCH_SPOT = Tile(3242, 3211, 0)

class ShopAssistantLumbridgeSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { sALNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    GO_TO_CHURCH_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(SAL_STAIRS_TOP, "SAL_bed_to_stairs") {
                            tele(SAL_STAIRS_BOTTOM)
                            travelTo(CHURCH_SPOT, "SAL_store_to_church") {
                                setSpawnAndWander(it, CHURCH_SPOT, true)
                            }
                        }
                    },
                ),
                ScheduleTransition(
                    MAN_STORE_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(sALSpawn!!, "SAL_church_to_spawn") {
                            setSpawnAndWander(it, sALSpawn!!, true)
                        }
                    },
                ),
                ScheduleTransition(
                    GO_HOME_HOUR,
                    ScheduleAction.Custom {
                        it.say("See ya upstairs.")
                        it.travelTo(SAL_STAIRS_BOTTOM, "SAL_shop_to_bed") {
                            tele(SAL_STAIRS_TOP)
                            setSpawnAndWander(it, SAL_STAIRS_TOP)
                        }
                    },
                ),
                ScheduleTransition(
                    GO_TO_BED_HOUR,
                    ScheduleAction.Custom {
                        it.travelTo(SAL_SLEEP_SPOT, "SAL_to_sleep") {
                            face(Direction.NORTH)
                            mode = PauseMode
                        }
                    },
                ),
            ),
        )

        npcOperate("Trade", SAL_STRING_ID) {
            if (!isGeneralStoreOpen()) {
                it.target.say("I'm off the clock, try again when I'm at the store.")
                return@npcOperate
            }
        }
        npcSpawn(SAL_STRING_ID) {
            sALNpc = this
            sALSpawn = this.tile
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(SAL_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (sALNpc == this) {
                sALNpc = null
            }
        }
    }
}
