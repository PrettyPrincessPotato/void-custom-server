package content.area.wilderness

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.player.bank.pin.openBank
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

private const val SIN_MISTER_STRING_ID = "sin_mister"

class SinMister(graph: NavigationGraph) : Script {
    val useBotNav = false

    private val routeExecutor: NpcRouteExecutor =
        if (useBotNav) {
            GraphNpcRouteExecutor(
                NpcNavMeshRouteFinder(graph),
            )
        } else {
            NativeNpcRouteExecutor()
        }

    private var sinMister: NPC? = null
    init {
        npcSpawn(SIN_MISTER_STRING_ID) {
            sinMister = this
            this["full_pathfinding"] = true

            // NpcSchedules.registry.register(schedule) TEMPORARILY DISABLED
        }
        npcOperate("Talk-to", SIN_MISTER_STRING_ID) {
            if (equipment.contains("ghostspeak_amulet")) {
                npc<Neutral>("Hello.")
                choice {
                    option<Confused>("What's a ghost doing out here?") {
                        npc<Neutral>("Why, I'm a banker of course!")
                        choice {
                            option<Confused>("What's a ghost banker doing out here?") {
                                npc<Shifty>("Let's just say I'm... Paying off a debt.")
                                choice {
                                    option<Happy>("Well that doesn't at all sound ominous. Can I see my bank please?") {
                                        openBank()
                                    }
                                }
                            }
                        }
                    }
                    option("I would like to open my bank account, please.") {
                        openBank()
                    }
                }
            } else if (inventory.contains("ghostspeak_amulet")) {
                npc<Idle>("Wooo wooo wooooo!")
                player<Idle>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
            } else {
                sinMister?.say("Woo.. Wooo!!")
                sinMister?.face(this)
                message("Sadly, you don't speak ghost.")
            }
        }
        npcOperate("Bank", SIN_MISTER_STRING_ID) {
            if (equipment.contains("ghostspeak_amulet")) {
                openBank()
            } else if (inventory.contains("ghostspeak_amulet")) {
                npc<Idle>("Wooo wooo wooooo!")
                player<Idle>("Why can't I understand you? Oh, yeah, it might help if I wear this amulet!")
            } else {
                sinMister?.say("Woo.. Wooo!!")
                sinMister?.face(this)
                message("Sadly, you don't speak ghost.")
            }
        }
    }
}
