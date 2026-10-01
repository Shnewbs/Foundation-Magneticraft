package com.foundations.magneticraft.manual;

/** Original cycle duration and downstream activation delay; no Minecraft dependencies. */
public final class SluiceCycle {
    public static final int CAPACITY = 10;
    public static final int DURATION = 80;
    public static final int CHAIN_DELAY = 20;
    private int remaining;
    private int chainDelay;
    public boolean active() { return remaining > 0; }
    public int remaining() { return remaining; }
    public int chainDelay() { return chainDelay; }
    public boolean start() {
        if (active()) return false;
        remaining = DURATION;
        chainDelay = CHAIN_DELAY;
        return true;
    }
    public record Tick(boolean activateNext, boolean complete) {}
    public Tick tick() {
        if (!active()) return new Tick(false, false);
        boolean next = chainDelay > 0 && --chainDelay == 0;
        return new Tick(next, --remaining == 0);
    }
    public void restore(int ticks, int delay) {
        remaining = Math.max(0, Math.min(DURATION, ticks));
        chainDelay = remaining == 0 ? 0 : Math.max(0, Math.min(Math.min(CHAIN_DELAY, remaining), delay));
    }
    public static int insertionCount(int stored, int held) {
        if (stored < 0 || stored > CAPACITY || held < 0) throw new IllegalArgumentException("Invalid stack size");
        return Math.min(CAPACITY - stored, held);
    }
}
