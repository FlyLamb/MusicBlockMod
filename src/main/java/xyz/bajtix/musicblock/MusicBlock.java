package xyz.bajtix.musicblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class MusicBlock extends Block {
    public MusicBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState().with(POWERED,Boolean.valueOf(false)).with(RECORDING,Boolean.valueOf(false)));
    }

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty RECORDING = BlockStateProperties.ENABLED;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(POWERED,Boolean.valueOf(false)).with(RECORDING,Boolean.valueOf(false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(RECORDING);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = worldIn.isBlockPowered(pos);
        if (flag != state.get(POWERED)) {
            if (flag) {
                ((MusicBlockTileEntity)worldIn.getTileEntity(pos)).play(true);
            }

            worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(flag)), 3);
        }

    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {

        ListNBT blockMusic = ((MusicBlockTileEntity)worldIn.getTileEntity(pos)).getSongDataNBT();
        String author = ((MusicBlockTileEntity)worldIn.getTileEntity(pos)).author;
        String name = ((MusicBlockTileEntity)worldIn.getTileEntity(pos)).songName;
        if(blockMusic.size() > 0) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("author", author);
            nbt.put("music", blockMusic);
            ItemStack stack = new ItemStack(ItemList.musicBlock, 1, nbt);
            stack.setTag(nbt);
            stack.setDisplayName(new StringTextComponent(name));
            ItemEntity s = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            worldIn.addEntity(s);
        }
        else if(!player.isCreative())
        {
            ItemStack stack = new ItemStack(ItemList.musicBlock, 1);
            ItemEntity s = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            worldIn.addEntity(s);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) return ActionResultType.SUCCESS;
        ((MusicBlockTileEntity)worldIn.getTileEntity(pos)).record(player,null);
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TEList.MUSIC_BLOCK.create();
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return true;
    }
}
