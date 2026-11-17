package com.xorume.ntmclaims;

import com.mojang.brigadier.CommandDispatcher;
import dev.ftb.mods.ftbchunks.FTBChunksWorldConfig;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/** /ntmclaims check - shows why NTM would or would not be blocked where you stand. */
final class CheckCommand {
    private CheckCommand() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("ntmclaims")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("check").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    List<String> lines = statusLines();
                    if (src.getEntity() instanceof ServerPlayer player) lines.addAll(chunkLines(player));
                    for (String l : lines) src.sendSuccess(() -> Component.literal(l), false);
                    return 1;
                })));
    }

    static List<String> statusLines() {
        List<String> l = new ArrayList<>();
        l.add("§6=== NTM Claims " + version() + " ===");
        l.add("Provider registered with Golem Core: " + yn(NtmClaims.registered));
        boolean disabled = FTBChunksWorldConfig.DISABLE_PROTECTION.get();
        l.add("FTB disable_protection: " + (disabled ? "§cON" : "§aoff")
                + (disabled ? (Config.IGNORE_DISABLE_PROTECTION.get() ? " §7(ignored by config)" : " §c-> claims NOT protected from NTM!") : ""));
        FTBChunksAPI.API api = FTBChunksAPI.api();
        l.add("FTB Chunks manager loaded: " + yn(api != null && api.isManagerLoaded()));
        l.add("Protect explosions: " + yn(Config.PROTECT_EXPLOSIONS.get()) + "  LC FTB Hook: " + yn(ModList.get().isLoaded("lc_ftb_hook")));
        l.add("Debug logging: " + yn(Config.DEBUG.get()));
        return l;
    }

    private static List<String> chunkLines(ServerPlayer player) {
        List<String> l = new ArrayList<>();
        ChunkDimPos pos = new ChunkDimPos(player);
        ClaimedChunk chunk = FtbChunksProvider.claim(pos);
        if (chunk == null) {
            l.add("This chunk " + pos + ": §enot claimed (or protection off) -> NTM allowed");
            return l;
        }
        Team team = chunk.getTeamData().getTeam();
        l.add("This chunk " + pos + ": claimed by §b" + team.getShortName() + "§r (server team: " + yn(team.isServerTeam()) + ")");
        l.add("FTB/hook allows explosions here: " + yn(FtbChunksProvider.explosionsAllowedPublic(chunk)));
        boolean allow = FtbChunksProvider.wouldAllowExplosion(chunk);
        l.add("Ownerless NTM explosion here: " + (allow ? "§cALLOWED" : "§aBLOCKED"));
        return l;
    }

    private static String yn(boolean b) { return b ? "§ayes§r" : "§cno§r"; }

    private static String version() {
        return NtmClaims.VERSION;
    }
}
