package com.foundations.magneticraft.manual;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SluiceBoxBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty CENTER = BooleanProperty.create("center");
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 10);
    public static final BooleanProperty FLOWING = BooleanProperty.create("flowing");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public SluiceBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH)
            .setValue(CENTER, true).setValue(ACTIVE, false).setValue(FLOWING, false).setValue(FILL, 0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CENTER, ACTIVE, FLOWING, FILL);
    }
    public static BlockPos mainPos(BlockState state, BlockPos pos) {
        return state.getValue(CENTER) ? pos : pos.relative(state.getValue(FACING).getOpposite());
    }
    private static Direction partnerDirection(BlockState state) {
        return state.getValue(CENTER) ? state.getValue(FACING) : state.getValue(FACING).getOpposite();
    }
    private boolean isPartner(BlockState state, BlockState other) {
        return other.is(this) && state.getValue(CENTER) != other.getValue(CENTER)
            && state.getValue(FACING) == other.getValue(FACING);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos other = context.getClickedPos().relative(facing);
        Level level = context.getLevel();
        return level.hasChunkAt(other) && level.getWorldBorder().isWithinBounds(other)
            && level.getBlockState(other).canBeReplaced(context)
            ? defaultBlockState().setValue(FACING, facing) : null;
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide())
            level.setBlock(pos.relative(state.getValue(FACING)), state.setValue(CENTER, false), Block.UPDATE_ALL);
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState other,
            LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        if (direction == partnerDirection(state) && !isPartner(state, other)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, other, level, pos, otherPos);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild) {
            BlockPos otherPos = pos.relative(partnerDirection(state));
            BlockState other = level.getBlockState(otherPos);
            if (isPartner(state, other))
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(CENTER) ? new SluiceBoxBlockEntity(pos, state) : null;
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && state.getValue(CENTER) && type == FoundationsMagneticraft.SLUICE_BOX_ENTITY.get()
            ? (world, pos, blockState, entity) -> ((SluiceBoxBlockEntity) entity).tick() : null;
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, state.getValue(CENTER) ? 16 : 8, 16);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
    private void interact(Level level, BlockPos pos, BlockState state, Player player, ItemStack held) {
        BlockPos main = mainPos(state, pos);
        if (!level.isClientSide() && level.hasChunkAt(main)
                && level.getBlockEntity(main) instanceof SluiceBoxBlockEntity box) box.interact(player, held);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        interact(level, pos, state, player, held);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        interact(level, pos, state, player, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SluiceBoxBlockEntity box) box.dropContents();
        super.onRemove(state, level, pos, next, moved);
    }
}
