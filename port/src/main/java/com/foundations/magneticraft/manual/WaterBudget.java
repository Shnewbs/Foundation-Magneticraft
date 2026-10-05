package com.foundations.magneticraft.manual;

public final class WaterBudget {
    public static final int CAPACITY = 1000;
    private WaterBudget() {}
    public static int insertion(int stored, int requested, boolean active) {
        if (stored < 0 || stored > CAPACITY || requested < 0) throw new IllegalArgumentException("Invalid water amount");
        return active ? 0 : Math.min(requested, CAPACITY - stored);
    }
    public static int restored(int amount) { return Math.max(0, Math.min(CAPACITY, amount)); }
}
