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
    private final SluiceAutomation automation = new SluiceAutomation(this);
    private ItemStack stored = ItemStack.EMPTY;
    private final SluiceCycle cycle = new SluiceCycle();
    public SluiceBoxBlockEntity(BlockPos pos, BlockState state) {
        super(FoundationsMagneticraft.SLUICE_BOX_ENTITY.get(), pos, state);
    }
    public void interact(Player player, ItemStack held) {
        if (level == null || level.isClientSide()) return;
        if (!cycle.active() && SluiceWaterContainer.consume(player)) {
            activate();
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
        player.sendOverlayMessage(Component.translatable("message.magneticraft.sluice_contents",
            stored.getCount(), cycle.remaining(), SluiceCycle.DURATION));
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
    private boolean activate() {
        if (level == null || level.isClientSide() || !cycle.start()) return false;
        updateActive(true);
        level.playSound(null, worldPosition, (hasNext() ? FoundationsMagneticraft.WATER_FLOW.get() : FoundationsMagneticraft.WATER_FLOW_END.get()), SoundSource.BLOCKS, 0.8F, 1.0F);
        changed();
        return true;
    }
    private void updateActive(boolean active) {
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(SluiceBoxBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        BlockPos other = worldPosition.relative(state.getValue(SluiceBoxBlock.FACING));
        if (level.hasChunkAt(other)) {
            BlockState outlet = level.getBlockState(other);
            if (outlet.is(state.getBlock()) && !outlet.getValue(SluiceBoxBlock.CENTER)
                    && outlet.getValue(SluiceBoxBlock.FACING) == state.getValue(SluiceBoxBlock.FACING))
                level.setBlock(other, outlet.setValue(SluiceBoxBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }
    public void tick() {
        if (level == null || level.isClientSide()) return;
        if (!cycle.active() && water == WaterBudget.CAPACITY && SluiceRecipes.find(level, stored) != null) { water = 0; activate(); }
        if (!cycle.active()) return;
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
                        if (level.getRandom().nextFloat() < output.chance())
                            Block.popResource(level, outlet, output.stack().copy());
            } // A removed recipe leaves its input recoverable.
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
    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.store("stored", ItemStack.OPTIONAL_CODEC, stored);
        output.putInt("water", water);
        output.putInt("remaining", cycle.remaining());
        output.putInt("chain_delay", cycle.chainDelay());
    }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        stored = input.read("stored", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (stored.getCount() > SluiceCycle.CAPACITY) stored.setCount(SluiceCycle.CAPACITY);
        water = WaterBudget.restored(input.getIntOr("water", 0));
        cycle.restore(input.getIntOr("remaining", 0), input.getIntOr("chain_delay", 0));
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        dropContents();
        super.preRemoveSideEffects(pos, state);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
