package xyz.bajtix.musicblock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MusicBlockTileEntity extends TileEntity implements ITickableTileEntity {
    public MusicBlockTileEntity() {
        super(TEList.MUSIC_BLOCK);

        if(recorded == null)
        recorded = new HashMap<Integer, ArrayList<Note>>();
    }

    public class Note implements INBTSerializable {
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


    /**
     * Song volume (pretty sure it can be max. 3)
     */
    public float volume = 3;
    /**
     * Song author
     */
    public String author = "anonymous";
    /**
     * Song name (defaults to the key specified in lang file as <code>msg.unnamed</code>)
     */
    public String songName = new TranslationTextComponent("msg.unnamed").getString();

    /**
     * Adds a note to the recording, with current tick
     * @param frequency The pitch of the sound
     * @param instrument Note Block Instrument to play this note
     */
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

    /**
     * Force adds a note, even if it is not recording, to the given tick
     * @param frequency The pitch of the sound
     * @param instrument Note Block Instrument to play this note
     * @param tick The tick of this note
     */
    public void addNote(float frequency, NoteBlockInstrument instrument, int tick)
    {
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

    /**
     * Clears the recorded song data
     */
    public void clearRecording()
    {
        recorded.clear();
    }

    /**
     * Get the full recording
     * @return the recording, <code>Map<[Integer]Tick, [Note]Note></code>
     */
    public Map<Integer, ArrayList<Note>> getRecorded()
    {
        return  recorded;
    }

    /**
     * Set the recorded Map. Please do not use this method, unless you know what you are doing
     * @param recorded The <code>Map< tick[Integer],note[Note] ></code>
     */
    public void setRecorded(Map<Integer, ArrayList<Note>> recorded) {
        this.recorded = recorded;
    }

    /**
     * The amount of entries in the recorded music
     * @return the amount of entries in the dictionary
     */
    public int getNoteAmount()
    {
        return recorded.size();
    }

    /**
     * Get notes at tick
     * @param tick Tick to get the notes at
     * @return The note array list
     */
    public ArrayList<Note> getNotes(int tick)
    {
        if(recorded.containsKey(tick))
            return  recorded.get(tick);
        else
            return null;
    }

    /**
     * Overwrite notes at a given tick
     * @param notes The note array to be put
     * @param tick The tick in which to overwrite
     */
    public void setNotes(ArrayList<Note> notes, int tick)
    {
        if(tick > 6000 ) return;
        recorded.put(tick,notes);
    }

    /**
     * Get the current state
     * @return the state variable
     */
    public int getState()
    {
        return  state;
    }

    /**
     * Get the current tick
     * @return the tick variable
     */
    public int getTick()
    {
        return  tick;
    }

    /**
     * Sets the current tick
     * @param s the tick value
     */
    public void setTick(int s)
    {
        tick = s;
    }


    //TODO: Cleanup this function
    @Override
    public void tick() {
        if(state == 2) return;

        if(state == 1) {
            BlockPos chunkPos = world.getChunk(pos).getPos().asBlockPos();
            for (int i = 0; i < 48; i++) {
                world.addParticle(ParticleTypes.FLAME, chunkPos.getX() + i - 16, pos.getY() + .5d, chunkPos.getZ() - 16, 0, 0.5d, 0);
            }
            for (int i = 0; i < 48; i++) {
                world.addParticle(ParticleTypes.FLAME, chunkPos.getX() + i - 16, pos.getY() + .5d, chunkPos.getZ() + 32, 0, 0.5d, 0);
            }
            for (int i = 0; i < 48; i++) {
                world.addParticle(ParticleTypes.FLAME, chunkPos.getX() - 16, pos.getY() + .5d, chunkPos.getZ() + i - 16, 0, 0.5d, 0);
            }
            for (int i = 0; i < 48; i++) {
                world.addParticle(ParticleTypes.FLAME, chunkPos.getX() + 32, pos.getY() + .5d, chunkPos.getZ() + i - 16, 0, 0.5d, 0);
            }
        }

        if(state == 0)
        {

            if(recorded.containsKey(tick)) {
                for(Note n : recorded.get(tick)) {
                    world.playSound((PlayerEntity) null, pos, n.instrument.getSound(), SoundCategory.RECORDS, volume, n.frequency);
                }
            }
            /*if(tick % 60 == 0)
                world.notifyBlockUpdate(pos,world.getBlockState(pos),world.getBlockState(pos),2);*/

        }
        tick++;

        if(tick > 6000)
        {
            if(state == 0)
            {
                state = 2;
                tick = 0;
            }
            else if(state == 1)
            {
                record(lastPlayerRecord,null);
            }
        }
    }


    /**
     * Toggles playing the recorded audio
     * @param announce Should it write "Now playing.." message to nearby players?
     */
    public void play(boolean announce)
    {
        if(state == 0 || state == 3) {
            state = 2;
            tick = 0;
        }
        else
        {
            state = 0;
            tick = 0;
            if(announce) {
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 190)
                        p.sendMessage(new StringTextComponent(new TranslationTextComponent("msg.nowplaying").getString() + " '" + songName + "' " + new TranslationTextComponent("msg.by").getString() + " " + author), p.getUniqueID());
                }
            }
        }
    }

    /**
     *
     * @param playerEntity The player who toggled recording
     * @param author If player entity is null, this will be the song's author
     * @return The final state of the musicbox
     */
    public int record(@Nullable PlayerEntity playerEntity,@Nullable String author)
    {

        if(playerEntity != null)
            lastPlayerRecord = playerEntity;
        if(recorded.size() > 0 && state != 3 && state != 1)
        {
            state = 3;
            if(playerEntity != null)
                lastPlayerRecord = playerEntity;

            playerEntity.sendMessage(new TranslationTextComponent("msg.overwrite"),playerEntity.getUniqueID());
            return state;
        }

        if(state == 1)
        {
            if(playerEntity != null)
                playerEntity.sendMessage(new TranslationTextComponent("msg.recsave"),playerEntity.getUniqueID());
            state = 2;
            world.setBlockState(pos,world.getBlockState(pos).with(MusicBlock.RECORDING,false));
            if(playerEntity != null) {
                lastPlayerRecord = playerEntity;
                this.author = playerEntity.getDisplayName().getString();
            }
            else {
                this.author = author;
            }
            markDirty();
        }
        else {
            if(playerEntity != null)
                playerEntity.sendMessage(new TranslationTextComponent("msg.recstart"), playerEntity.getUniqueID());
            recorded.clear();
            state = 1;
            tick = 0;

            world.setBlockState(pos, world.getBlockState(pos).with(MusicBlock.RECORDING, true));
        }
        return state;
    }




    /**
     * This function returns song data as NBT
     * @return The song data as ListNBT
     */
    public ListNBT getSongDataNBT()
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

    /**
     * Don't touch, this is used by minecraft to write NBT. Use <code>getSongDataNBT()</code> and the other methods to retrieve the info you want
     */
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("noteData", getSongDataNBT());
        compound.putString("author",author);
        compound.putString("name",songName);
        return compound;
    }

    /**
     * Don't touch, this is used by minecraft to read NBT. Use <code>applyNBTSettings()</code>
     */
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

    /**
     * Imports provided parameters as the current NBT
     * @param musicData the song data
     * @param author the author
     * @param songName the song name
     */
    public void applyNBTSettings(ListNBT musicData, String author, String songName)
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

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        if(nbt == null) return null;

        nbt.putString("author",author);
        nbt.putString("name",songName);
        nbt.putInt("state",state);
        nbt.putInt("tick",tick);
        nbt.put("noteData",getSongDataNBT());
        return nbt;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = super.getUpdateTag();

        if(nbt == null) return new SUpdateTileEntityPacket(getPos(),-1,nbt);
        nbt.putString("author",author);
        nbt.putString("name",songName);
        nbt.putInt("state",state);
        nbt.putInt("tick",tick);
        nbt.put("noteData",getSongDataNBT());

        System.out.println("Sending data : Tick:" + tick + "; State: " + state);

        return new SUpdateTileEntityPacket(getPos(),-1,nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT tag = pkt.getNbtCompound();

        if(tag == null) return;
        if(!tag.contains("author")) return;
        if(!tag.contains("name")) return;
        if(!tag.contains("noteData")) return;
        if(!tag.contains("state")) return;
        if(!tag.contains("tick")) return;

        applyNBTSettings((ListNBT) tag.get("noteData"),tag.getString("author"),tag.getString("name"));
        state = tag.getInt("state");
        tick = tag.getInt("tick");

        System.out.println("Aquired data from server: Tick:" + tick + "; State: " + state);
    }

    @Override
    public void handleUpdateTag(BlockState bs, CompoundNBT tag) {
        if(tag == null) return;
        if(!tag.contains("author")) return;
        if(!tag.contains("name")) return;
        if(!tag.contains("noteData")) return;
        if(!tag.contains("state")) return;
        if(!tag.contains("tick")) return;

        applyNBTSettings((ListNBT) tag.get("noteData"),tag.getString("author"),tag.getString("name"));
        state = tag.getInt("state");
        tick = tag.getInt("tick");
    }
}
