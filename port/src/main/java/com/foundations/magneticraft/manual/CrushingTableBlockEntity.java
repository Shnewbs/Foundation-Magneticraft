package com.foundations.magneticraft.manual;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CrushingTableBlockEntity extends BlockEntity {
    private ItemStack stored = ItemStack.EMPTY;
    private ItemStack lastInput = ItemStack.EMPTY;
    private final CrushingProgress progress = new CrushingProgress();
    public CrushingTableBlockEntity(BlockPos pos, BlockState state) {
        super(FoundationsMagneticraft.CRUSHING_TABLE_ENTITY.get(), pos, state);
    }
    public ItemStack input() { return stored.copy(); }
    public int damage() { return progress.damage(); }
    private record Hammer(int miningLevel, int speed) {}
    private static Hammer hammer(ItemStack held) {
        return switch (BuiltInRegistries.ITEM.getKey(held.getItem()).toString()) {
            case "magneticraft:stone_hammer" -> new Hammer(1, 8);
            case "magneticraft:iron_hammer" -> new Hammer(2, 10);
            case "magneticraft:steel_hammer" -> new Hammer(4, 15);
            default -> null;
        };
    }
    public void interact(Player player, ItemStack held) {
        if (level == null || level.isClientSide()) return;
        Hammer hammer = hammer(held);
        if (stored.isEmpty()) {
            if (hammer != null && !lastInput.isEmpty()) {
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack candidate = player.getInventory().getItem(slot);
                    if (ItemStack.isSameItemSameComponents(candidate, lastInput)
                            && CrushingRecipes.find(level, candidate) != null) {
                        insert(player, candidate);
                        break;
                    }
                }
            } else if (!held.isEmpty() && CrushingRecipes.find(level, held) != null) {
                insert(player, held);
            }
        } else {
            CrushingRecipes.Recipe recipe = CrushingRecipes.find(level, stored);
            if (hammer != null && recipe != null) {
                int requiredTier = recipe.miningLevel();
                if (stored.getItem() instanceof BlockItem blockItem) {
                    BlockState input = blockItem.getBlock().defaultBlockState();
                    if (input.is(BlockTags.NEEDS_DIAMOND_TOOL)) requiredTier = Math.max(requiredTier, 3);
                    else if (input.is(BlockTags.NEEDS_IRON_TOOL)) requiredTier = Math.max(requiredTier, 2);
                    else if (input.is(BlockTags.NEEDS_STONE_TOOL)) requiredTier = Math.max(requiredTier, 1);
                }
                if (hammer.miningLevel() < requiredTier) {
                    player.displayClientMessage(Component.translatable("message.magneticraft.hammer_tier"), true);
                    return;
                }
                if (!player.getAbilities().instabuild)
                    held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                ItemStack particleInput = stored.copy();
                if (stored.is(net.minecraft.world.item.Items.BLAZE_ROD) && ProcessingConfig.BLAZE_FIRE.get())
                    player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), 100));
                boolean finished = progress.hit(hammer.speed());
                if (finished) {
                    lastInput = stored.copyWithCount(1);
                    stored = recipe.output().copy();
                }
                level.playSound(null, worldPosition, finished ? FoundationsMagneticraft.CRUSHING_FINAL.get() : FoundationsMagneticraft.CRUSHING_HIT.get(),
                        SoundSource.BLOCKS, 0.7F, 0.8F);
                if (level instanceof net.minecraft.server.level.ServerLevel server) {
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.95, worldPosition.getZ() + 0.5, 6, 0.15, 0.1, 0.15, 0.05);
                }
                changed();
            } else {
                // Inventory.add mutates the copy, including partial transfers. Keep only the remainder.
                ItemStack remainder = stored.copy();
                player.getInventory().add(remainder);
                if (remainder.getCount() != stored.getCount()) progress.reset();
                stored = remainder.isEmpty() ? ItemStack.EMPTY : remainder;
                changed();
            }
        }
        player.displayClientMessage(stored.isEmpty()
            ? Component.translatable("message.magneticraft.table_empty")
            : Component.translatable("message.magneticraft.table_contents",
                stored.getCount(), stored.getHoverName(), progress.damage(), CrushingProgress.REQUIRED), true);
    }
    private void insert(Player player, ItemStack candidate) {
        stored = candidate.copyWithCount(1);
        if (!player.getAbilities().instabuild) candidate.shrink(1);
        progress.reset();
        changed();
    }
    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    public void dropContents() {
        if (level == null || level.isClientSide() || stored.isEmpty()) return;
        ItemStack dropped = stored;
        stored = ItemStack.EMPTY;
        progress.reset();
        setChanged();
        Block.popResource(level, worldPosition, dropped);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!stored.isEmpty()) tag.put("stored", stored.save(registries));
        if (!lastInput.isEmpty()) tag.put("last_input", lastInput.save(registries));
        tag.putInt("damage", progress.damage());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = ItemStack.parseOptional(registries, tag.getCompound("stored"));
        lastInput = ItemStack.parseOptional(registries, tag.getCompound("last_input"));
        progress.restore(stored.isEmpty() ? 0 : tag.getInt("damage"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
