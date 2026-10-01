package com.foundations.magneticraft.manual;

/** Source ModuleCrushingTable threshold; independent of Minecraft for regression checks. */
public final class CrushingProgress {
    public static final int REQUIRED = 40;
    private int damage;
    public int damage() { return damage; }
    public void reset() { damage = 0; }
    public void restore(int value) { damage = Math.max(0, Math.min(REQUIRED - 1, value)); }
    public boolean hit(int speed) {
        if (speed <= 0 || speed > REQUIRED) throw new IllegalArgumentException("Invalid hammer speed");
        damage += speed;
        if (damage < REQUIRED) return false;
        reset();
        return true;
    }
}
