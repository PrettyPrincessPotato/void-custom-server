package content.world.time

enum class TimeOfDay {
    DAWN,
    DAY,
    DUSK,
    NIGHT;

    fun next(): TimeOfDay =
        entries[(ordinal + 1) % entries.size]
}

fun interface HourChangeListener {
    fun onHourChanged(previousHour: Int, currentHour: Int)
}

object WorldTime {
    private val hourChangeListeners = mutableSetOf<HourChangeListener>()

    var hour: Int = 12
        private set

    fun subscribeToHourChanges(listener: HourChangeListener) {
        hourChangeListeners += listener
    }

    fun unsubscribeFromHourChanges(listener: HourChangeListener) {
        hourChangeListeners -= listener
    }

    fun advanceHour() {
        val previousHour = hour
        hour = (hour + 1) % 24

        hourChangeListeners
            .toList()
            .forEach { it.onHourChanged(previousHour, hour) }

    }

    val timeOfDay: TimeOfDay
        get() = when (hour) {
            in 5..7 -> TimeOfDay.DAWN
            in 8..17 -> TimeOfDay.DAY
            in 18..19 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }
}

