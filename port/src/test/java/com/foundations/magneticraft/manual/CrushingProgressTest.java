package com.foundations.magneticraft.manual;
public final class CrushingProgressTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        for (int[] example : new int[][] {{8,5}, {10,4}, {15,3}}) {
            CrushingProgress progress = new CrushingProgress();
            for (int hit = 1; hit < example[1]; hit++)
                require(!progress.hit(example[0]), "Output produced before original threshold");
            require(progress.hit(example[0]), "Missing output at threshold");
            require(progress.damage() == 0, "Completed work carries over to next input");
        }
        CrushingProgress progress = new CrushingProgress();
        progress.hit(8);
        progress.reset();
        require(progress.damage() == 0, "Removed input retains progress");
        progress.restore(24);
        require(!progress.hit(8) && progress.hit(8), "Partial work did not survive save/load");
        progress.restore(-10);
        require(progress.damage() == 0, "Negative persisted progress");
        progress.restore(1000);
        require(progress.damage() == 39, "Invalid persisted progress");
        try { progress.hit(0); throw new AssertionError("Invalid speed accepted"); }
        catch (IllegalArgumentException expected) {}
        System.out.println("Crushing threshold, reset, restore, and validation checks passed");
    }
}
