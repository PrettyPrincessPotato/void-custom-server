# NPC Schedule and Routing

## Status

Active

## Goal

Build a reusable day/night schedule and routing foundation that can support
more NPCs, goblin raids, and other movement-driven systems without coupling
route planning to NPC schedules. The routing work will focus on adapting the
existing map/navmesh.toml network used by bots so it can eventually serve NPC
movement as well.

## Decisions

- Schedules are data-driven: they describe when an action occurs, while the
  action implementation remains extensible.
- `ScheduleAction` uses typed action variants, such as travel and custom
  behavior, rather than making every transition an opaque lambda.
- `NpcLocation` contains reusable destination metadata: an identifier, tile,
  area, and optional navigation tag.
- `NpcRouteTarget` contains route-specific data such as queue name, dialogue,
  and arrival behavior.
- `NpcRouteExecutor` is the movement boundary. The schedule requests movement;
  the executor determines how movement is performed.
- Native NPC movement remains the current default through
  `NativeNpcRouteExecutor`.
- Existing bot navigation is based on `BotWorld` and `NavigationGraph`, which
  currently use `Player`/bot semantics and cannot be called directly for NPCs.
  The existing `map/navmesh.toml` network is the routing data source being
  expanded; player-bot behavior itself is outside this plan.
- Bot-navmesh support should be introduced behind a generic route-finder
  abstraction and a separate executor, rather than coupling schedules or NPC
  movement directly to bot-only APIs.
- Route selection should remain reusable outside schedules, including for
  goblin raids and future encounter systems.
- Manual executor selection is preferred initially. Automatic selection can be
  added later when stable criteria—such as tagged destinations or native path
  failure—are established.

## Progress

### Complete

- Defined the reusable schedule/action direction.
- Decoupled the schedule controller from a specific NPC instance by resolving
  the NPC through a provider.
- Added the `NpcLocation` and `NpcRouteTarget` route data model.
- Added `NpcRouteExecutor` and `NativeNpcRouteExecutor`.
- Wired the Falador shop assistant through the native route executor.
- Confirmed the current vertical slice is functional.

### Next

1. Review and stabilize the reusable route model.
2. Define a generic route-finder/context abstraction independent of schedules.
3. Identify the minimum NPC-compatible subset of the existing navigation graph.
4. Implement `BotNavMeshRouteExecutor` only after the route finder can accept NPC
   runtime state.
5. Add focused validation for route completion, no-op destinations, fallback
   behavior, and NPC lifecycle cleanup.

## Scope Notes

The bot navmesh implementation is intentionally deferred. The existing bot
graph should be adapted carefully rather than copied wholesale, because its
edge conditions and action execution may depend on player inventory, bot
frames, shortcuts, or instruction handling.
