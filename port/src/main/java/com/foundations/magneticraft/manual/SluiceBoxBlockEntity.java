package com.foundations.magneticraft.manual;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SluiceBoxBlockEntity extends BlockEntity {
    private int water;
    private boolean passiveCycle;
    private boolean sourceWater;
    private boolean sourceChecked;
    private final SluiceAutomation automation = new SluiceAutomation(this);
    private ItemStack stored = ItemStack.EMPTY;
    private final SluiceCycle cycle = new SluiceCycle();
    public SluiceBoxBlockEntity(BlockPos pos, BlockState state) {
        super(FoundationsMagneticraft.SLUICE_BOX_ENTITY.get(), pos, state);
    }
    public void interact(Player player, ItemStack held) {
        if (level == null || level.isClientSide()) return;
        if ((!cycle.active() || (passiveCycle && !sourceWater)) && SluiceWaterContainer.consume(player)) {
            if (cycle.active()) { passiveCycle = false; updateActive(true); changed(); }
            else activate();
        } else if (!cycle.active() && held.isEmpty()) {
            ItemStack remainder = stored.copy();
            player.getInventory().add(remainder);
            stored = remainder.isEmpty() ? ItemStack.EMPTY : remainder;
            changed();
        } else if (!cycle.active() && SluiceRecipes.find(level, held) != null
                && (stored.isEmpty() || ItemStack.isSameItemSameComponents(stored, held))) {
            int amount = SluiceCycle.insertionCount(stored.getCount(), held.getCount());
            if (amount > 0) {
                if (stored.isEmpty()) stored = held.copyWithCount(amount);
                else stored.grow(amount);
                if (!player.getAbilities().instabuild) held.shrink(amount);
                changed();
            }
        }
        player.displayClientMessage(Component.translatable("message.magneticraft.sluice_contents",
            stored.getCount(), cycle.remaining(), SluiceCycle.DURATION), true);
    }
    private boolean hasNext() {
        BlockPos next = worldPosition.relative(getBlockState().getValue(SluiceBoxBlock.FACING), 2).below();
        return level.hasChunkAt(next) && level.getBlockEntity(next) instanceof SluiceBoxBlockEntity;
    }
    public ItemStack input() { return stored.copy(); }
    public int waterAmount() { return water; }
    public int remainingTicks() { return cycle.remaining(); }
    public boolean active() { return cycle.active(); }
    public SluiceAutomation automation() { return automation; }
    public void flushAutomation() { changed(); }
    public record AutomationState(ItemStack input, int water) {}
    public AutomationState automationState() { return new AutomationState(stored.copy(), water); }
    public void restoreAutomation(AutomationState state) { stored = state.input().copy(); water = state.water(); }
    public void setAutomation(ItemStack input, int water) { stored = input; this.water = water; }
    public ItemStack insertInput(ItemStack incoming, boolean simulate) {
        if (incoming.isEmpty() || cycle.active() || SluiceRecipes.find(level, incoming) == null
            || (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, incoming))) return incoming.copy();
        int accepted = Math.min(SluiceCycle.insertionCount(stored.getCount(), incoming.getCount()), Math.max(0, incoming.getMaxStackSize() - stored.getCount()));
        if (!simulate && accepted > 0) {
            if (stored.isEmpty()) stored = incoming.copyWithCount(accepted); else stored.grow(accepted);
            changed();
        }
        return incoming.copyWithCount(incoming.getCount() - accepted);
    }
    public ItemStack extractInput(int amount, boolean simulate) {
        if (amount <= 0 || cycle.active() || stored.isEmpty()) return ItemStack.EMPTY;
        int accepted = Math.min(amount, stored.getCount());
        ItemStack result = stored.copyWithCount(accepted);
        if (!simulate) { stored.shrink(accepted); changed(); }
        return result;
    }
    public int insertWater(int amount, boolean simulate) {
        int accepted = WaterBudget.insertion(water, amount, cycle.active());
        if (!simulate && accepted > 0) { water += accepted; changed(); }
        return accepted;
    }
    private boolean hasSourceWater() {
        var facing = getBlockState().getValue(SluiceBoxBlock.FACING);
        // Intake back and sides at the same height. The downstream outlet is not an intake.
        for (var direction : new net.minecraft.core.Direction[] {
                facing.getOpposite(), facing.getClockWise(), facing.getCounterClockWise() }) {
            BlockPos neighbor = worldPosition.relative(direction);
            if (!level.hasChunkAt(neighbor)) continue;
            var fluid = level.getFluidState(neighbor);
            if (fluid.is(net.minecraft.tags.FluidTags.WATER) && fluid.isSource()) return true;
        }
        return false;
    }
    private boolean activate() { return activate(false); }
    private boolean activate(boolean passive) {
        if (level == null || level.isClientSide() || !cycle.start()) return false;
        passiveCycle = passive;
        updateActive(true);
        level.playSound(null, worldPosition, (hasNext() ? FoundationsMagneticraft.WATER_FLOW.get() : FoundationsMagneticraft.WATER_FLOW_END.get()), SoundSource.BLOCKS, 0.8F, 1.0F);
        changed();
        return true;
    }
    private void updateActive(boolean active) {
        BlockState state = getBlockState();
        boolean flowing = sourceWater || (active && !passiveCycle);
        BlockState updated = state.setValue(SluiceBoxBlock.ACTIVE, active).setValue(SluiceBoxBlock.FLOWING, flowing);
        if (updated != state) level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        BlockPos other = worldPosition.relative(state.getValue(SluiceBoxBlock.FACING));
        if (level.hasChunkAt(other)) {
            BlockState outlet = level.getBlockState(other);
            if (outlet.is(state.getBlock()) && !outlet.getValue(SluiceBoxBlock.CENTER)
                    && outlet.getValue(SluiceBoxBlock.FACING) == state.getValue(SluiceBoxBlock.FACING)) {
                BlockState next = outlet.setValue(SluiceBoxBlock.ACTIVE, active).setValue(SluiceBoxBlock.FLOWING, flowing);
                if (next != outlet) level.setBlock(other, next, Block.UPDATE_CLIENTS);
            }
        }
    }
    public void tick() {
        if (level == null || level.isClientSide()) return;
        // Stagger checks across boxes: three loaded neighbor reads at most once per second.
        if (!sourceChecked || Math.floorMod(level.getGameTime() + worldPosition.asLong(), 20) == 0) {
            sourceWater = hasSourceWater();
            sourceChecked = true;
            updateActive(cycle.active());
        }
        if (!cycle.active() && !stored.isEmpty()) {
            SluiceWaterSupply.Start supply = SluiceWaterSupply.start(
                SluiceRecipes.find(level, stored) != null, sourceWater, water);
            if (supply != SluiceWaterSupply.Start.NONE) {
                if (supply == SluiceWaterSupply.Start.BUFFER) water = 0;
                activate(supply == SluiceWaterSupply.Start.SOURCE);
            }
        }
        if (!cycle.active() || !SluiceWaterSupply.canAdvance(passiveCycle, sourceWater)) return;
        SluiceCycle.Tick tick = cycle.tick();
        setChanged(); // Persist exact remaining work even when the chunk unloads mid-cycle.
        if (tick.activateNext()) {
            BlockPos next = worldPosition.relative(getBlockState().getValue(SluiceBoxBlock.FACING), 2).below();
            // Never force-load a downstream chunk. Busy boxes cannot restart or double-spend their input.
            if (level.hasChunkAt(next) && level.getBlockEntity(next) instanceof SluiceBoxBlockEntity box)
                box.activate();
        }
        if (cycle.remaining() % 8 == 0) updateFill();
        if (tick.complete()) {
            SluiceRecipes.Recipe recipe = SluiceRecipes.find(level, stored);
            if (recipe != null) {
                ItemStack input = stored;
                stored = ItemStack.EMPTY; // Consume before spawning outputs.
                BlockPos outlet = worldPosition.relative(getBlockState().getValue(SluiceBoxBlock.FACING));
                for (int item = 0; item < input.getCount(); item++)
                    for (SluiceRecipes.Output output : recipe.outputs())
                        if (level.random.nextFloat() < output.chance())
                            Block.popResource(level, outlet, output.stack().copy());
            } // A removed recipe leaves its input recoverable.
            passiveCycle = false;
            updateActive(false);
            changed();
        }
    }
    private void updateFill() {
        int fill = cycle.active()
            ? (stored.getCount() * cycle.remaining() + SluiceCycle.DURATION - 1) / SluiceCycle.DURATION
            : stored.getCount();
        BlockState state = getBlockState();
        if (state.getValue(SluiceBoxBlock.FILL) != fill)
            level.setBlock(worldPosition, state.setValue(SluiceBoxBlock.FILL, fill), Block.UPDATE_CLIENTS);
    }
    private void changed() {
        setChanged();
        updateFill();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    public void dropContents() {
        if (level == null || level.isClientSide() || stored.isEmpty()) return;
        ItemStack dropped = stored;
        stored = ItemStack.EMPTY;
        cycle.restore(0, 0);
        setChanged();
        Block.popResource(level, worldPosition, dropped);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!stored.isEmpty()) tag.put("stored", stored.save(registries));
        tag.putBoolean("passive_cycle", passiveCycle);
        tag.putInt("water", water);
        tag.putInt("remaining", cycle.remaining());
        tag.putInt("chain_delay", cycle.chainDelay());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = ItemStack.parseOptional(registries, tag.getCompound("stored"));
        if (stored.getCount() > SluiceCycle.CAPACITY) stored.setCount(SluiceCycle.CAPACITY);
        passiveCycle = tag.getBoolean("passive_cycle");
        sourceChecked = false;
        water = WaterBudget.restored(tag.getInt("water"));
        cycle.restore(tag.getInt("remaining"), tag.getInt("chain_delay"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
