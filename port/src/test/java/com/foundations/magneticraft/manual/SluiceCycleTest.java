package com.foundations.magneticraft.manual;
public final class SluiceCycleTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        SluiceCycle cycle = new SluiceCycle();
        require(cycle.start(), "Idle cycle fails to start");
        require(!cycle.start(), "Busy cycle restarts");
        int chains = 0, completions = 0;
        for (int tick = 1; tick <= 80; tick++) {
            SluiceCycle.Tick event = cycle.tick();
            if (event.activateNext()) { chains++; require(tick == 20, "Wrong chain timing"); }
            if (event.complete()) { completions++; require(tick == 80, "Early completion"); }
        }
        require(chains == 1 && completions == 1 && !cycle.active(), "Cycle repeats/loses an event");
        require(!cycle.tick().complete() && !cycle.tick().activateNext(), "Idle cycle emits events");
        cycle.restore(65, 5);
        chains = 0; completions = 0;
        for (int tick = 1; tick <= 65; tick++) {
            SluiceCycle.Tick event = cycle.tick();
            if (event.activateNext()) { chains++; require(tick == 5, "Reload loses chain delay"); }
            if (event.complete()) { completions++; require(tick == 65, "Reload loses progress"); }
        }
        require(chains == 1 && completions == 1, "Restored cycle duplicates events");
        cycle.restore(30, 0);
        for (int tick = 0; tick < 30; tick++) require(!cycle.tick().activateNext(), "Reload repeats dispatched chain");
        require(SluiceCycle.insertionCount(0, 64) == 10, "Batch exceeds capacity");
        require(SluiceCycle.insertionCount(7, 64) == 3, "Top-up exceeds capacity");
        require(SluiceCycle.insertionCount(10, 1) == 0, "Full machine consumes input");
        require(SluiceCycle.insertionCount(0, 0) == 0, "Empty stack inserts items");
        cycle.restore(-1, 20);
        require(!cycle.active() && cycle.chainDelay() == 0, "Invalid persisted cycle");
        System.out.println("Sluice timing, capacity, restart and saved chain checks passed");
    }
}
