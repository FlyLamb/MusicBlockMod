package xyz.bajtix.musicblock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.IPosition;

public class BlockList {

    public static final MusicBlock musicBlock = new MusicBlock(Block.Properties.create(Material.WOOD).harvestLevel(1).hardnessAndResistance(4));
    public static final MusicNoteBlock musicNoteBlock = new MusicNoteBlock(Block.Properties.create(Material.WOOD).harvestLevel(1).hardnessAndResistance(2));

    public static Block[] GetBlocks()
    {
        return new Block[]{
            musicBlock.setRegistryName("music_block"),
            musicNoteBlock.setRegistryName("note_block")
        };

    }
}
