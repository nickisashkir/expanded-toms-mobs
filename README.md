# Expanded Toms Mobs

A fork of [tomalbrc/toms-mobs](https://github.com/tomalbrc/toms-mobs) (MIT), extended for use on [The Remnants](https://theremnants.gg/) with new behaviors, systems, and a much larger mob roster.

Runs entirely **server-side** via [Polymer](https://modrinth.com/mod/polymer) - players join with a vanilla client, no mods required.

---

## Setup

Install alongside:
- [Polymer](https://modrinth.com/mod/polymer)
- [Fabric-API](https://modrinth.com/mod/fabric-api)

The server auto-generates a resource pack via blockbench-import-library and serves it through Polymer's autohost. See [Polymer's resource pack hosting guide](https://polymer.pb4.eu/latest/user/resource-pack-hosting/) for setup.

Entities live under the `toms_mobs` namespace (`/summon toms_mobs:reindeer`, etc.).

> **Note:** The `.bbmodel` art assets are **not** included in this repo. See [Assets Not Included](#assets-not-included) below.

---

# New Systems

## Taming & Bonding
- **Canine + feline mobs are tamable** - feed the right item (bones for canines, cod/salmon for felines), 1/3 success roll, heart particle burst on success
- **Sit toggle** - right-click your tamed pet to make it sit; right-click again to release
- **Heal on feed** - feed food to a tamed mob below max health to heal
- **Independent mode** - shift + right-click with empty hand toggles wander mode. The mob stores its current spot as "home" and stops following you; if it strays more than 16 blocks it walks back on its own. Persists across server restarts.
- **Owner UUID is saved**, so ownership survives reloads and unloads

## Breeding, Babies & Growth
- **Heart particles** on successful tame and breed
- **Babies spawn naturally** alongside parents via the spawn ticker with a configurable baby chance per entry
- **Baby → adult growth timer** - 60 real minutes (72000 ticks). When the timer fires, the baby entity is replaced by an adult at the same position, preserving rotation, head direction, and custom name
- **Golden dandelion freezes growth permanently** - feed a baby a golden dandelion and it stays a baby forever. XP orb pickup sound + happy villager particle burst confirmation.
- Growth state saves to NBT (`BabyAge`, `BabyAgeFrozen`) so progress survives restarts

## Names & Identity
- **Every mob automatically gets its species as a custom name** on first tick, so Jade tooltips and vanilla floating nametags show "Reindeer", "Bombay Cat", etc. with zero setup
- **Nametag is proximity-gated** to 6 blocks so you don't get name clutter from a hundred mobs across the field
- **Name tag item renaming works** and persists across reloads
- **Custom names survive baby → adult growth** - a renamed baby keeps its name when it grows up

## Health Display
- Floating heart bar appears above a mob **only when you aim at it** (8 block crosshair raycast)
- Red hearts for full, gray for empty, scales with max health
- Auto-hides 2 seconds after you look away

## NPC Framework & Patrol Routes
- **AbstractNpc** base with knockback immunity so NPCs don't get shoved around by players
- Per-NPC random idle animations on a timer (salutes, waves, librarian falling over, etc.)
- **Named patrol routes** stored in `config/toms_mobs_patrols.json`
  - Each route has waypoints and assigned NPC UUIDs
  - NPCs cycle waypoints with a configurable pause at each stop
  - Full admin command suite for creating and managing routes

## Custom Spawn Ticker
- **Layered cap system** to keep populations sane:
  - Hard total cap: 40 Tom's Mobs in a 400-block radius around any player
  - Soft cap: configurable per type within 128 blocks (default 12)
  - Immediate proximity guard: won't spawn the same type if any exist within 64 blocks
  - Per-player attempt loop on a configurable interval (default 24000 ticks)
- **Natural placement check** - only spawns on grass / dirt / sand / stone / terracotta / snow with light > 8 (no base invasions)
- **Chunk dedup** so multiple players don't quadruple-spawn the same area
- **Per-player opt-out** via `/toggleanimals`, persisted in `toms_mobs_optout.json`
- **Force-spawn admin commands** for testing (`forcespawn` for land, `forceocean` for aquatic)

---

# New AI Behaviors

### Land predators
- Hunt-rabbit and hunt-silverfish target goals on a small fox category
- Wild canines target small rodents; tamed ones leave them alone
- **Allay-style fetch** - tamed canines scan 10 blocks around their owner for dropped items, walk over, "carry" the item, walk back, and drop it at the owner's feet. 3s cooldown, 20s timeout.

### Prey
- Avoid-entity goal on small rodents targeting both canines and felines (10 block range, slow + sprint speeds)
- **Curl defense** on a spiky shore-dwelling category: 75% damage reduction while curled, 2-second curl triggered on hit

### Aquatic
- **Flee-to-water** on shore-dwellers: getting hit triggers a 10s panic where the mob scans 16 blocks for water and books it at 1.6x speed
- Speed swap on amphibious mobs: fast in water, slow on land

### Avian
- **Day / night activity cycle** on a nocturnal bird category: faster move + fly speeds at night, sleepy by day
- **Burst-fly panic** on a small ground bird: when a player gets within 4 blocks the bird gets a sudden upward velocity + fly animation + parrot fly sound
- **Cat chase goal** - cats target chickens and winter birds, walk to within 1.5 blocks but stop short of damaging them. Tamed cats chase rarely (1/200 chance), wild cats often (1/80).

### Social
- **Lookout behavior** on a small burrowing category: when standing still with 2+ kin within 12 blocks, 1/3 chance every 5 seconds to play a peek animation for 3-6 seconds

---

# New Mobs

All new mobs spawn via the custom ticker (can be force-spawned with `/tomsmobs forcespawn` or `/summon`).

### Land mammals
- **Antelope** (+ baby) - herd grazer
- **Giraffe** (+ baby) - tall savanna herbivore
- **Zebra** (+ baby) - herd animal
- **Tiger** (+ baby) - jungle predator
- **Meerkat** (+ baby) - with lookout/peek social behavior
- **Raccoon** (+ baby) - forest scavenger
- **Ice Penguin** (+ baby) - snowy biome swimmer
- **Reindeer** - eats grass like a sheep, heals when feeding
- **Red Panda** - bamboo forest climber
- **Fennec Fox** - desert hunter, targets rabbits and silverfish
- **Otter** - river/beach swimmer
- **Hedgehog** - curls defensively when hit (75% damage reduction)
- **Partridge** - burst-flies when players get too close

### Tamable pets
- **Dogs (17 breeds)** - tame with bones. Breeds: Bulldog (English), Corgi, Dalmation, Finnish Lapphund (Black / Ginger / White), German Shepherd, Husky (Black / Brown), Labrador (Black / Chocolate / Yellow), Poodle (Black / Brown / White), Pug (Black / Yellow). Wild dogs chase squirrels. Tamed dogs fetch dropped items for their owner.
- **Cats (13 variants)** - tame with cod or salmon. Variants: Bombay, Chartreux, Egyptian Mau (Brown / Gray), Japanese Bobtail, Maine Coon (Brown / Gray), Orange, Siamese, Sphynx, Stray, Tabby, Tuxedo. Cats hunt chickens and winter birds (stare-only, no damage).

### Birds
- **Owls (4 variants)** - Barn, Horned, Long-Eared, Snowy. Day/night activity cycle: sleepy in day, active and faster at night. Hunt chickens and rabbits.
- **Ducks (Brown / Mallard / White) + Ducklings** - semi-aquatic, float on water, separate water/land animations
- **Winter birds (Blue Tit / Cardinal / Robin)** - small perching birds for winter biomes

### Aquatic
- **Seahorses (5 colors)** - Black, Blue, Green, Purple, Red
- **Jellyfish (6 variants)** - Blue, White, Pink, Orange, Golden (luminescent), Fusion
- **Koi (4 colors)** - Gold, Orange, Red, White
- **Seals (3 variants)** - Arctic, Harbor, Monk. Fast in water, slow on land.
- **Crabs (3 colors)** - Blue, Orange, Red. Flee to nearest water when hit.

### Small creatures
- **Squirrels (3 colors)** - Brown, Gray, Red. Avoid dogs and cats.
- **Sloths (2 variants)** - Brown, Gray

### NPCs (20+ variants)
- **Blacksmiths** (5)
- **Farmers** (5)
- **Librarians** (7)
- **Guards** (2)

---

# Commands

## `/tomsmobs` (admin / operator)

Spawn ticker:
```
/tomsmobs status              → show ticker config
/tomsmobs reload              → reload config from disk
/tomsmobs ticker <true|false> → enable/disable custom spawner
/tomsmobs interval <ticks>    → spawn check frequency (20-72000)
/tomsmobs softcap <n>         → per-mob-type soft cap (1-100)
```

Counting and force spawns:
```
/tomsmobs count [radius]      → count Tom's Mobs near you (default 128)
/tomsmobs total               → total Tom's Mobs on the server
/tomsmobs forcespawn [n]      → force-spawn land mobs (1-50 attempts)
/tomsmobs forceocean          → force-spawn ocean mobs in nearby water
```

Patrol routes for NPCs:
```
/tomsmobs patrol create <name>   → create new route
/tomsmobs patrol add <name>      → add waypoint at your position
/tomsmobs patrol assign <name>   → assign nearest NPC to route
/tomsmobs patrol unassign        → unassign nearest NPC
/tomsmobs patrol list            → list all routes
/tomsmobs patrol delete <name>   → delete a route
```

## `/toggleanimals` (player)

Opts you in or out of the custom mob spawner. Persisted per-player in `toms_mobs_optout.json`. Opting out still lets vanilla biome spawns run.

---

# Art Credit

**Most** of the mob models used by this fork are from **Nog's Menagerie**, a premium Blockbench model pack by Nog ([shop page](https://mcmodels.net/vendors/24/nog)). Huge shout-out - the packs are fantastic and absolutely worth picking up if you're building anything similar.

Additional models are from **Scenes** and other licensed Blockbench creators. The remaining models (butterfly, capybara, elephant, lobster, mantaray, possum, seagull, snake, tuna, and a few others) come from the original upstream toms-mobs by Tom Albrecht.

# Assets Not Included

The added mob `.bbmodel` files are **not** committed here because they are licensed content and cannot be redistributed.

**To build / run this mod with full visuals you must:**

1. Purchase the corresponding packs from Nog's shop: https://mcmodels.net/vendors/24/nog
2. Drop the purchased `.bbmodel` files into `src/main/resources/model/toms_mobs/`
3. Build with `./gradlew build`

Without the purchased assets the mod still compiles, but any mob whose model is missing will render as an invisible entity.

---

# Credits

- Original mod by **Tom Albrecht** ([tomalbrc](https://github.com/tomalbrc)) - MIT
- Models by **Nog** (Nog's Menagerie) and **Scenes**
- Fork maintained by **[nickisashkir](https://github.com/nickisashkir)** for [The Remnants](https://theremnants.gg/)
- Bird brain from [Fowl-Play](https://github.com/aqariio/Fowl-Play) by aqariio (see `LICENSE-fowl-play.md`)

---
---

# Original Toms Mobs (Upstream)

Everything below describes the mobs shipped by the original [tomalbrc/toms-mobs](https://github.com/tomalbrc/toms-mobs). These are preserved in this fork for compatibility but most are commented out in the registry and not used on The Remnants server.

## Passive / Ambient

### Snake
- **Type:** Passive
- Can be bred.
- **Spawns in:** Jungles, Deserts, Swamps
- **Food:** Any kind of meat

### Capybara
- **Type:** Passive
- The world's largest rodent! You can pet it!
- **Spawns in:** Swamps, near Rivers
- **Food:** Apple, Melon, Pumpkin, Sugar Cane

### Possum
- **Type:** Passive
- It's a poppy! Don't pet it, it will not like it. (okay maybe a little)
- **Spawns in:** Forests

### Firemoths
- **Type:** Passive
- Adds atmosphere to the Nether.
- **Spawns in:** All Nether biomes (except Basalt Deltas)

### Butterfly
- **Type:** Passive
- Appear in a variety of colors and bred. They have a rare chance to spawn a large butterfly when breeding.
- **Spawns in:** Overworld (except cold biomes)
- **Food:** Flowers

### Penguin
- **Type:** Passive
- Flightless birds!
- **Spawns in:** Snowy/Icy biomes
- **Drops:** 1-3 Feathers
- **Food:** Raw Salmon, Raw Cod

### Elephant
- **Type:** Passive, Rideable
- Large, rideable animals with improved health.
- **Spawns in:** Savanna Plateau, Jungle
- **Food:** Sugar, Sugar Cane, Bamboo

### Seagull
- **Type:** Passive
- Eats any food item it comes across. Will attack tadpoles and fish!
- **Spawns in:** Beaches

### Mantaray
- **Type:** Passive
- **Spawns in:** Oceans

### Yellowfin Tuna
- **Type:** Passive
- Large, fast-swimming fish.
- **Spawns in:** Oceans

### Lobster
- **Type:** Passive
- Can be bred. Will dance when a jukebox is playing nearby.
- **Spawns in:** Oceans, Rivers, Beaches, Mangrove Swamps

## Hostile

### Sculkling
- **Type:** Hostile
- Steals your XP when it attacks. Defeat it to reclaim it.
- **Spawns in:** All caves except lush caves, below y-level 40

### Iceologer
- **Type:** Hostile
- The Mob Vote Illager that summons icy spikes and clusters to freeze you in place.
- **Spawns in:** Snowy mountains, above y-level 150, only on snow/ice

### Showmaster
- **Type:** Hostile
- A powerful Illager that throws potions, fires rapid arrows, and summons circles of fangs.
- **Spawns in:** Does not spawn naturally (commands or spawn egg only)

## Tom's Test Server

Join Tom's test server with a 1.21.11 client at **mc.tomalbrc.de:25565** to try upstream toms-mobs and Tom's other mods.

Tom's discord:
[![discord invite](https://img.shields.io/badge/discord-label?style=for-the-badge&logo=discord&logoColor=%23ffffff&labelColor=%237289da&color=%237289da)](https://discord.gg/9X6w2kfy89)
