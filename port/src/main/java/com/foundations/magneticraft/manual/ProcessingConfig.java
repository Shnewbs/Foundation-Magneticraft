package com.foundations.magneticraft.manual;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProcessingConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue BLAZE_FIRE;
    static {
        var builder = new ModConfigSpec.Builder();
        BLAZE_FIRE = builder.comment("Original crushing-table behavior: hitting blaze rods ignites the player for five seconds.").define("crushingTableCausesFire", true);
        SPEC = builder.build();
    }
    private ProcessingConfig() {}
}
