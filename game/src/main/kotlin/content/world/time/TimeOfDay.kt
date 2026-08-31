package content.world.time

enum class TimeOfDay {
    DAWN,
    DAY,
    DUSK,
    NIGHT;

    fun next(): TimeOfDay =
        entries[(ordinal + 1) % entries.size]
}

object WorldTime {
    var hour: Int = 12
        private set

    fun advanceHour() {
        hour = (hour + 1) % 24
    }

    val timeOfDay: TimeOfDay
        get() = when (hour) {
            in 5..7 -> TimeOfDay.DAWN
            in 8..17 -> TimeOfDay.DAY
            in 18..19 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }
}

