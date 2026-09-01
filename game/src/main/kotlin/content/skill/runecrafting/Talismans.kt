package content.skill.runecrafting

import content.entity.obj.ObjectTeleports
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.Direction

class Talismans(val teleports: ObjectTeleports) : Script {

    init {
        itemOption("Locate", "*_talisman") { (item) ->
            if (item.id == "cosmic_talisman"){
                if(tile in Areas["zanaris"]){
                    message("The talisman is pulling towards the ${direction.name.toKebabCase()}.")
                    return@itemOption
                }  else {
                    message("The talisman is having trouble pin-pointing the location.")
                    return@itemOption
                }
            }
            if (item.id == "elemental_talisman") {
                message("You cannot tell which direction the talisman is pulling...")
                return@itemOption
            }
            val id = item.id.replace("_talisman", "_altar_portal")
            val teleport = teleports.get(id, "Enter").first()
            if (tile.region == teleport.tile.region) {
                val type = item.id.removeSuffix("_talisman").toSentenceCase()
                message("You are standing in the $type temple.")
                return@itemOption
            }
            val direction = teleport.to.delta(tile).toDirection()
            if (tile in Areas["overworld"] || direction == Direction.NONE) {
                message("The talisman is pulling towards the ${direction.name.toKebabCase()}.")
            } else {
                message("The talisman is having trouble pin-pointing the location.")
            }
        }
    }
}
