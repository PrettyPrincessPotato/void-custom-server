package content.activity.city_raids

import world.gregs.voidps.engine.entity.character.npc.NPC

class RaidIdentityResolver(
    private val goblinIds: Set<String>
) {
    fun identityOf(npc: NPC): RaidIdentity? {
        when {
            isGoblin(npc) -> return RaidIdentity(
                race = RaidRace.GOBLIN,
                faction = RaidFaction.GOBLIN_TRIBE
            )

            isFaladorGuard(npc) -> return RaidIdentity(
                race = RaidRace.HUMAN,
                faction = RaidFaction.FALADOR
            )

            isVarrockGuard(npc) -> return RaidIdentity(
                race = RaidRace.HUMAN,
                faction = RaidFaction.VARROCK
            )
        }

        return null
    }

    fun isGoblin(npc: NPC): Boolean = npc.id in goblinIds // Currently this identifies all goblins. TODO: identify only goblins we spawn so we don't affect all goblins globally
    fun isFaladorGuard(npc: NPC): Boolean = npc.id.contains("guard_falador")
    fun isVarrockGuard(npc: NPC): Boolean = npc.id.contains("guard_varrock")
}