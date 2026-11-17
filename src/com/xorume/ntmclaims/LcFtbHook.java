package com.xorume.ntmclaims;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Optional bridge to LC FTB Hook (paid claims). The hook moves PvP/explosion protection of player teams into
 * per-region settings; explosions are read through ClaimedChunkImpl.allowExplosions() (which the hook patches),
 * PvP needs this lookup. Server teams keep FTB's own team settings, exactly like the hook does.
 * Reflection only, so NTM Claims runs fine without the hook.
 */
final class LcFtbHook {
    private static boolean init;
    private static Method resolveRegion, resolveOwnership, createDefault, byId, isAtMinimum;

    private LcFtbHook() {}

    /** @return true/false from the hook's region, or null when the hook is absent or doesn't manage this chunk. */
    static Boolean pvpAllowed(ClaimedChunk chunk) {
        if (!ready()) return null;
        try {
            if (chunk.getTeamData().getTeam().isServerTeam()) return null;
            Object region = resolveRegion.invoke(null, chunk);
            if (region == null) region = createDefault.invoke(null);
            Object ownership = resolveOwnership.invoke(null, chunk);
            Object prop = byId.invoke(null, "allow_pvp");
            if (prop == null) return null;
            // "at minimum" = this protection is not bought/active -> PvP allowed
            return (Boolean) isAtMinimum.invoke(null, region, ownership, prop);
        } catch (Throwable t) {
            NtmClaims.LOG.warn("LC FTB Hook PvP lookup failed, using FTB team setting: {}", t.toString());
            resolveRegion = null;
            return null;
        }
    }

    private static boolean ready() {
        if (!init) {
            init = true;
            if (!ModList.get().isLoaded("lc_ftb_hook")) return false;
            try {
                Class<?> lookup = Class.forName("dev.malik.lcftbhook.adapter.ftb.FtbRegionLookup");
                Class<?> region = Class.forName("dev.malik.lcftbhook.data.Region");
                Class<?> ownership = Class.forName("dev.malik.lcftbhook.data.ChunkOwnership");
                Class<?> property = Class.forName("dev.malik.lcftbhook.data.ProtectionProperty");
                Class<?> resolution = Class.forName("dev.malik.lcftbhook.service.ProtectionResolution");
                resolveRegion = lookup.getMethod("resolveRegion", ClaimedChunk.class);
                resolveOwnership = lookup.getMethod("resolveOwnership", ClaimedChunk.class);
                createDefault = region.getMethod("createDefault");
                byId = property.getMethod("byId", String.class);
                isAtMinimum = resolution.getMethod("isAtMinimum", region, ownership, property);
                NtmClaims.LOG.info("LC FTB Hook found: NTM hazards follow its paid region protection");
            } catch (Throwable t) {
                NtmClaims.LOG.warn("LC FTB Hook found but its API changed; PvP uses FTB team settings: {}", t.toString());
                resolveRegion = null;
            }
        }
        return resolveRegion != null;
    }
}
