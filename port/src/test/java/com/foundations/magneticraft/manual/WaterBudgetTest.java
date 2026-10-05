package com.foundations.magneticraft.manual;

public final class WaterBudgetTest {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        check(WaterBudget.insertion(0, 10000, false) == 1000);
        check(WaterBudget.insertion(600, 600, false) == 400);
        check(WaterBudget.insertion(1000, 100, false) == 0);
        check(WaterBudget.insertion(0, 1000, true) == 0);
        check(WaterBudget.insertion(300, 0, false) == 0);
        check(WaterBudget.restored(-20) == 0 && WaterBudget.restored(2000) == 1000);
        boolean rejected = false;
        try { WaterBudget.insertion(1001, 1, false); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected);
        System.out.println("Sluice water capacity, partial fills, busy rejection and restore bounds checks passed");
    }
}
