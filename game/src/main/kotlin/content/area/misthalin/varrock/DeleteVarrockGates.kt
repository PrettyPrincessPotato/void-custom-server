package content.area.misthalin.varrock

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class DeleteVarrockGates : Script {
    init {
        worldSpawn {
            // West gate
            val tile1 = Tile(3273, 3429)
            val tile2 = Tile(3273, 3428)
            val door1GameObject = GameObjects.find(tile1, "gate_west_varrock_closed")
            val door2GameObject = GameObjects.find(tile2, "gate_west_varrock_2_closed")

            // North Gate
            val tile3 = Tile(3246, 3501, 0)
            val tile4 = Tile(3245, 3501, 0)
            val door3GameObject = GameObjects.at(tile3).first()
            val door4GameObject = GameObjects.at(tile4).first()
            // println("Dooorr $door3GameObject $door4GameObject")
            // Dooorr GameObject(id=, intId=46258, tile=Tile(3246, 3501, 0), shape=0, rotation=1)
            // GameObject(id=gate_west_varrock_closed, intId=45849, tile=Tile(3245, 3501, 0), shape=0, rotation=1)

            GameObjects.remove(door1GameObject)
            GameObjects.remove(door2GameObject)
            GameObjects.remove(door3GameObject)
            GameObjects.remove(door4GameObject)
        }
    }
}
