package xyz.bajtix.musicblock;

import net.minecraft.item.crafting.SpecialRecipeSerializer;

class MusicCloningSerializer
{
    public static final SpecialRecipeSerializer<MusicCloningRecipe> CRAFTING_SPECIAL_MUSICCLONING = (SpecialRecipeSerializer<MusicCloningRecipe>) new SpecialRecipeSerializer<>(MusicCloningRecipe::new).setRegistryName("crafting_special_musiccloning");
}