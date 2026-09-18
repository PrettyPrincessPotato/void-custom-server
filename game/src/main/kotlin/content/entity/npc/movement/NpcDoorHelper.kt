package content.entity.npc.movement

import content.entity.obj.door.Door.replace
import content.entity.obj.door.Door.revert
import world.gregs.voidps.engine.entity.obj.GameObject

class NpcDoorHelper
fun npcOpenDoor(
    door: GameObject,
    duration: Int,
    tileRotation: Int = 1,
    objRotation: Int = 1,
    collision: Boolean = true,
): Boolean {
    if (door.id.endsWith("_closed")) {
        replace(
            door, door.def, "_closed", "_opened", tileRotation, objRotation, duration, collision,
            revert(door.def, door, "close"),
        )
        return true
    }
    return false
}

fun npcCloseDoor(
    door: GameObject,
    duration: Int,
    tileRotation: Int = 1,
    objRotation: Int = 1,
    collision: Boolean = true,
): Boolean {
    if (door.id.endsWith("_opened")) {
        replace(
            door, door.def, "_opened", "_closed", tileRotation, objRotation, duration, collision,
            revert(door.def, door, "open"),
        )
        return true
    }
    return false
}
