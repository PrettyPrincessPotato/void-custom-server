package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.encoder.ItemEncoder
import world.gregs.voidps.tools.cache.FixItems.fix

//private const val BATTLESTAFF_ID = 1391 // Normal Battlestaff
private const val BATTLESTAFF_ID = 1393 // Fire Battlestaff

//private val IDS_TO_CHANGE = intArrayOf(BATTLESTAFF_ID, FIRE_BATTLESTAFF_ID)

private const val EQUIP_LEVEL_VALUE = 0 // Not always equip_level_2, keep in mind.

object MagicGearTweaks {

    fun tweak(library: CacheLibrary){
        println("Applying Magic Gear Tweaks...")
        val indexId = Index.ITEMS
        val index = library.index(indexId)

        val cache = CacheDelegate(library)
        val decoder = ItemDecoderFull()
        val encoder = ItemEncoder()

        // Read current cached item definition
        println("Applying Battlestaff Tweak...")
        val definition = ItemDefinitionFull(BATTLESTAFF_ID)
        val data = library.data(indexId, decoder.getArchive(BATTLESTAFF_ID), decoder.getFile(BATTLESTAFF_ID)) ?: run {
            error("Cache entry not found for item id=$BATTLESTAFF_ID in $indexId")
        }

        val buffer = ArrayReader(data)
        decoder.readLoop(definition, buffer)

        @Suppress("UNCHECKED_CAST")
        val params = definition.params as? MutableMap<Int, Any?>
            ?: error("Expected Int keyed params map")


        // Debug dumping definitions.
        params.forEach { (k, v) ->
            if (v is String) {
                println("  string param[$k] = '$v'")
            }
        }


        // IMPORTANT: we need the exact param key name used by the cache for equip_level_2.
        params[752] = EQUIP_LEVEL_VALUE
        println("params type: ${definition.params?.javaClass}")
        // Re-encode and write back into cache
        val writer = ArrayWriter(500)
        with(encoder) {
            writer.encode(definition)
        }
        val out = writer.toArray()

        // Write into the same archive/file
        library.put(
            indexId,
            decoder.getArchive(definition.id),
            decoder.getFile(definition.id),
            out
        )

        index.flag()
        cache.update()
        println("Updated item id=$BATTLESTAFF_ID equip_level_2 -> $EQUIP_LEVEL_VALUE")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "data/cache/"
        val lib = CacheLibrary(path)
        tweak(lib)
    }

}