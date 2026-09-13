package content.area.wilderness

import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.player.bank.bank
import content.entity.player.bank.pin.openBank
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.member.ghosts_ahoy.checkGhostspeak
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

private const val SIN_MISTER_STRING_ID = "sin_mister"
private const val GHOSTSPEAK_AMULET_STRING_ID = "ghostspeak_amulet"
private const val ENCH_GHOSTSPEAK_AMULET_STRING_ID = "ghostspeak_amulet_enchanted"

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
            if(!sinMisterCheckGhostspeak()){
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
            if(!sinMisterCheckGhostspeak()){
                return@npcOperate
            }
            openBank()
        }
    }
}

private suspend fun Player.sinMisterCheckGhostspeak(): Boolean {
    if(bank.contains(GHOSTSPEAK_AMULET_STRING_ID)) {
        bank.remove(GHOSTSPEAK_AMULET_STRING_ID)
        inventory.add(GHOSTSPEAK_AMULET_STRING_ID)
        statement("Sin Mister hands you your Amulet of Ghostspeak from your bank.")
        return false
    }
    if(bank.contains(ENCH_GHOSTSPEAK_AMULET_STRING_ID)) {
        bank.remove(ENCH_GHOSTSPEAK_AMULET_STRING_ID)
        inventory.add(ENCH_GHOSTSPEAK_AMULET_STRING_ID)
        statement("Sin Mister hands you your Enchanted Amulet of Ghostspeak from your bank.")
        return false
    }
    if (!checkGhostspeak()) {
        return false
    }
    return true
}