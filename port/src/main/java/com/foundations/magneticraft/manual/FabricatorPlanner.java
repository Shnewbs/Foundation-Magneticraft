package com.foundations.magneticraft.manual;

import java.util.Arrays;

/** Bounded allocation of up to nine ingredients across shared source-slot capacities. */
public final class FabricatorPlanner {
    private FabricatorPlanner() {}
    public static int[] allocate(int[] available, boolean[][] eligible) {
        if (eligible.length > 9) throw new IllegalArgumentException("At most nine ingredients");
        for (int n : available) if (n < 0) throw new IllegalArgumentException("Negative capacity");
        for (boolean[] row : eligible) if (row.length != available.length) throw new IllegalArgumentException("Matrix dimensions");
        Integer[] order = new Integer[eligible.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Integer.compare(options(eligible[a], available), options(eligible[b], available)));
        int[] result = new int[eligible.length];
        return search(0, order, available.clone(), eligible, result, new int[]{4096}) ? result : null;
    }
    private static int options(boolean[] row, int[] counts) {
        int n = 0;
        for (int i = 0; i < row.length; i++) if (row[i] && counts[i] > 0) n++;
        return n;
    }
    private static boolean search(int depth, Integer[] order, int[] counts, boolean[][] eligible, int[] result, int[] budget) {
        if (depth == order.length) return true;
        if (--budget[0] < 0) return false;
        int input = order[depth];
        for (int source = 0; source < counts.length; source++) {
            if (counts[source] == 0 || !eligible[input][source]) continue;
            counts[source]--;
            result[input] = source;
            if (search(depth + 1, order, counts, eligible, result, budget)) return true;
            counts[source]++;
            if (budget[0] < 0) return false;
        }
        return false;
    }
}
