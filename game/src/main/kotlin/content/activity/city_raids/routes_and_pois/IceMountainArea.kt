package content.activity.city_raids.routes_and_pois

import world.gregs.voidps.type.Tile

object IceMountainArea {
    val FALADOR_CAMP = Tile(2953, 3407)
    val GOBLIN_VILLAGE = Tile(2956, 3503)
    val FALADOR_GATE = Tile(2966, 3394)

    val GOBLIN_VILLAGE_TO_FALADOR_CAMP = listOf(
        Tile(2956, 3499),
        Tile(2956, 3491),
        Tile(2955, 3489),
        Tile(2955, 3484),
        Tile(2955, 3480),
        Tile(2954, 3474),
        Tile(2952, 3468),
        Tile(2948, 3452),
        Tile(2949, 3424),
        Tile(2953, 3407)
    )

    val FALADOR_CAMP_TO_GATE = listOf(
        Tile(2955, 3405),
        Tile(2956, 3398),
        Tile(2966, 3398),
        Tile(2966, 3394)
    )
}