package xyz.bajtix.musicblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MusicBlockItem extends BlockItem {
    public MusicBlockItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }



    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(stack.getTag() == null) return;

        if(!stack.getTag().contains("author")) return;

        tooltip.add(new StringTextComponent("Recorded by " + stack.getTag().getString("author")));
        stack.getTag().remove("RepairCost"); //after anvil this is added, but it's useless and annoying and prevents from stacking
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains("author");
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        CompoundNBT nbt = context.getItem().getTag();



        boolean success = context.getWorld().setBlockState(context.getPos(), state, 11);

        if(nbt == null || !nbt.contains("music"))
            return success;

        if(success)
            ((MusicBlockTileEntity)(context.getWorld().getTileEntity(context.getPos()))).applyNBTSettings((ListNBT) nbt.get("music"),nbt.getString("author"),context.getItem().getDisplayName().getString());

        return success;
    }
}
