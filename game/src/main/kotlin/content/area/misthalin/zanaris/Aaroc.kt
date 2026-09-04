package content.area.misthalin.zanaris


import content.bot.behaviour.navigation.NavigationGraph
import content.entity.npc.movement.GraphNpcRouteExecutor
import content.entity.npc.movement.NativeNpcRouteExecutor
import content.entity.npc.movement.NpcNavMeshRouteFinder
import content.entity.npc.movement.NpcRouteExecutor
import content.entity.npc.schedule.NpcScheduleController
import content.entity.npc.schedule.NpcSchedules
import content.entity.npc.schedule.ScheduleAction
import content.entity.npc.schedule.ScheduleTransition
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.queue.queue as enqueue

private const val AAROC_STRING_ID = "aaroc"
private const val AAROC_TO_ZANARIS_HOUR = 20
private const val AAROC_LEAVE_ZANARIS_HOUR = 5
private val AAROC_HIDING_SPOT = Tile(2417, 4471)
private val AAROC_ROAMING_SPOTS = arrayOf(Areas["al_kharid_mine"],Areas["sophanem"],Areas["desert_bandit_camp_multi_area"],Areas["mudskipper_point"],Areas["lassar_teleport"],Areas["trollheim_teleport"],Areas["ice_plateau_teleport"],Areas["south_feldip_hills"])

// TODO: Make him a wandering trader when the schedule slice is finished.
// Is this NPC completely incapable of speech? Or Maybe we can make it speak some made-up language?
// What if it's incapable of talking in Zanaris for some reason?
class Aaroc(graph: NavigationGraph) : Script {
    val useBotNav = false

    private val routeExecutor: NpcRouteExecutor =
        if (useBotNav) {
            GraphNpcRouteExecutor(
                NpcNavMeshRouteFinder(graph)
            )
        } else {
            NativeNpcRouteExecutor()
        }
    private var aaroc: NPC? = null

    init {
        fun aarocChant(){
            aaroc?.mode = EmptyMode
            if(aaroc?.tile in Areas["zanaris"]){
                aaroc?.say("...")
            } else {
                aaroc?.say("Zarith... Shenoth... Tarin...")
            }
        }
        fun aarocTeleport(teleport: NPC.() -> Unit) {
            val npc = aaroc ?: return

            npc.teleport()

            val currentTile = npc.tile
            npc["spawn_tile"] = currentTile
            npc.mode = Wander(npc, currentTile)
        }

        fun aarocTeleportArea(teleTo: Area) {
            aarocTeleport {
                tele(teleTo)
            }
        }

        fun aarocTeleportTile(teleTo: Tile) {
            aarocTeleport {
                tele(teleTo)
            }
        }

        /**
         * This function will be responsible for Aaroc picking a location and teleporting there from Zanaris, choosing that spot to wander around until nightfall.
         */
        fun moveAarocFromZanaris(){
            aaroc?.enqueue("aaroc_tele"){
                val teleportLoc = AAROC_ROAMING_SPOTS.random()
                aarocChant()
                aaroc?.delay(3)
                aarocTeleportArea(teleportLoc)
            }
        }
        fun moveAarocToZanaris(){
            aaroc?.enqueue("aaroc_tele"){
                aarocChant()
                aaroc?.delay(3)
                aarocTeleportTile(AAROC_HIDING_SPOT)
            }
        }

        val schedule = NpcScheduleController(
            npcProvider = { aaroc },
            routeExecutor = routeExecutor,
            scheduleTransitions = listOf(
                ScheduleTransition(
                    AAROC_LEAVE_ZANARIS_HOUR,
                    ScheduleAction.Custom{
                        moveAarocFromZanaris()
                    }
                ),
                ScheduleTransition(
                    AAROC_TO_ZANARIS_HOUR,
                    ScheduleAction.Custom{
                        moveAarocToZanaris()
                    }
                )
            )
        )

        npcSpawn(AAROC_STRING_ID) {
            aaroc = this
            this["full_pathfinding"] = true
            this.mode = Wander(this)

            NpcSchedules.registry.register(schedule)
        }

        npcDespawn(AAROC_STRING_ID) {
            NpcSchedules.registry.unregister(schedule)

            if (aaroc === this) {
                aaroc = null
            }
        }
    }
    init{
        npcOperate("Talk-to", "aaroc"){
            if(tile in Areas["zanaris"]){
                npc<Neutral>("...")
                choice {
                    option<Confused>("Err... Hello?"){
                        npc<Sad>("...")
                        openShop("mage_training_arena")
                    }
                }
            } else {
                npc<Neutral>("Zoltek...? Marthel...?")
                choice {
                    option<Confused>("Uh.. I'm sorry what?"){
                        npc<Sad>("Malek... Tomek...")
                        openShop("mage_training_arena")
                    }
                }
            }
        }
    }
}