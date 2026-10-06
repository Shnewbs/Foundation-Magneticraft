package com.foundations.magneticraft.manual;

/** Water source policy; passive batches do not spend or fill the pipe buffer. */
public final class SluiceWaterSupply {
    private SluiceWaterSupply() {}
    public enum Start { NONE, SOURCE, BUFFER }
    public static Start start(boolean validInput, boolean source, int water) {
        if (!validInput) return Start.NONE;
        if (source) return Start.SOURCE;
        return water == WaterBudget.CAPACITY ? Start.BUFFER : Start.NONE;
    }
    public static boolean canAdvance(boolean passiveCycle, boolean source) {
        return !passiveCycle || source;
    }
}
