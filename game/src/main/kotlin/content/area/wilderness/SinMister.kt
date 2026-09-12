package content.area.wilderness

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.player.bank.pin.openBank
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.member.ghosts_ahoy.checkGhostspeak
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC

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
            if (!checkGhostspeak()) {
                return@npcOperate
            }
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
        }
        npcOperate("Bank", SIN_MISTER_STRING_ID) {
            if (!checkGhostspeak()) {
                return@npcOperate
            }
            openBank()
        }
    }
}
