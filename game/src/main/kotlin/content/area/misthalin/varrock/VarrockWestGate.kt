package content.area.misthalin.varrock

import content.entity.obj.door.enterDoor
import content.entity.obj.door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class VarrockWestGate : Script {
    init {
        objectOperate("Open", "gate_west_varrock_closed,gate_west_varrock_2_closed") { (target) ->
            enterDoor(target)
        }
    }
}
