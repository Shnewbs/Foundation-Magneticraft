package com.foundations.magneticraft.manual;

public final class SluiceWaterSupplyTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        for (int water : new int[] {0, 1, 999, 1000}) {
            check(SluiceWaterSupply.start(false, true, water) == SluiceWaterSupply.Start.NONE);
            check(SluiceWaterSupply.start(true, true, water) == SluiceWaterSupply.Start.SOURCE);
            check(SluiceWaterSupply.start(true, false, water) ==
                (water == 1000 ? SluiceWaterSupply.Start.BUFFER : SluiceWaterSupply.Start.NONE));
        }
        check(SluiceWaterSupply.canAdvance(false, false));
        SluiceCycle cycle = new SluiceCycle();
        cycle.start();
        int chains = 0, completions = 0;
        for (int time = 0; time < 180; time++) {
            boolean source = time < 10 || time >= 110;
            if (SluiceWaterSupply.canAdvance(true, source)) {
                var tick = cycle.tick();
                if (tick.activateNext()) chains++;
                if (tick.complete()) completions++;
            }
            if (time >= 10 && time < 110) check(cycle.remaining() == 70);
        }
        check(chains == 1 && completions == 1 && !cycle.active());
        System.out.println("Sluice source preference, buffer fallback and pause/resume checks passed");
    }
}
