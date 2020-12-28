package xyz.bajtix.musicblock;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.Set;

public class MusicNoteBlock extends NoteBlock {
    public MusicNoteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        Set<BlockPos> tes =  worldIn.getChunk(pos).getTileEntitiesPos();

        tes.addAll(worldIn.getChunk(pos.add(16,0,0)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(-16,0,0)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(0,0,16)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(0,0,-16)).getTileEntitiesPos());

        tes.addAll(worldIn.getChunk(pos.add(-16,0,-16)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(16,0,16)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(16,0,-16)).getTileEntitiesPos());
        tes.addAll(worldIn.getChunk(pos.add(-16,0,16)).getTileEntitiesPos());

        net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(worldIn, pos, state, state.get(NOTE), state.get(INSTRUMENT));

        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) return false;
        state = state.with(NOTE, e.getVanillaNoteId()).with(INSTRUMENT, e.getInstrument());
        int i = state.get(NOTE);
        float f = (float)Math.pow(2.0D, (double)(i - 12) / 12.0D);

        for (BlockPos p : tes) {
            TileEntity te = worldIn.getTileEntity(p);

            if(te.getType() == TEList.MUSIC_BLOCK) {
                ((MusicBlockTileEntity)te).addNote(f,state.get(INSTRUMENT));

            }
        }

        worldIn.playSound((PlayerEntity)null, pos, state.get(INSTRUMENT).getSound(), SoundCategory.RECORDS, 3.0F, f);
        return true;
    }

    private void triggerNote(World worldIn, BlockPos pos) {
        if (worldIn.isAirBlock(pos.up())) {
            worldIn.addBlockEvent(pos, this, 0, 0);
        }

    }


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            int _new = net.minecraftforge.common.ForgeHooks.onNoteChange(worldIn, pos, state, state.get(NOTE), state.func_235896_a_(NOTE).get(NOTE));

            if(player.isCrouching())
                _new-=2;

            if (_new == -1 || _new < 0) {
                return ActionResultType.FAIL;
            }

            state = state.with(NOTE, _new);
            worldIn.setBlockState(pos, state, 3);
            this.triggerNote(worldIn, pos);
            player.addStat(Stats.TUNE_NOTEBLOCK);
            return ActionResultType.CONSUME;
        }
    }
}
