![NTM Claims](res/ntmclaims_logo.png)

# NTM Claims

**Makes [NTM - Continuum](https://github.com/Warfactory-Official/NTM-Continuum) respect [FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) claims.**

NTM uses its own explosion system, so FTB Chunks never finds out when one of NTM's bombs goes off. On a server, anyone can fire a missile or set off a nuke and wipe out a claimed base. Fallout, fire, acid and other NTM effects get into claims the same way.

**NTM Claims fixes that.** NTM - Continuum already checks for protection before it changes the world, but nothing was answering those checks. This mod answers them using your FTB Chunks claims.

No mixins and no gameplay changes: NTM just skips the protected blocks before it does anything to them. That also means **no item duplication**, because protected blocks never drop items.

---

## 🛡️ What gets protected

Inside a claimed chunk:

| Source | Behavior |
|---|---|
| 💥 **Explosions** (bombs, charges, missiles, nukes, shrapnel...) | Blocked, unless the claim's team has **Allow Explosions** turned on in FTB Chunks |
| ☢️ **Radiation and fallout** turning blocks into dead or contaminated ones | Blocked |
| 🔥 **Fire** (incendiaries, flamethrowers, napalm, balefire...) | Blocked |
| 🧪 **Fluids** (acid, volcanic lava, gases, mud...) | Blocked |
| ⛏️ **Terraforming** (craters, boring missiles...) | Blocked |
| 🧱 **Block placement by effects** (debris, slag, taint...) | Blocked |
| ⚔️ **Hazards caused by another player** (radiation, gas, fire, blast) | Blocked where the claim has **PvP disabled** |

Blocks outside claims behave as normal. A nuke at the edge of a claim still levels the surrounding terrain and stops at the claim border.

### 🤝 Trusted players
NTM keeps track of who placed a bomb, missile or machine.
- **Explosions, nukes and fallout follow only the claim's _Allow Explosions_ setting**, just like TNT in FTB Chunks. Even **your own** nuke won't crater or contaminate your own claim unless your team allows explosions.
- **Machines and smaller effects** (Excavator, Mining Laser, fire, fluids...) trust the claim's **team members** and **allies**, so your own machines keep working in your base.
- **Admins** in FTB Chunks bypass mode are always trusted.

### ⚡ Performance
FTB Chunks claims cover whole chunks, so the check runs once per chunk, not once per block. Even very large nukes stay fast.

---

## ⚙️ Configuration
`config/ntmclaims-common.toml`

- Turn protection on or off for each category: `explosions`, `fire`, `fluids`, `radiation`, `terraform`, `placement`
- `trust.members` / `trust.allies`: whether a claim's team members and allies are trusted for machines and small effects
- `trust.explosions` / `trust.radiation`: also trust them for explosions/nukes and fallout (off by default)
- `debug.logDecisions`: log every protection decision, which is useful while testing
- `protect.ignoreFtbDisableProtection`: keep protecting claims from NTM even when FTB Chunks' `disable_protection` is on
- `/ntmclaims check` (OP): shows whether the mod is active and whether an NTM explosion would be blocked in the chunk you stand in
- `trust.protectUnknownSources`: protect claims from changes that have no known owner, such as ambient radiation or natural spreading
- `pvp.respectClaimPvp`: apply the claim's PvP setting to NTM hazards

FTB Chunks' own `disable_protection` and `pvp_mode` settings are respected too.

### 💰 LC FTB Hook
If **LC FTB Hook** (paid claims) is installed, NTM follows its paid region protections: a region that bought explosion protection is safe from NTM explosions, nukes and fallout, and one that did not is not. Server teams keep using their normal FTB Chunks settings, like they do in the hook.

---

## 📦 Requirements
- Minecraft **1.21.1** with **NeoForge**
- **NTM - Continuum** + **Golem Core**
- **FTB Chunks** (+ FTB Library, FTB Teams and Architectury)

Install it on the **server**. Clients don't need it, but it doesn't hurt to have it there too.

---

## ❓ FAQ

**Does it stop NTM in unclaimed land?**
No. Only claimed chunks are protected.

**Can I still blow things up in my own claim?**
Turn on **Allow Explosions** in your team's FTB Chunks settings, or set `trust.explosions = true` in the config.

**Something from NTM still damages my claim. What now?**
This mod protects everything NTM routes through its protection checks, which covers explosions, nukes, fallout, fire, fluids, radiation and terraforming. If you find an effect that gets through, please report which one in the comments or on the issue tracker.

**Is this official?**
No. It's an unofficial addon and isn't affiliated with the NTM - Continuum team or with FTB.

---

## 🙏 Credits
- **HbMinecraft** and the HBM's Nuclear Tech Mod team
- The **NTM - Continuum** team (Warfactory), whose protection API makes this possible
- **FTB** (Feed The Beast), for FTB Chunks

---

### 🇧🇷 Português
**NTM Claims** faz o **NTM - Continuum** respeitar as claims do **FTB Chunks**. Dentro de áreas protegidas, explosões, nukes, mísseis, fallout, radiação, fogo, ácido e crateras do NTM deixam de destruir blocos. Também deixa de valer o dano causado por outro jogador onde o PvP da claim está desligado. Explosões e nukes seguem a opção "Allow Explosions" da claim, mesmo quando quem detona é do próprio time. Já as máquinas de membros e aliados continuam funcionando nas próprias claims. Basta instalar no servidor.

---

## 🔨 Building
See `build.sh`. It compiles with plain `javac` (Java 21) against the jars of a NeoForge 1.21.1 instance and packs the jar.

## 📄 License
MIT
