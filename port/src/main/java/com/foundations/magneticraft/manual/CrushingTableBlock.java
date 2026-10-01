package com.foundations.magneticraft.manual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CrushingTableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);
    public CrushingTableBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrushingTableBlockEntity(pos, state);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || hit.getDirection() != Direction.UP)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrushingTableBlockEntity table)
            table.interact(player, held);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrushingTableBlockEntity table)
            table.interact(player, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CrushingTableBlockEntity table)
            table.dropContents();
        super.onRemove(state, level, pos, next, moved);
    }
}
