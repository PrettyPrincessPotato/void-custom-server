package content.area.misthalin.zanaris

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas

// TODO: Make him a wandering trader when the schedule slice is finished.
// Is this NPC completely incapable of speech? Or Maybe we can make it speak some made-up language?
// What if it's incapable of talking in Zanaris for some reason?
class Aaroc : Script {
    init{
        npcOperate("Talk-to", "aaroc"){
            if(tile in Areas["zanaris"]){
                npc<Neutral>("...")
                choice {
                    option<Confused>("Err... Hello?"){
                        npc<Sad>("...")
                    }
                }
            } else {
                npc<Neutral>("Sorry, the coder hasn't made this part yet. Buy my shit.")
            }
        }
    }
}