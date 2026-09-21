package content.schedules.area.misthalin.lumbridge

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.movement.npcOpenDoor
import content.entity.npc.movement.setSpawnAndWander
import content.entity.npc.movement.travelTo
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
private var milkSellerSpawnTile: Tile? = null

private const val TRAVEL_WARN_HOUR = 4
private const val TRAVEL_HOUR = 5

private val LUMBRIDGE = Tile(3225, 3221, 0)
private val VARROCK = Tile(3209, 3435, 0)
private val EDGEVILLE = Tile(3101, 3504, 0)
private val FALADOR = Tile(2968, 3377, 0)
private val PORT_SARIM = Tile(3043, 3256, 0)
private val RIMMINGTON = Tile(2959, 3218, 0)
private val DRAYNOR = Tile(3080, 3250, 0)

private val LUMBRIDGE_GATE_SOUTH = Tile(3177, 3315, 0)
private val LUMBRIDGE_GATE_NORTH = Tile(3177, 3316, 0)
private val GERTRUDE_DOOR_OUTSIDE = Tile(3151, 3412, 0)
private val GERTRUDE_DOOR_INSIDE = Tile(3151, 3411, 0)

private val LUMBRIDGE_MARKET = Tile(3221, 3243, 0)
private val VARROCK_SOUTH_MINE = Tile(3179, 3363, 0)
private val BLUE_MOON_INN = Tile(3229, 3399, 0)
private val SOUTH_WEST_GE = Tile(3134, 3461, 0)
private val EDGEVILLE_FAIRY_RING = Tile(3129, 3497, 0)
private val PORT_SARIM_TELE_SPOT = Tile(3043, 3270, 0)
private val WIZARDS_TOWER_FAIRY_RING = Tile(3107, 3149, 0)

private val LUMBRIDGE_GATE = GameObjects.at(LUMBRIDGE_GATE_NORTH).first()
private val GERTRUDE_DOOR = GameObjects.at(GERTRUDE_DOOR_OUTSIDE).first()

private val SELL_LOCATIONS = arrayOf(
    LUMBRIDGE,
    VARROCK,
    EDGEVILLE,
    FALADOR,
    PORT_SARIM,
    RIMMINGTON,
    DRAYNOR,
    milkSellerSpawnTile,
)

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
            milkSellerSpawnTile = this.tile
            this["full_pathfinding"] = true

            // NpcSchedules.registry.register(schedule)
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
}

private fun travelSomewhereNew(npc: NPC) {
    npc.say("Well Bessie, time we headed out.")
    when (val destination = nextSellingLocation()) {
        LUMBRIDGE -> travelToLumbridge(npc)
        VARROCK -> travelToVarrock(npc)
        EDGEVILLE -> varrockToEdgeville(npc)
        FALADOR -> edgevilleToFalador(npc)
        RIMMINGTON -> faladorToRimmington(npc)
        PORT_SARIM -> rimmingtonToPortSarim(npc)
        DRAYNOR -> portSarimToDraynor(npc)
        milkSellerSpawnTile -> draynorToSpawn(npc)
    }
}

private fun nextSellingLocation(): Tile {
    val destination = SELL_LOCATIONS[routeIndex]

    routeIndex = (routeIndex + 1) % SELL_LOCATIONS.size

    return destination!!
}

private fun draynorToSpawn(npc: NPC) {
    npc.enqueue("milk_seller_to_spawn") {
        patrolDelay("wizard_tower_to_draynor")
        say("Home sweet home.")
        setSpawnAndWander(npc, milkSellerSpawnTile!!)
    }
}

private fun portSarimToDraynor(npc: NPC) {
    npc.travelTo(PORT_SARIM_TELE_SPOT, "milk_seller_to_tele") {
        enqueue("milk_seller_to_draynor") {
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
    }
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
    npc.travelTo(LUMBRIDGE_MARKET, "milk_seller_spawn_to_lumbridge_market") {
        npc.travelTo(LUMBRIDGE, "milk_seller_market_to_lumbridge") {
            say("Careful about Bob, Bessie. I think he was looking at you funny.")
            // Moo
            setSpawnAndWander(npc, LUMBRIDGE)
        }
    }
}

private fun travelToVarrock(npc: NPC) {
    npc.travelTo(LUMBRIDGE_MARKET, "milk_seller_lumbridge_to_market") {
        travelTo(milkSellerSpawnTile!!, "milk_seller_lumbridge_to_spawn") {
            travelTo(LUMBRIDGE_GATE_SOUTH, "milk_seller_spawn_to_gate") {
                tele(LUMBRIDGE_GATE_NORTH)
                travelTo(VARROCK_SOUTH_MINE, "milk_seller_gate_to_mines") {
                    travelTo(BLUE_MOON_INN, "milk_seller_mines_to_inn") {
                        say("Hey, just dropping off the usual.")
                        travelTo(VARROCK, "milk_seller_inn_to_varrock") {
                            say("We should remember to stop by Gertrude's on the way over, Bessie.")
                            // Moo
                            setSpawnAndWander(npc, VARROCK)
                        }
                    }
                }
            }
        }
    }
}

private fun varrockToEdgeville(npc: NPC) {
    npc.travelTo(GERTRUDE_DOOR_OUTSIDE, "milk_seller_varrock_to_gertrude") {
        npcOpenDoor(GERTRUDE_DOOR, 69420)
        travelTo(GERTRUDE_DOOR_INSIDE, "milk_seller_walk_in_gertrudes_home") {
            enqueue("milk_seller_gertrude_talk") {
                milkSellerNpc!!.mode = PauseMode
                say("Hey Gertrude, came to see if you need a top-off.")
                pause(3)
                gertrudeNpc!!.say("Thank you dearie. Want a kitten?")
                pause(3)
                say("Oh no, thank you. Bessie is enough for me.")
                pause(3)
                // Moo
                pause(3)
                say("I better get back to it before she breaks the house down.")
                pause(3)
                gertrudeNpc!!.say("Haha, take care dearie.")
                npcOpenDoor(GERTRUDE_DOOR, 5)
                travelTo(SOUTH_WEST_GE, "milk_seller_gertrude_to_ge") {
                    enqueue("milk_seller_bessie_teleport") {
                        say("Ugh... Guards... Bessie, could you please?")
                        pause(3)
                        // Moo...
                        pause(3)
                        tele(EDGEVILLE_FAIRY_RING)
                        say("Thanks, Bessie.")
                        pause(3)
                        // Moo.
                        travelTo(EDGEVILLE, "milk_seller_ge_to_edgeville") {
                            say("I hope the programmer remembers to enter flavor text here...")
                            // Moo
                            setSpawnAndWander(npc, EDGEVILLE)
                        }
                    }
                }
            }
        }
    }
}
