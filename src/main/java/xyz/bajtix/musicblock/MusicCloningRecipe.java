package xyz.bajtix.musicblock;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MusicCloningRecipe extends SpecialRecipe {
    public MusicCloningRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    public boolean matches(CraftingInventory inv, World worldIn) {
        int i = 0;
        boolean oneHasData = false;

        for(int j = 0; j < inv.getSizeInventory(); ++j) {
            if(inv.getStackInSlot(j).getItem() == ItemList.musicBlock) {
                i++;

                if(inv.getStackInSlot(j).hasTag() && inv.getStackInSlot(j).getTag().contains("music"))
                    oneHasData = !oneHasData;
            }
        }

        return i == 2 && oneHasData;
    }

    /**
     * Returns an Item that is the result of this recipe
     *
     * @param inv
     */
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {

        int i = 0;
        CompoundNBT data = null;

        for(int j = 0; j < inv.getSizeInventory(); ++j) {
            if(inv.getStackInSlot(j).getItem() == ItemList.musicBlock) {
                i++;

                if(inv.getStackInSlot(j).hasTag() && inv.getStackInSlot(j).getTag().contains("music"))
                    data = inv.getStackInSlot(j).getTag();
            }
        }

        if(data == null)
            return null;

        ItemStack stack = new ItemStack(ItemList.musicBlock,1,data);
        stack.setTag(data);
        return stack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (itemstack.hasContainerItem()) {
                nonnulllist.set(i, itemstack.getContainerItem());
            } else if (itemstack.getItem() == ItemList.musicBlock) {
                ItemStack itemstack1 = itemstack.copy();
                itemstack1.setCount(1);
                nonnulllist.set(i, itemstack1);
                break;
            }
        }

        return nonnulllist;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     *
     * @param width
     * @param height
     */
    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
}
