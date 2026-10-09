package content.entity.npc.movement

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotInteractObject
import content.bot.behaviour.action.BotWalkTo
import content.entity.obj.Replace
import content.entity.obj.door.Door.closed
import content.entity.obj.door.Door.isDiagonal
import content.entity.obj.door.Door.isDoor
import content.entity.obj.door.Door.opened
import content.entity.obj.door.Door.replace
import content.entity.obj.door.Door.revert
import content.entity.obj.door.Door.rotation
import content.entity.obj.door.Door.tile
import content.entity.obj.door.Gate.isGate
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

fun npcOpenDoor(
    door: GameObject,
    duration: Int,
    tileRotation: Int = 1,
    objRotation: Int = 1,
    collision: Boolean = true,
): Boolean {
    if (door.id.endsWith("_closed")) {
        replace(
            door,
            door.def,
            door.def.opened,
            tileRotation,
            objRotation,
            duration,
            collision,
            revert(door.def, door, "close"),
        )
        return true
    }
    return false
}

fun npcCloseDoor(
    door: GameObject,
    duration: Int,
    tileRotation: Int = 3,
    objRotation: Int = 3,
    collision: Boolean = true,
): Boolean {
    if (door.id.endsWith("_opened")) {
        replace(
            door,
            door.def,
            door.def.closed,
            tileRotation,
            objRotation,
            duration,
            collision,
            revert(door.def, door, "open"),
        )
        return true
    }
    return false
}

/**
 * How long a door or gate opened by a nav edge action stays open: long
 * enough for the NPC to cross the edge, short enough to swing shut behind
 * it.
 */
const val NPC_DOOR_OPEN_DURATION = 50

/**
 * Open [door] the way the server opens it for a player using "Open": a
 * single leaf swaps in place, and a double door or two-leaf gate swings
 * both leaves. The nav tomls name one leaf of a pair, so the partner is
 * found by tile — mirroring the DoubleDoor/Gate handling of Door.openDoor
 * with plain object definitions, since NPCs have no per-player definition
 * resolution.
 */
fun npcOpenDoor(
    npc: NPC,
    door: GameObject,
    duration: Int,
): Boolean {
    val def = door.def
    if (!def.stringId.endsWith("_closed")) {
        return false
    }

    val double = findDoubleLeaf(door, def)
    if (double == null || !double.def.stringId.endsWith("_closed")) {
        replace(
            door,
            def,
            def.opened,
            if (door.isDiagonal()) 0 else 1,
            1,
            duration,
            true,
            revert(def, door, "close"),
        )
        return true
    }

    // Both leaves swing open, mirroring DoubleDoor.open without the Player.
    val delta = door.tile.delta(double.tile)
    val flip = Direction.cardinal[door.rotation].delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
    if (def.isGate()) {
        val first = if (flip) double else door
        val second = if (flip) door else double
        val hingeTile = tile(first, 1)
        Replace.objects(
            first,
            first.def.opened,
            hingeTile,
            first.rotation(3),
            second,
            second.def.opened,
            tile(hingeTile, second.rotation, 1),
            second.rotation(3),
            duration,
            collision = true,
            onRevert = revert(first.def, first, "close"),
        )
    } else {
        Replace.objects(
            door,
            def.opened,
            tile(door, 1),
            door.rotation(if (flip) 1 else 3),
            double,
            double.def.opened,
            tile(double, 1),
            double.rotation(if (flip) 3 else 1),
            duration,
            collision = true,
            onRevert = revert(def, door, "close"),
        )
    }
    return true
}

/**
 * NPC-side twin of DoubleDoor.get with clockwise = 0: given one leaf, find
 * the partner on an adjacent tile — along the door axis first, then on the
 * perpendicular axis, because gate leaves are not always aligned with the
 * door axis.
 */
private fun findDoubleLeaf(door: GameObject, def: ObjectDefinition): GameObject? {
    var orientation = Direction.cardinal[door.rotation]
    var other = GameObjects.getShape(door.tile.add(orientation.delta), door.shape)
    if (other != null && other.def.isDoor()) {
        return other
    }
    orientation = orientation.inverse()
    other = GameObjects.getShape(door.tile.add(orientation.delta), door.shape)
    if (other != null && other.def.isDoor()) {
        return other
    }
    if (def.isGate()) {
        orientation = orientation.rotate(2)
        other = GameObjects.getShape(door.tile.add(orientation.delta), door.shape)
        if (other != null && other.def.isGate()) {
            return other
        }
        orientation = orientation.inverse()
        other = GameObjects.getShape(door.tile.add(orientation.delta), door.shape)
        if (other != null && other.def.isGate()) {
            return other
        }
    }
    return null
}

/**
 * Run a nav edge's action list for an NPC. BotInteractObject is a
 * player-side concept — it issues InteractObject instructions to a player
 * client — so object actions are interpreted here as server-side object
 * replacements. BotWalkTo actions are already folded into NpcRouteSegment.tiles
 * by the route finder.
 */
fun NPC.executeEdgeActions(actions: List<BotAction>) {
    for (action in actions) {
        when (action) {
            is BotInteractObject -> when (action.option) {
                "Open" -> openEdgeObject(action)
                "Close" -> closeEdgeObject(action)
                else -> println("NPC $id: unsupported edge action ${action.option} ${action.id} at $tile")
            }
            is BotWalkTo -> Unit
            else -> println("NPC $id: unsupported edge action $action at $tile")
        }
    }
}

private fun NPC.openEdgeObject(action: BotInteractObject) {
    val door = action.findObject() ?: run {
        println("NPC $id: edge action Open ${action.id} not found near ${action.x},${action.y}")
        return
    }
    if (!npcOpenDoor(this, door, NPC_DOOR_OPEN_DURATION)) {
        println("NPC $id: failed to open ${door.id}")
    }
}

private fun NPC.closeEdgeObject(action: BotInteractObject) {
    val door = action.findObject() ?: run {
        println("NPC $id: edge action Close ${action.id} not found near ${action.x},${action.y}")
        return
    }
    if (!npcCloseDoor(door, NPC_DOOR_OPEN_DURATION)) {
        println("NPC $id: failed to close ${door.id}")
    }
}

/**
 * Same target search as BotInteractObject's: a spiral from the action's
 * tile, matching the id with wildcards.
 */
private fun BotInteractObject.findObject(): GameObject? {
    if (x == null || y == null) {
        return null
    }
    for (tile in Spiral.spiral(Tile(x, y), radius)) {
        for (obj in GameObjects.at(tile)) {
            if (wildcardEquals(id, obj.id)) {
                return obj
            }
        }
    }
    return null
}
