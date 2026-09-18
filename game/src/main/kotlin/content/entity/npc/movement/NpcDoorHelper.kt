package content.entity.npc.movement

import content.entity.obj.door.Door
import content.entity.obj.door.Door.replace
import world.gregs.voidps.engine.entity.obj.GameObject

class NpcDoorHelper {
}
fun npcOpenDoor(door: GameObject, duration: Int) {
    replace(door, door.def, "_opened", "_closed", 1, 1, duration, true,
        Door.revert(door.def, door, "open"))
}