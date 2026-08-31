package content.activity.city_raids

import content.activity.city_raids.routes_and_pois.IceMountainArea
import content.entity.combat.dead
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class RaidController(private val raidManager: RaidManager) {
    // Essentially goblin brains. Go big war god!
    fun transition(
        gobbo: RaidMember,
        state: RaidState
    ) {
        gobbo.state = state

        when (state) {
            RaidState.TRAVELLING_TO_CAMP -> {
                val waypoints: List<Pair<Tile, Int>> = IceMountainArea.GOBLIN_VILLAGE_TO_FALADOR_CAMP.map { tile -> //TODO: Make this a variable somehow to expand different camps
                    tile to 0 // or some delay ticks between tiles
                }
                // Walk over to encampment
                gobbo.npc.mode = Patrol(
                    character = gobbo.npc,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = false,
                    onComplete = {
                        if (!gobbo.npc.dead) {
                            respawnGoblin(gobbo, IceMountainArea.FALADOR_CAMP)
                            transition( gobbo, RaidState.MUSTERING )
                        }
                    }
                )
            }

            RaidState.MUSTERING -> {
                //Find the tree object nearby somehow
                //can do gobboNew.interactObject(TreeObject) once we find nearby trees to have the goblins chop one down nearby.
            }

            RaidState.TRAVELLING_TO_TOWN -> {
                val waypoints: List<Pair<Tile, Int>> = IceMountainArea.FALADOR_CAMP_TO_GATE.map { tile ->
                    tile to 0 // or some delay ticks between tiles
                }
                gobbo.npc.mode = Patrol(
                    character = gobbo.npc,
                    waypoints = waypoints,
                    loop = false,
                    noCollision = true, // Enabled here because they get caught and clump easily. This is more like a goblin rush.
                    onComplete = {
                        if (!gobbo.npc.dead) {
                            respawnGoblin( gobbo, IceMountainArea.FALADOR_GATE )
                            transition( gobbo, RaidState.SIEGING_TOWN )
                        }
                    }
                )
            }

            RaidState.SIEGING_TOWN -> {
                /**
                 * Nothing really special needed here, they're in town. Maybe add something fancy to do here. It'd be *really* cool
                 * if the various mobs can take an entire town through some minigame, king of the hill or something, but I can see
                 * some issues with that from a gameplay stance.
                 * That also may be difficult with this engine.
                 */
            }
        }
    }
    private fun respawnGoblin(member: RaidMember, tile: Tile): NPC {
        member.npc.despawn(0)

        val replacement = NPCs.add(goblinIds.random(), tile)
        raidManager.replaceMemberNpc(member, replacement)

        return replacement
    }
}