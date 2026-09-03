package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.cache.definition.encoder.NPCEncoder

/**
 * WARNING: this script is still in its infancy and needs more testing/refining.
 *
 * Adds a new custom NPC to the cache without overwriting any existing NPCs.
 *
 * The NPC's appearance (model IDs) is copied from an existing NPC, but the name
 * and right-click options are fully custom.
 *
 * After running this script, define the NPC in a .npcs.toml file and add a spawn
 * in a .npc-spawns.toml file. See the printed instructions after running.
 *
 * Run from project root: gradle tools:addCustomNPC
 * Or run directly via IDE main function.
 */
object AddCustomNPC {

    // ---- Configuration --------------------------------------------------
    // Change these values to customize your new NPC.

    /** A cache ID that doesn't collide with existing NPCs. Higher is safer.
     * WARNING: upon using 69420 the "hanging suspect" npc was placed instead from the
     * evil twin random event.
     * This implies the existence of a buffer overflow somewhere.
     * */
    private const val NEW_NPC_ID = 16128 // First slot that works for some reason. Slots after may or may not work.

    /** NPC ID to copy appearance from. 3103 = rewards_guardian (MTA area). */
    private const val COPY_FROM_ID = 3103

    /** The name shown when hovering or examining the NPC. */
    private const val NEW_NPC_NAME = "Aaroc"

    /**
     * Right-click options (slots 0-4). Slot 5 is always "Examine" by default.
     * Set to null for empty slots.
     */
    private val NEW_NPC_OPTIONS = arrayOf("Talk-to", "Trade", null, null, null)

    /** Combat level shown to players. Set to 0 for no level. */
    private const val NEW_NPC_COMBAT = 0

    // ---- End configuration ----------------------------------------------

    private val decoder = NPCDecoderFull(members = true)
    private val encoder = NPCEncoder()

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val library = CacheLibrary(path)

        // 0. Safety check: verify the target ID is unused to avoid overwriting
        val actualId = checkIdIsSafe(library, NEW_NPC_ID)

        // 1. Read the source NPC to copy its appearance
        println("Reading source NPC $COPY_FROM_ID...")
        val source = readNpc(library, COPY_FROM_ID)
        println("  Source name: ${source.name}")
        println("  Source models: ${source.modelIds?.toList()}")

        // 2. Build the new NPC definition
        val newNpc = NPCDefinitionFull(
            id = actualId,
            name = NEW_NPC_NAME,
            modelIds = source.modelIds,
            dialogueModels = source.dialogueModels,
            size = source.size,
            options = NEW_NPC_OPTIONS + "Examine",
            combat = NEW_NPC_COMBAT,
            drawMinimapDot = true,
            clickable = true,
            animateIdle = true,
            slowWalk = true,
            rotation = source.rotation,
            scaleXY = source.scaleXY,
            scaleZ = source.scaleZ,
            renderEmote = source.renderEmote,
            mainOptionIndex = 0.toByte(),             // "Trade" as left-click option
            primaryShadowColour = source.primaryShadowColour,
            secondaryShadowColour = source.secondaryShadowColour,
            primaryShadowModifier = source.primaryShadowModifier,
            secondaryShadowModifier = source.secondaryShadowModifier,
            lightModifier = source.lightModifier,
            shadowModifier = source.shadowModifier,
        )

        // 3. Encode the definition to bytes
        val writer = ArrayWriter(500)
        with(encoder) {
            writer.encode(newNpc)
        }
        val data = writer.toArray()

        // 4. Verify the encoded data round-trips correctly
        val verify = NPCDefinitionFull(actualId)
        decoder.readLoop(verify, ArrayReader(data))
        require(verify.name == NEW_NPC_NAME) {
            "Name mismatch after encode/decode: '${verify.name}' != '$NEW_NPC_NAME'"
        }
        println("Verified: name='${verify.name}', models=${verify.modelIds?.toList()}")

        // 5. Write to cache at the target ID's archive/file location
        val archive = decoder.getArchive(actualId)
        val file = decoder.getFile(actualId)
        println("Writing NPC $actualId to archive $archive, file $file...")
        library.put(Index.NPCS, archive, file, data)
        library.index(Index.NPCS).flag()

        // 6. Save changes to disk
        val cache = CacheDelegate(library)
        cache.update()

        println()
        println("Done! NPC $actualId '$NEW_NPC_NAME' added to cache.")
        println()
        println("---- Next steps ----")
        println()
        println("1. Add to data/area/misthalin/zanaris/zanaris.npcs.toml:")
        println()
        println("   [oric]")
        println("   id = $actualId")
        println("   shop = \"mage_training_arena\"")
        println("   examine = \"A magical golem, imbued with arcane knowledge.\"")
        println()
        println("2. Add a spawn in zanaris.npc-spawns.toml:")
        println()
        println("   { id = \"oric\", x = 2417, y = 4471, members = true }")
        println()
        if (actualId != NEW_NPC_ID) {
            println("NOTE: Requested ID $NEW_NPC_ID was in use; used $actualId instead.")
            println("      Make sure the TOML id above matches!")
        }
    }

    /**
     * Checks whether [id] already has cache data. If it does, reports the
     * existing NPC name and finds a safe unused ID beyond the cache's last
     * archive. Returns the ID to actually use (either [id] if safe, or a
     * computed safe alternative).
     */
    private fun checkIdIsSafe(library: CacheLibrary, id: Int): Int {
        val archive = decoder.getArchive(id)
        val file = decoder.getFile(id)
        val existing = library.data(Index.NPCS, archive, file)
        if (existing == null) {
            println("ID $id is unused — proceeding.")
            return id
        }

        // Read the existing NPC to report what would be overwritten
        val existingNpc = NPCDefinitionFull(id)
        decoder.readLoop(existingNpc, ArrayReader(existing))
        println("WARNING: ID $id is already in use by NPC '${existingNpc.name}'!")
        println("  Aborting write to $id to prevent overwriting.")

        // Find a safe ID in a brand-new archive beyond the cache's current range
        val lastArchive = library.index(Index.NPCS).last()?.id ?: -1
        val safeArchive = lastArchive + 1
        val safeId = safeArchive * 128  // first ID in the new archive (file 0)
        println("  Found safe ID: $safeId (archive $safeArchive, file 0)")
        println("  This is beyond the cache's last NPC archive ($lastArchive).")
        println("  Using $safeId instead.")
        return safeId
    }

    /**
     * Reads a single NPC definition from the cache by ID.
     */
    private fun readNpc(library: CacheLibrary, id: Int): NPCDefinitionFull {
        val definition = NPCDefinitionFull(id)
        val archive = decoder.getArchive(id)
        val file = decoder.getFile(id)
        val data = library.data(Index.NPCS, archive, file)
            ?: throw IllegalStateException(
                "NPC $id not found in cache at archive $archive, file $file"
            )
        decoder.readLoop(definition, ArrayReader(data))
        return definition
    }
}
