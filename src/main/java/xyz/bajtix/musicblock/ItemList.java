package xyz.bajtix.musicblock;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemList {

    public static final MusicBlockItem musicBlock = new MusicBlockItem(BlockList.musicBlock, new Item.Properties().group(ItemGroup.REDSTONE));
    public static final BlockItem musicNoteBlock = new BlockItem(BlockList.musicNoteBlock, new Item.Properties().group(ItemGroup.REDSTONE));
    public static final Item processor = new Item(new Item.Properties().group(ItemGroup.MISC).maxStackSize(16));

    public static Item[] GetItems()
    {
        return new Item[]{
                musicBlock.setRegistryName("music_block"),
                musicNoteBlock.setRegistryName("note_block"),
                processor.setRegistryName("music_processor")
        };

    }
}
