package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcLocation
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.NpcRouteTarget
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

private var milkSellerNpc: NPC? = null
private var gertrudeNpc: NPC? = null
private const val MILK_SELLER_STRING_ID = "milk_seller"
private val MILK_SELLER_SPAWN_TILE: Tile = Tile(3189, 3293, 0)

private const val TRAVEL_WARN_HOUR = 4
private const val TRAVEL_HOUR = 5

private val LUMBRIDGE = Tile(3225, 3225, 0)
private val VARROCK = Tile(3209, 3435, 0)
private val EDGEVILLE = Tile(3101, 3504, 0)
private val FALADOR = Tile(2968, 3377, 0)
private val PORT_SARIM = Tile(3043, 3256, 0)
private val RIMMINGTON = Tile(2959, 3218, 0)
private val DRAYNOR = Tile(3080, 3250, 0)

private val GERTRUDE_DOOR_OUTSIDE = Tile(3151, 3412, 0)
private val GERTRUDE_DOOR_INSIDE = Tile(3151, 3411, 0)

private val LUMBRIDGE_MARKET = Tile(3221, 3243, 0)
private val VARROCK_SOUTH_MINE = Tile(3179, 3363, 0)
private val BLUE_MOON_INN = Tile(3229, 3399, 0)
private val SOUTH_WEST_GE = Tile(3134, 3461, 0)
private val EDGEVILLE_FAIRY_RING = Tile(3129, 3497, 0)
private val PORT_SARIM_TELE_SPOT = Tile(3043, 3270, 0)
private val WIZARDS_TOWER_FAIRY_RING = Tile(3107, 3149, 0)

private val GERTRUDE_DOOR = GameObjects.at(GERTRUDE_DOOR_OUTSIDE).first()

private val SELL_LOCATIONS = arrayOf(
    LUMBRIDGE,
    VARROCK,
    EDGEVILLE,
    FALADOR,
    PORT_SARIM,
    RIMMINGTON,
    DRAYNOR,
    MILK_SELLER_SPAWN_TILE,
)

// Travel destinations as navmesh locations. Area-less on purpose: these are
// single-tile stops, and isAt() tolerates the last graph node landing within
// two tiles. Only genuinely large arrival zones (shop floors, taverns) need
// a named area.
private val LUMBRIDGE_LOC = NpcLocation("milk_seller_lumbridge", LUMBRIDGE)
private val LUMBRIDGE_MARKET_LOC = NpcLocation("lumbridge_market", LUMBRIDGE_MARKET)
private val SPAWN_LOC = NpcLocation("milk_seller_spawn", MILK_SELLER_SPAWN_TILE)
private val VARROCK_MINE_LOC = NpcLocation("varrock_south_mine", VARROCK_SOUTH_MINE)
private val BLUE_MOON_LOC = NpcLocation("blue_moon_inn", BLUE_MOON_INN)
private val VARROCK_LOC = NpcLocation("varrock", VARROCK)
private val GERTRUDE_OUTSIDE_LOC = NpcLocation("gertrude_door_outside", GERTRUDE_DOOR_OUTSIDE)
private val GERTRUDE_INSIDE_LOC = NpcLocation("gertrude_door_inside", GERTRUDE_DOOR_INSIDE)
private val SOUTH_WEST_GE_LOC = NpcLocation("south_west_ge", SOUTH_WEST_GE)
private val EDGEVILLE_LOC = NpcLocation("edgeville", EDGEVILLE)
private val PORT_SARIM_TELE_LOC = NpcLocation("port_sarim_tele_spot", PORT_SARIM_TELE_SPOT)

private var routeIndex = 0

class MilkSellerSchedule(graph: NavigationGraph) : Script {
    private val routeExecutor: NpcRouteExecutor = GraphNpcRouteExecutor(NpcNavMeshRouteFinder(graph))

    init {
        val schedule = NpcScheduleController(
            npcProvider = { milkSellerNpc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    TRAVEL_HOUR,
                    ScheduleAction.Custom {
                        travelSomewhereNew(it)
                    },
                ),
                ScheduleTransition(
                    TRAVEL_WARN_HOUR,
                    ScheduleAction.Custom {
                        it.say("Last call! We're heading out soon.")
                    },
                ),
            ),
        )

        npcSpawn(MILK_SELLER_STRING_ID) {
            milkSellerNpc = this
            this["full_pathfinding"] = true

            NpcSchedules.registry.register(schedule)
        }
        npcDespawn(MILK_SELLER_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (milkSellerNpc == this) {
                milkSellerNpc = null
            }
        }
        npcSpawn("gertrude") {
            gertrudeNpc = this
        }
        npcDespawn("gertrude") {
            if (gertrudeNpc == this) {
                gertrudeNpc = null
            }
        }
    }

    private fun travelSomewhereNew(npc: NPC) {
        npc.say("Well Bessie, time we headed out.")
        val destination = nextSellingLocation()
        when (destination) {
            LUMBRIDGE -> travelToLumbridge(npc)
            VARROCK -> travelToVarrock(npc)
            EDGEVILLE -> varrockToEdgeville(npc)
            FALADOR -> edgevilleToFalador(npc)
            RIMMINGTON -> faladorToRimmington(npc)
            PORT_SARIM -> rimmingtonToPortSarim(npc)
            DRAYNOR -> portSarimToDraynor(npc)
            MILK_SELLER_SPAWN_TILE -> draynorToSpawn(npc)
        }
    }

    private fun nextSellingLocation(): Tile {
        val destination = SELL_LOCATIONS[routeIndex]

        routeIndex = (routeIndex + 1) % SELL_LOCATIONS.size

        return destination
    }

    private fun draynorToSpawn(npc: NPC) {
        npc.enqueue("milk_seller_to_spawn") {
            patrolDelay("wizard_tower_to_draynor")
            say("Home sweet home.")
            setSpawnAndWander(npc, MILK_SELLER_SPAWN_TILE)
        }
    }

    private fun portSarimToDraynor(npc: NPC) {
        routeExecutor.move(
            npc,
            NpcRouteTarget(
                location = PORT_SARIM_TELE_LOC,
                queueName = "milk_seller_to_tele",
                onArrival = { npc ->
                    npc.enqueue("milk_seller_to_draynor") {
                        say("Could you please take us away, Bessie?")
                        pause(3)
                        // Moo
                        pause(3)
                        tele(WIZARDS_TOWER_FAIRY_RING)
                        say("Thanks, Bessie.")
                        pause(3)
                        // Moo
                        pause(3)
                        patrolDelay("wizard_tower_to_draynor")
                        setSpawnAndWander(npc, DRAYNOR)
                    }
                },
            ),
        )
    }

    private fun rimmingtonToPortSarim(npc: NPC) {
        npc.enqueue("milk_seller_to_port_sarim") {
            patrolDelay("rimmington_to_port_sarim")
            say("Let's be sure to sell to the bar while we're here.")
            // Moo
            setSpawnAndWander(npc, PORT_SARIM)
        }
    }

    private fun faladorToRimmington(npc: NPC) {
        npc.enqueue("milk_seller_to_rimmington") {
            enqueue("milk_seller_rimmington_banter") {
                patrolDelay("falador_to_rimmington")
                say("Maybe the local witch here needs milk for some brews.")
                // Moo
                setSpawnAndWander(npc, RIMMINGTON)
            }
        }
    }

    private fun edgevilleToFalador(npc: NPC) {
        npc.enqueue("milk_seller_to_falador") {
            enqueue("milk_seller_falador_banter") {
                patrolDelay("edgeville_to_falador")
                say("I hear the white knights need plenty of milk to drink.")
                // Moo
                setSpawnAndWander(npc, FALADOR)
            }
        }
    }

    private fun travelToLumbridge(npc: NPC) {
        routeExecutor.move(
            npc,
            NpcRouteTarget(
                location = LUMBRIDGE_LOC,
                queueName = "milk_seller_to_lumbridge",
                onArrival = { npc ->
                    npc.say("Careful about Bob, Bessie. I think he was looking at you funny.")
                    // Moo
                    setSpawnAndWander(npc, LUMBRIDGE)
                },
            ),
        )
    }

    private fun travelToVarrock(npc: NPC) {
        routeExecutor.move(
            npc,
            NpcRouteTarget(
                location = BLUE_MOON_LOC,
                queueName = "milk_seller_to_inn",
                onArrival = { npc ->
                    npc.say("Hey, just dropping off the usual.") // bot network prefers back door, needs opening
                    routeExecutor.move(
                        npc,
                        NpcRouteTarget(
                            location = VARROCK_LOC,
                            queueName = "milk_seller_to_varrock",
                            onArrival = { npc ->
                                npc.say("We should remember to stop by Gertrude's on the way over, Bessie.")
                                // Moo
                                setSpawnAndWander(npc, VARROCK)
                            },
                        ),
                    )
                },
            ),
        )
    }

//    private fun travelToVarrock(npc: NPC) {
//        routeExecutor.move(
//            npc,
//            NpcRouteTarget(
//                location = LUMBRIDGE_MARKET_LOC,
//                queueName = "milk_seller_lumbridge_to_market",
//                onArrival = { npc ->
//                    routeExecutor.move(
//                        npc,
//                        NpcRouteTarget(
//                            location = SPAWN_LOC,
//                            queueName = "milk_seller_lumbridge_to_spawn",
//                            onArrival = { npc ->
//                                routeExecutor.move(
//                                    npc,
//                                    NpcRouteTarget(
//                                        location = GATE_SOUTH_LOC,
//                                        queueName = "milk_seller_spawn_to_gate",
//                                        onArrival = { npc ->
//                                            npc.tele(LUMBRIDGE_GATE_NORTH)
//                                            routeExecutor.move(
//                                                npc,
//                                                NpcRouteTarget(
//                                                    location = VARROCK_MINE_LOC,
//                                                    queueName = "milk_seller_gate_to_mines",
//                                                    onArrival = { npc ->
//                                                        routeExecutor.move(
//                                                            npc,
//                                                            NpcRouteTarget(
//                                                                location = BLUE_MOON_LOC,
//                                                                queueName = "milk_seller_mines_to_inn",
//                                                                onArrival = { npc ->
//                                                                    npc.say("Hey, just dropping off the usual.")
//                                                                    routeExecutor.move(
//                                                                        npc,
//                                                                        NpcRouteTarget(
//                                                                            location = VARROCK_LOC,
//                                                                            queueName = "milk_seller_inn_to_varrock",
//                                                                            onArrival = { npc ->
//                                                                                npc.say("We should remember to stop by Gertrude's on the way over, Bessie.")
//                                                                                // Moo
//                                                                                setSpawnAndWander(npc, VARROCK)
//                                                                            },
//                                                                        ),
//                                                                    )
//                                                                },
//                                                            ),
//                                                        )
//                                                    },
//                                                ),
//                                            )
//                                        },
//                                    ),
//                                )
//                            },
//                        ),
//                    )
//                },
//            ),
//        )
//    }

    private fun varrockToEdgeville(npc: NPC) {
        routeExecutor.move(
            npc,
            NpcRouteTarget(
                location = GERTRUDE_OUTSIDE_LOC,
                queueName = "milk_seller_varrock_to_gertrude",
                onArrival = { npc ->
                    npcOpenDoor(GERTRUDE_DOOR, 69420)
                    routeExecutor.move(
                        npc,
                        NpcRouteTarget(
                            location = GERTRUDE_INSIDE_LOC,
                            queueName = "milk_seller_walk_in_gertrudes_home",
                            onArrival = { npc ->
                                npc.enqueue("milk_seller_gertrude_talk") {
                                    milkSellerNpc?.mode = PauseMode
                                    say("Hey Gertrude, came to see if you need a top-off.")
                                    pause(3)
                                    gertrudeNpc?.say("Thank you dearie. Want a kitten?")
                                    pause(3)
                                    say("Oh no, thank you. Bessie is enough for me.")
                                    pause(3)
                                    // Moo
                                    pause(3)
                                    say("I better get back to it before she breaks the house down.")
                                    pause(3)
                                    gertrudeNpc?.say("Haha, take care dearie.")
                                    npcOpenDoor(GERTRUDE_DOOR, 5)
                                    routeExecutor.move(
                                        npc,
                                        NpcRouteTarget(
                                            location = SOUTH_WEST_GE_LOC,
                                            queueName = "milk_seller_gertrude_to_ge",
                                            onArrival = { npc ->
                                                npc.enqueue("milk_seller_bessie_teleport") {
                                                    say("Ugh... Guards... Bessie, could you please?")
                                                    pause(3)
                                                    // Moo...
                                                    pause(3)
                                                    tele(EDGEVILLE_FAIRY_RING)
                                                    say("Thanks, Bessie.")
                                                    pause(3)
                                                    // Moo.
                                                    routeExecutor.move(
                                                        npc,
                                                        NpcRouteTarget(
                                                            location = EDGEVILLE_LOC,
                                                            queueName = "milk_seller_ge_to_edgeville",
                                                            onArrival = { npc ->
                                                                npc.say("I hope the programmer remembers to enter flavor text here...")
                                                                // Moo
                                                                setSpawnAndWander(npc, EDGEVILLE)
                                                            },
                                                        ),
                                                    )
                                                }
                                            },
                                        ),
                                    )
                                }
                            },
                        ),
                    )
                },
            ),
        )
    }
}
