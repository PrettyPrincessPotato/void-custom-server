package content.world.time

enum class TimeOfDay {
    DAWN,
    DAY,
    DUSK,
    NIGHT;

    fun next(): TimeOfDay =
        entries[(ordinal + 1) % entries.size]
}
