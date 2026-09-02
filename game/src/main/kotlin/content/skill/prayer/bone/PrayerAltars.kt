package content.skill.prayer.bone

import content.entity.player.command.PlayerCommands
import content.skill.prayer.Prayer
import content.skill.prayer.PrayerApi
import content.skill.prayer.PrayerConfigs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.prayer.isCurses

class PrayerAltars : Script {

    init {
        objectOperate("Pray", "prayer_altar_*") {
            pray()
        }

        objectOperate("Pray-at", "prayer_altar_*") {
            pray()
        }

        objectOperate("Check", "prayer_altar_chaos_varrock") {
            message("An altar to the evil god Zamorak.")
        }

        objectOperate("Convert", "prayer_altar_zaros") {
            message("Swapping prayer books...")
            if(!isCurses()){
                this[PrayerConfigs.PRAYERS] = "curses"
            }
            else{
                this[PrayerConfigs.PRAYERS] = "normal"
            }
        }
    }

    fun Player.pray() {
        if (levels.getOffset(Skill.Prayer) >= 0) {
            message("You already have full Prayer points.")
        } else {
            levels.set(Skill.Prayer, levels.getMax(Skill.Prayer))
            anim("altar_pray")
            message("You recharge your Prayer points.")
            set("prayer_point_power_task", true)
        }
    }
}
