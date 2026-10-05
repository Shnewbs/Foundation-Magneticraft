package com.foundations.magneticraft.integration;

import java.util.concurrent.atomic.AtomicInteger;

public final class GenerationCacheTest {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        var cache = new GenerationCache<Object, Integer>();
        var loads = new AtomicInteger();
        Object manager = new Object();
        check(cache.get(manager, 0, loads::incrementAndGet) == 1);
        for (int i = 0; i < 100000; i++) check(cache.get(manager, 0, loads::incrementAndGet) == 1);
        check(loads.get() == 1);
        check(cache.get(manager, 1, loads::incrementAndGet) == 2);
        check(cache.get(new Object(), 1, loads::incrementAndGet) == 3);
        cache.clear();
        check(cache.get(manager, 1, loads::incrementAndGet) == 4);
        boolean failed = false;
        try { cache.get(manager, 2, () -> { throw new IllegalStateException(); }); }
        catch (IllegalStateException expected) { failed = true; }
        check(failed && cache.get(manager, 1, loads::incrementAndGet) == 4);
        System.out.println("Recipe generation cache: 100000 hits, reload, script revision and failed-load checks passed");
    }
}
