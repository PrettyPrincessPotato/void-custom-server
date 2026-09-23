package content.area.misthalin.wizards_tower

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.queue.longQueue
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
        npcOperate("Talk-to", "wizard_yanille") {
            if(it.target.index != wizard?.index) {
                return@npcOperate
            }
            when(get("the_lost_talisman", "unstarted")) {
                "unstarted" -> questStart()
                "completed" -> postQuest()
                else -> howsTheSearch()
            }
        }
        entered("cosmic_altar_teleport") {
            if(get("the_lost_talisman", "unstarted") == "searching") {
                set("the_lost_talisman", "altar_found")
                message("Looks like you found the altar the wizard was looking for. You should return to tell him where it is.")
            }
        }
    }
}

private suspend fun Player.howsTheSearch() {
    npc<Quiz>("How goes the search?")
    if(get("the_lost_talisman", "unstarted") == "searching") {
        player<Sad>("I haven't found it yet.")
        npc<Neutral>("That's alright, I'm sure you'll find it eventually.")
        return
    }
    if(get("the_lost_talisman", "unstarted") == "altar_found") {
        player<Happy>("I've found it!")
        npc<Happy>("That's wonderful! Where is it, then?")
        player<Neutral>("It's in the lost city of Zanaris.")
        npc<Shock>("...")
        npc<Shock>("You... Found the lost city?")
        player<Happy>("Yup! Turns out it's just in the shed, you need a Dramen staff to get in.")
        npc<Confused>("You know, if you haven't actually found it, you could just say so...")
        player<Confused>("What? No, I'm saying it's actually there, go make a Dramen staff for yourself and see!")
        npc<Neutral>("... Right. I'll get right on that...")
        set("the_lost_talisman", "completed")
        exp(Skill.Runecrafting, 500.0)
        longQueue("quest_complete", 1) {
            questComplete(
                "The Lost Talisman",
                "A Cosmic Talisman",
                "500 Runecrafting XP",
                item = "cosmic_talisman",
            )
        }
        return
    }
}

private suspend fun Player.postQuest() {
    statement("The wizard doesn't seem interested in speaking.")
}

private suspend fun Player.questStart() {
    npc<Confused>("Hmm... I just can't...")
    npc<Neutral>("Oh, hello there.")
    player<Quiz>("Hello, what's that you're messing with?")
    if(!questCompleted("rune_mysteries") || !questCompleted("enter_the_abyss") || !questCompleted("lost_city")) {
        npc<Neutral>("Hm? Oh, sorry. I don't think you'd be of any use here.")
        statement("You must complete Rune Mysteries, Enter the Abyss, and Lost City to start The Lost Talisman")
        return
    }
    npc<Happy>("Ah, you! I've heard of you. I found this strange talisman.")
    npc<Confused>("It's just the oddest thing, though. It doesn't pull like the other talismans do.")
    player<Quiz>("Really? Why do you think that?")
    npc<Neutral>("I can't say anything for sure, it's almost like the location for this has been lost.")
    npc<Quiz>("Would you like to take it? I'm sure someone as well-travelled as you could find it.")
    questQuestions()
}

private suspend fun Player.questQuestions() {
    choice {
        option<Quiz>("What's the catch?") {
            npc<Neutral>("All I ask is that you tell me where you've found it.")
            npc<Quiz>("Is that something you're interested in?")
            questQuestions()
        }
        option("Yes please.") {
            acceptQuest()
        }
        option<Neutral>("No thank you. I don't feel like adventuring today.") {
            npc<Neutral>("Suit yourself. I'm here if you change your mind.")
        }
    }
}

private suspend fun Player.acceptQuest() {
    player<Happy>("Sure, finding the impossible has been something I managed to pull off before.")
    inventory.transaction {
        inventory.add("cosmic_talisman")
    }
    when (inventory.transaction.error) {
        is TransactionError.Full -> npc<Neutral>("Oh dear, it appears your inventory is full.")
        else -> questAccepted()
    }
}

private suspend fun Player.questAccepted() {
    npc<Happy>("There you go! Don't forget to tell me when you find it.")
    set("the_lost_talisman", "searching")
}