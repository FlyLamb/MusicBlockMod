package xyz.bajtix.musicblock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MusicBlockTileEntity extends TileEntity implements ITickableTileEntity {
    public MusicBlockTileEntity() {
        super(TEList.MUSIC_BLOCK);

        if(recorded == null)
        recorded = new HashMap<Integer, ArrayList<Note>>();
    }

    class Note implements INBTSerializable {
        public float frequency;
        public NoteBlockInstrument instrument;

        public Note(float frequency, NoteBlockInstrument instrument) {
            this.frequency = frequency;
            this.instrument = instrument;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("f",frequency);
            nbt.putString("i",instrument.name());
            return nbt;
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            CompoundNBT bnbt = (CompoundNBT)nbt;
            frequency = bnbt.getFloat("f");
            String iname = bnbt.getString("i");
            instrument = NoteBlockInstrument.valueOf(iname);
        }
    }

    private Map<Integer, ArrayList<Note>> recorded;
    private int tick = 0;
    private PlayerEntity lastPlayerRecord;
    private int state = 2; // Play = 0; Record = 1; Idle = 2

    public String author;
    public String songName = new TranslationTextComponent("msg.unnamed").getString();

    public void addNote(float frequency, NoteBlockInstrument instrument)
    {
        if(state == 1){
            if(!recorded.containsKey(tick)){
                ArrayList<Note> notes = new ArrayList<>();
                notes.add(new Note(frequency,instrument));
                recorded.put(tick, notes);
                return;
            }
            else{
                recorded.get(tick).add(new Note(frequency,instrument));
            }

        }
    }


    @Override
    public void tick() {
        if(state == 2) return;
        tick++;

        if(state == 0)
        {
            if(recorded.containsKey(tick)) {
                for(Note n : recorded.get(tick)) {
                    world.playSound((PlayerEntity) null, pos, n.instrument.getSound(), SoundCategory.RECORDS, 3f, n.frequency);
                }
            }
        }

        if(tick > 6000)
        {
            if(state == 0)
            {
                state = 2;
                tick = 0;
            }
            else if(state == 1)
            {
                record(lastPlayerRecord);
            }
        }
    }

    public void play()
    {
        for(PlayerEntity p : world.getPlayers())
        {
            if(p.getDistanceSq(pos.getX(),pos.getY(),pos.getZ()) < 80)
                p.sendMessage(new StringTextComponent("Now playing: '" + songName + "' by " + author),null);
        }
        if(state == 0 || state == 3) {
            state = 2;
            tick = 0;
        }
        else
        {
            state = 0;
            tick = 0;
        }
    }

    public void record(PlayerEntity playerEntity)
    {

        lastPlayerRecord = playerEntity;
        if(recorded.size() > 0 && state != 3 && state != 1)
        {
            state = 3;
            playerEntity.sendMessage(new TranslationTextComponent("msg.overwrite"),null);
            return;
        }

        if(state == 1)
        {
            playerEntity.sendMessage(new TranslationTextComponent("msg.recsave"),null);
            state = 2;
            world.setBlockState(pos,world.getBlockState(pos).with(MusicBlock.RECORDING,false));
            author = playerEntity.getDisplayName().getString();
            markDirty();
        }
        else {
            playerEntity.sendMessage(new TranslationTextComponent("msg.recstart"), null);
            recorded.clear();
            state = 1;
            tick = 0;

            world.setBlockState(pos, world.getBlockState(pos).with(MusicBlock.RECORDING, true));
        }
    }

    public ListNBT writeSongData()
    {
        ListNBT musicData = new ListNBT();

        for(Map.Entry k : recorded.entrySet())
        {
            CompoundNBT mtickData = new CompoundNBT();


            int tick = (Integer)k.getKey();
            mtickData.putInt("tick",tick);

            ArrayList<Note> notes = (ArrayList<Note>)k.getValue();
            ListNBT notesNbt = new ListNBT();
            for(Note n : notes)
            {
                notesNbt.add(n.serializeNBT());
            }
            mtickData.put("notes",notesNbt);
            musicData.add(mtickData);

        }

        return musicData;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("noteData", writeSongData());
        compound.putString("author",author);
        compound.putString("name",songName);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state,nbt);
        this.state = 2;
        ListNBT musicData = (ListNBT)nbt.get("noteData");
        HashMap<Integer,ArrayList<Note>> r = new HashMap<>();
        System.out.println("Loading note recording:");

        if(musicData == null || musicData.size() < 1) return;
        for(INBT k : musicData)
        {
            int tick = ((CompoundNBT)k).getInt("tick");
            ListNBT notesNbt = (ListNBT)((CompoundNBT)k).get("notes");
            ArrayList<Note> notes = new ArrayList<>();
            if(notesNbt.size() < 1) continue;
            for(INBT n : notesNbt)
            {
                Note note = new Note(0,null);
                note.deserializeNBT(n);
                notes.add(note);
            }

            r.put(tick,notes);
        }
        tick = 0;
        author = nbt.getString("author");
        songName = nbt.getString("name");
        this.recorded = r;
        System.out.println("Finished loading notes, final size:" + recorded.size());

    }

    public void readAllNBT(ListNBT musicData, String author, String songName)
    {
        HashMap<Integer,ArrayList<Note>> r = new HashMap<>();
        System.out.println("Loading note recording:");
        this.author = author;
        if(musicData == null || musicData.size() < 1) return;
        for(INBT k : musicData)
        {
            int tick = ((CompoundNBT)k).getInt("tick");
            ListNBT notesNbt = (ListNBT)((CompoundNBT)k).get("notes");
            ArrayList<Note> notes = new ArrayList<>();
            if(notesNbt.size() < 1) continue;
            for(INBT n : notesNbt)
            {
                Note note = new Note(0,null);
                note.deserializeNBT(n);
                notes.add(note);
            }

            r.put(tick,notes);
        }
        tick = 0;
        this.recorded = r;
        this.songName = songName;
    }
}
