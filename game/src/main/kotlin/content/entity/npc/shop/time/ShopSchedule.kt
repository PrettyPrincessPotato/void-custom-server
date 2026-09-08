package content.entity.npc.shop.time

data class ShopSchedule(
    val openingHour: Int,
    val closingHour: Int,
) {
    fun isOpen(hour: Int): Boolean = if (openingHour < closingHour) {
        hour in openingHour until closingHour
    } else {
        hour !in closingHour..<openingHour
    }
}
