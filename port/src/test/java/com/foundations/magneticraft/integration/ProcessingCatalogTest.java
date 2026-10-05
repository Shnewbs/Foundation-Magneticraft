package com.foundations.magneticraft.integration;

import java.util.List;

public final class ProcessingCatalogTest {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    private static void rejects(Runnable action) { boolean rejected = false; try { action.run(); } catch (RuntimeException expected) { rejected = true; } check(rejected); }
    public static void main(String[] args) {
        var entry = new ProcessingCatalog.Entry("sluice", "magneticraft:sand", "#c:sands", 0, List.of(new ProcessingCatalog.Output("minecraft:gold_nugget", 1, 0.005)));
        check(ProcessingCatalog.decode(ProcessingCatalog.encode(List.of(entry))).equals(List.of(entry)));
        rejects(() -> new ProcessingCatalog.Output("minecraft:stone", 65, 1));
        rejects(() -> new ProcessingCatalog.Output("minecraft:stone", 1, Double.NaN));
        rejects(() -> new ProcessingCatalog.Entry("unknown", "a:b", "a:b", 0, entry.outputs()));
        rejects(() -> new ProcessingCatalog.Entry("sluice", "a:b", "INVALID", 0, entry.outputs()));
        rejects(() -> ProcessingCatalog.decode(" ".repeat(ProcessingCatalog.MAX_JSON + 1)));
        rejects(() -> ProcessingCatalog.decode("[{}]"));
        rejects(() -> ProcessingCatalog.encode(java.util.Collections.nCopies(4097, entry)));
        final int[] changes = {0};
        ProcessingCatalog.setClientListener(recipes -> changes[0]++);
        ProcessingCatalog.acceptClient(List.of(entry));
        ProcessingCatalog.acceptClient(List.of());
        check(changes[0] == 3 && ProcessingCatalog.client().isEmpty());
        ProcessingCatalog.setClientListener(null);
        System.out.println("Processing catalog round-trip, malformed input, bounds and disconnect reset checks passed");
    }
}
