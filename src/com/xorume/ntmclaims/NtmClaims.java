package com.xorume.ntmclaims;

import dev.golem.core.world.Protection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes NTM - Continuum respect FTB Chunks claims.
 *
 * NTM already asks Golem Core's {@link Protection} API before any explosion, nuke, fallout, fire, acid,
 * radiation block conversion, terraforming or block placement touches the world (and before many hazards
 * hurt an entity), but nothing registers a provider, so the answer is always "allowed". This mod registers
 * a provider backed by FTB Chunks.
 */
@Mod(NtmClaims.MODID)
public final class NtmClaims {
    public static final String MODID = "ntmclaims";
    public static final String VERSION = "1.0.3";
    public static final Logger LOG = LoggerFactory.getLogger("NTM Claims");
    static volatile boolean registered;

    public NtmClaims(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modBus.addListener(NtmClaims::setup);
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, NtmClaims::serverStarted);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> CheckCommand.register(e.getDispatcher()));
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Protection.register(FtbChunksProvider.INSTANCE);
            registered = true;
            LOG.info("Registered FTB Chunks protection provider for NTM");
        });
    }

    private static void serverStarted(ServerStartedEvent event) {
        for (String line : CheckCommand.statusLines()) LOG.info("[NTM Claims] {}", line.replaceAll("§.", ""));
    }
}
