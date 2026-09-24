# AI Assistant Instructions — Void (RuneScape 2011 Server)

## Project Overview

- Custom build of [Void](https://github.com/GregHib/void): a RuneScape 2011 server emulation written in Kotlin.
- Runtime: Java 21 (JDK 21+), Kotlin 2.3.
- Engine source lives under `game/src/main/kotlin/`; entry point is `Main.kt`.
- Server config is in `game.properties`; cache files go into `/data/cache/`.
- Full docs: https://greghib.github.io/void/docs
- **PvP is disabled** — there is no player-versus-player combat on this server. 
- Do not design or reason around PK, ganking, or Wilderness safety as a concern.

## Branch Strategy

- **`main`** — holds the canonical 2011 restoration content. Treat it as the upstream baseline.
  Do not add personal content here unless explicitly asked.
- **`personal-flavor`** — the branch for all personal additions and gameplay tuning.
  This is the **default branch to work on**.

## Goal of `personal-flavor`

The branch must be:

1. **Solo-friendly** — a single player can progress and complete content on their own;
   no forced grouping where avoidable.
2. **Multiplayer-friendly** — friends can play together with minimal friction and few issues:
   shared areas stay stable, no obvious exploits, low performance impact.
3. **Low-friction** — prefer fixes that remove edge cases (stuck NPCs, orphaned objects,
   desynced states, client crashes) over new features.

When in doubt, choose the option that makes solo *and* group play smoother and harder to break.

## Personal Content (personal-flavor)

Orientation index for the personal systems on this branch. Treat "planned" items as in-progress —
verify current behavior in code before assuming anything is shipped.

### Day/Night Cycle

- A full day/night cycle runs on the server and drives time-based behavior.
- **Banks and general stores close at night** — their availability is tied to the in-game time of day.
- **Several NPCs have full schedules** that cycle around the day (spawn/despawn, movement, dialogue, availability).
- This is a **living system the owner plans to expand**. When adding or changing anything, keep timing
  consistent with the existing cycle and check that nothing breaks at the day/night boundary.
- Checklist for time-gated content: confirm midnight transitions reset cleanly, NPCs/stores re-open correctly,
  and group play near a transition doesn't desync.

### Sin Mister

- **Custom ghost banker NPC.**
- Acts as a **24/7 banker** at the **Chaos Temple** (wilderness), near the altar where you can sacrifice bones.
- **Planned (not yet built):** a thieving-centered quest centered on robbing banks, undertaken with Sin Mister.
- Note: as a 24/7 banker he is *deliberately outside* the normal bank closing hours — treat this as an
  intentional exception to the day/night store rules, not a bug.
- When working on the planned bank-robbing quest, ensure: no item dupes/free items from the robbery,
  solo-completable, and group-safe.

### Aaroc

- **Custom NPC.**
- **Teleports to a random location each day from a defined set of locations** (the list lives in the NPC's
  script/data — check it before assuming where he can appear); at night he retreats to **Zanaris**.
- Currently provides an **alternative way to obtain wands** (a personal replacement for the mage arena,
  which the owner dislikes).
- Longer-term plan for Aaroc is **not yet defined**.
- When touching Aaroc: keep the daily random teleport and the night retreat to Zanaris stable/deterministic,
  and make sure the wand-provisioning path has no exploit or dupe.

### Runecrafting

- **Heavily reworked** from the 2011 baseline. The edited script is applied directly to the cache
  (`script_1012`, `cs2` format), so the in-cache data is the source of truth.
- **New rune tiers** added on top of the standard set: Mist, Dust, Mud, Smoke, Steam, Lava, Cosmic.
- **Rune levels are adjusted** from standard 2011 (e.g. Mind is 2, Earth is 9, Fire is 14,
  Body is 20, Nature is 44, Law is 54, Death is 65, Blood is 77 — verify against the script before assuming).
- **Rune-per-essence scaling** goes far beyond the 2011 baseline, reaching up to 21 runes per essence
  at level 99.

### Smithing

- **Heavily reworked** from the 2011 baseline. Level/XP data lives in `smithing.tables` (TOML),
  applied to the cache — treat it as the source of truth.
- **Each metal tier spans 10 levels:** Bronze (1), Iron (10), Steel (20), Mithril (30),
  Adamant (40), Rune (50).
- **Iron no longer has a 50% fail rate** (unlike the 2011 baseline).
- **XP values are adjusted** per item relative to the 2011 baseline — do not assume standard XP.
- When touching smithing: do **not** assume standard 2011 fail rates, XP values, or item lists.
  Check `smithing.tables` first.

## Working Rules

- Keep changes small, focused, and easy to review; one logical change per commit.
- Follow the style of the surrounding Kotlin codebase.
- Prefer adding content via Void's script system instead of modifying engine internals.
- Do not alter restoration behavior from `main`; personal-flavor changes should be additive.
- Never propose untested behavior: run the server locally and exercise the changed feature in-game.
- Run `./gradlew spotlessApply` before committing.
- Don't paste large blocks of existing code into responses; reference file paths instead.

## Build & Run

```bash
./gradlew build -x test   # build the project
./gradlew run             # run the server locally
./gradlew spotlessApply   # format code before commit
```

- Requires Java 21+ (`java --version`).
- Cache files must be extracted into `/data/cache/` before running.
- Successful startup prints `[Main] - Void loaded in ...ms`.

## Content Checklist (personal-flavor)

For any new or modified content, verify:

- **Solo**: can one player complete it alone? What happens if they abandon it mid-way
  (no stuck NPCs, orphaned objects, or unresetable state)?
- **Group**: are spawns/resets safe with several players overlapping? Any exploitable
  free kills, item dupes, or skip-paths?
- **Time**: does it behave correctly across day/night transitions?
- **Skills**: does it align with the *edited* runecrafting/smithing levels, XP, and fail rates
  (not the 2011 defaults)?
- **Performance**: no per-tick hot-spots that scale badly with nearby players.
- **Compatibility**: works with the 2011 client and existing cache.

## Handling Issues

1. Identify the branch: restoration bug → `main`; personal content → `personal-flavor`.
2. Reproduce locally before proposing a fix.
3. Prefer the smallest diff that resolves the issue.
4. Note in the summary anything that could regress solo, group, or time-based play.

## References

- Void wiki: https://greghib.github.io/void/docs
- Scripts guide: https://greghib.github.io/void/docs/scripts
- Prebuilt client: https://github.com/GregHib/void-client
```