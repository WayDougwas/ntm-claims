package com.xorume.ntmclaims;

import dev.ftb.mods.ftbchunks.FTBChunksWorldConfig;
import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkImpl;
import dev.ftb.mods.ftbchunks.data.PvPMode;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.golem.core.world.Protection;
import dev.golem.core.world.Protection.Cause;
import dev.golem.core.world.Protection.Coverage;
import dev.golem.core.world.Protection.Hazard;
import dev.golem.core.world.Protection.Source;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Golem Core protection provider backed by FTB Chunks. FTB claims are whole chunks, so {@link #coverage}
 * answers OPEN/CLOSED per chunk and Golem Core never has to ask block by block (keeps big nukes fast).
 */
public final class FtbChunksProvider implements Protection.Provider {
    public static final FtbChunksProvider INSTANCE = new FtbChunksProvider();

    private FtbChunksProvider() {}

    @Override
    public Coverage coverage(Level level, int chunkX, int chunkZ, Source source, Cause cause) {
        return allowed(level, new ChunkDimPos(level.dimension(), chunkX, chunkZ), source, cause) ? Coverage.OPEN : Coverage.CLOSED;
    }

    @Override
    public boolean mayAlter(Level level, BlockPos pos, Source source, Cause cause) {
        return allowed(level, new ChunkDimPos(level.dimension(), new ChunkPos(pos)), source, cause);
    }

    @Override
    public boolean mayHarm(Level level, Entity victim, Source source, Hazard hazard) {
        try {
            if (!Config.RESPECT_PVP.get() || !(victim instanceof Player) || !(level instanceof ServerLevel)) return true;
            UUID attacker = source == null ? null : source.player();
            if (attacker == null || attacker.equals(victim.getUUID())) return true; // only player-vs-player
            ClaimedChunk chunk = claim(new ChunkDimPos(victim));
            return chunk == null || pvpAllowed(chunk);
        } catch (Throwable t) {
            return failOpen(t);
        }
    }

    private static boolean allowed(Level level, ChunkDimPos pos, Source source, Cause cause) {
        try {
            if (!(level instanceof ServerLevel)) return true;
            if (!protects(cause)) return true;
            ClaimedChunk chunk = claim(pos);
            if (chunk == null) return true; // wilderness

            boolean result = decide(chunk, source, cause);
            if (Config.DEBUG.get()) {
                NtmClaims.LOG.info("[NTM Claims] {} chunk {} ({}) cause={} source={} -> {}",
                        result ? "ALLOW" : "DENY", pos, chunk.getTeamData().getTeam().getShortName(),
                        cause, source == null ? "none" : source.player(), result ? "allowed" : "protected");
            }
            return result;
        } catch (Throwable t) {
            return failOpen(t);
        }
    }

    private static boolean decide(ClaimedChunk chunk, Source source, Cause cause) {
        UUID player = source == null ? null : source.player();
        boolean blastLike = cause == Cause.EXPLOSION || cause == Cause.RADIATION_BLOCK_CONVERSION;

        if (blastLike) {
            // Explosions (craters, nukes) and their fallout follow the team's "Allow Explosions" setting,
            // like vanilla TNT does in FTB Chunks - no matter who detonated it.
            if (explosionsAllowed(chunk)) return true;
            if (player != null && FTBChunksAPI.api().getManager().getBypassProtection(player)) return true;
            boolean trust = cause == Cause.EXPLOSION ? Config.TRUST_EXPLOSIONS.get() : Config.TRUST_RADIATION.get();
            return trust && player != null && trusted(chunk, player);
        }

        // machines and small effects (excavator, mining laser, fire, fluids, placement): owners/allies are trusted
        if (player == null) return !Config.PROTECT_UNKNOWN_SOURCES.get();
        return trusted(chunk, player);
    }

    /**
     * Same answer FTB Chunks gives vanilla TNT for this chunk. Going through ClaimedChunkImpl.allowExplosions()
     * (instead of the team property) picks up mods that patch it, e.g. LC FTB Hook's paid per-region protection.
     */
    private static boolean explosionsAllowed(ClaimedChunk chunk) {
        if (chunk instanceof ClaimedChunkImpl impl) return impl.allowExplosions();
        return chunk.getTeamData().canExplosionsDamageTerrain();
    }

    /** Mirrors FTB Chunks' PvP rule (world pvp_mode, then the team/region setting). */
    private static boolean pvpAllowed(ClaimedChunk chunk) {
        PvPMode mode = FTBChunksWorldConfig.PVP_MODE.get();
        if (mode == PvPMode.ALWAYS) return true;
        if (mode == PvPMode.NEVER) return false;
        Boolean region = LcFtbHook.pvpAllowed(chunk);
        return region != null ? region : chunk.getTeamData().allowPVP();
    }

    private static boolean trusted(ClaimedChunk chunk, UUID player) {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        if (manager.getBypassProtection(player)) return true; // admins in /ftbchunks admin bypass mode
        ChunkTeamData team = chunk.getTeamData();
        if (Config.TRUST_MEMBERS.get() && team.isTeamMember(player)) return true;
        return Config.TRUST_ALLIES.get() && team.isAlly(player);
    }

    static ClaimedChunk claim(ChunkDimPos pos) {
        if (FTBChunksWorldConfig.DISABLE_PROTECTION.get() && !Config.IGNORE_DISABLE_PROTECTION.get()) {
            if (Config.DEBUG.get() && !warnedDisabled) {
                warnedDisabled = true;
                NtmClaims.LOG.warn("[NTM Claims] FTB Chunks 'disable_protection' is ON in this world - claims are not protected from NTM. "
                        + "Set protect.ignoreFtbDisableProtection = true in ntmclaims-common.toml to protect them anyway.");
            }
            return null;
        }
        FTBChunksAPI.API api = FTBChunksAPI.api();
        if (api == null || !api.isManagerLoaded()) return null;
        return api.getManager().getChunk(pos);
    }

    private static boolean warnedDisabled;

    /** For /ntmclaims check: would an ownerless NTM explosion be allowed in this chunk? */
    static boolean wouldAllowExplosion(ClaimedChunk chunk) {
        return decide(chunk, Source.NONE, Cause.EXPLOSION);
    }

    static boolean explosionsAllowedPublic(ClaimedChunk chunk) {
        return explosionsAllowed(chunk);
    }

    private static boolean protects(Cause cause) {
        return switch (cause) {
            case EXPLOSION -> Config.PROTECT_EXPLOSIONS.get();
            case FIRE -> Config.PROTECT_FIRE.get();
            case FLUID -> Config.PROTECT_FLUIDS.get();
            case RADIATION_BLOCK_CONVERSION -> Config.PROTECT_RADIATION.get();
            case TERRAFORM -> Config.PROTECT_TERRAFORM.get();
            case PLACEMENT -> Config.PROTECT_PLACEMENT.get();
        };
    }

    private static boolean warned;

    /** Never let a protection lookup crash the server: log once and allow. */
    private static boolean failOpen(Throwable t) {
        if (!warned) {
            warned = true;
            NtmClaims.LOG.error("FTB Chunks lookup failed; NTM protection is allowing this change", t);
        }
        return true;
    }
}
