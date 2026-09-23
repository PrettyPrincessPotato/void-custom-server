package content.area.misthalin.wizards_tower

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class ConfusedWizard : Script {
    private val spawnTile = Tile(3120, 3209, 0)
    private var wizard: NPC? = null

    private fun spawnConfusedWizard() {
        wizard = NPCs.add("wizard_yanille", spawnTile)
        wizard!!["full_pathfinding"] = false
    }

    init {
        worldSpawn {
            spawnConfusedWizard()
        }
    }
}