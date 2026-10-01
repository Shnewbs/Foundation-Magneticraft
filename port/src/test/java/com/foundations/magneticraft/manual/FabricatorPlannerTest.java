package com.foundations.magneticraft.manual;

import java.util.Arrays;

public final class FabricatorPlannerTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        boolean[][] repeated = {{true}, {true}};
        check(FabricatorPlanner.allocate(new int[]{1}, repeated) == null);
        int[] counts = {2};
        check(Arrays.equals(FabricatorPlanner.allocate(counts, repeated), new int[]{0, 0}));
        check(counts[0] == 2);
        check(Arrays.equals(FabricatorPlanner.allocate(new int[]{1, 1}, new boolean[][]{{true, true}, {true, false}}), new int[]{1, 0}));
        check(FabricatorPlanner.allocate(new int[]{1}, new boolean[][]{{false}}) == null);
        check(FabricatorPlanner.allocate(new int[0], new boolean[][]{new boolean[0]}) == null);
        check(FabricatorPlanner.allocate(new int[0], new boolean[0][]).length == 0);
        boolean[][] nine = new boolean[9][1];
        for (boolean[] row : nine) row[0] = true;
        check(FabricatorPlanner.allocate(new int[]{8}, nine) == null);
        check(FabricatorPlanner.allocate(new int[]{9}, nine) != null);
        boolean rejected = false;
        try { FabricatorPlanner.allocate(new int[]{-1}, new boolean[0][]); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected);
        System.out.println("Fabricator capacity, overlap, empty grid and validation checks passed");
    }
}
