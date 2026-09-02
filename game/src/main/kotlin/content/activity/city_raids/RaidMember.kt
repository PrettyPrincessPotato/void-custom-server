package content.activity.city_raids

import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidMember(
    var npc: NPC,
    val raid: Raid,
    var state: RaidState = RaidState.TRAVELLING_TO_CAMP
)