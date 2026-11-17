package com.xorume.ntmclaims;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue PROTECT_EXPLOSIONS;
    public static final ModConfigSpec.BooleanValue PROTECT_FIRE;
    public static final ModConfigSpec.BooleanValue PROTECT_FLUIDS;
    public static final ModConfigSpec.BooleanValue PROTECT_RADIATION;
    public static final ModConfigSpec.BooleanValue PROTECT_TERRAFORM;
    public static final ModConfigSpec.BooleanValue PROTECT_PLACEMENT;
    public static final ModConfigSpec.BooleanValue TRUST_MEMBERS;
    public static final ModConfigSpec.BooleanValue TRUST_ALLIES;
    public static final ModConfigSpec.BooleanValue PROTECT_UNKNOWN_SOURCES;
    public static final ModConfigSpec.BooleanValue RESPECT_PVP;
    public static final ModConfigSpec.BooleanValue TRUST_EXPLOSIONS;
    public static final ModConfigSpec.BooleanValue TRUST_RADIATION;
    public static final ModConfigSpec.BooleanValue DEBUG;
    public static final ModConfigSpec.BooleanValue IGNORE_DISABLE_PROTECTION;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Which kinds of NTM world changes are blocked inside FTB Chunks claims.").push("protect");
        PROTECT_EXPLOSIONS = b.comment(
                "Explosions, nukes, missiles, bullets/shrapnel breaking blocks.",
                "Follows the claim's own 'Allow Explosions' team setting: if the team allows explosions, NTM explosions work there too.")
                .define("explosions", true);
        PROTECT_FIRE = b.comment("Fire placed by NTM (incendiaries, flamethrowers, napalm, balefire...).")
                .define("fire", true);
        PROTECT_FLUIDS = b.comment("NTM fluids/blocks spreading into claims (acid, volcanic lava, mud, gases...).")
                .define("fluids", true);
        PROTECT_RADIATION = b.comment("Blocks converted by radiation/fallout (dead grass, contaminated blocks, fallout layer...).")
                .define("radiation", true);
        PROTECT_TERRAFORM = b.comment("Terraforming effects (craters, digging/boring missiles, machines carving terrain...).")
                .define("terraform", true);
        PROTECT_PLACEMENT = b.comment("NTM effects placing blocks inside a claim (debris, slag, taint, crashed bombs...).")
                .define("placement", true);
        IGNORE_DISABLE_PROTECTION = b.comment(
                "FTB Chunks' world setting 'disable_protection' (serverconfig/ftbchunks-world.snbt) normally turns this mod off too.",
                "Set true to keep protecting claims from NTM even when FTB's own land protection is disabled",
                "(e.g. when another mod such as Admin Chunk Manager handles block protection).")
                .define("ignoreFtbDisableProtection", false);
        b.pop();

        b.comment("Who is trusted inside a claim (when NTM knows who caused the change, e.g. the player who placed the bomb or machine).").push("trust");
        TRUST_MEMBERS = b.comment("Members of the claim's team may use NTM machines/effects in their own claims (excavator, mining laser, fire, fluids...).")
                .define("members", true);
        TRUST_ALLIES = b.comment("Allies of the claim's team are trusted too.")
                .define("allies", true);
        TRUST_EXPLOSIONS = b.comment(
                "Also trust members/allies for EXPLOSIONS (including nukes). Default false: explosions only follow the claim's",
                "'Allow Explosions' setting, like TNT in FTB Chunks - so even your own nuke won't crater your own claim.")
                .define("explosions", false);
        TRUST_RADIATION = b.comment(
                "Also trust members/allies for radiation/fallout block conversion. Default false (your own nuke's fallout won't",
                "contaminate your claim unless the claim allows explosions).")
                .define("radiation", false);
        PROTECT_UNKNOWN_SOURCES = b.comment(
                "When NTM can't tell who caused a change (ambient radiation, natural spreading, unowned entities),",
                "still protect claims. If false, unknown sources are allowed everywhere (explosions still follow the claim's 'Allow Explosions').")
                .define("protectUnknownSources", true);
        b.pop();

        b.push("pvp");
        RESPECT_PVP = b.comment("Stop NTM hazards caused by another player (radiation, gas, fire, blast...) from hurting players",
                "inside claims where FTB Chunks disallows PvP.")
                .define("respectClaimPvp", true);
        b.pop();

        b.push("debug");
        DEBUG = b.comment("Log every protection decision (chunk, team, cause, who caused it). Turn on only while testing.")
                .define("logDecisions", false);
        b.pop();

        SPEC = b.build();
    }

    private Config() {}
}
