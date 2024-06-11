package com.hanawa.animalfinder.datagen;


import com.hanawa.animalfinder.AnimalFinder;
import com.hanawa.animalfinder.item.ModItems;
import com.hanawa.animalfinder.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, AnimalFinder.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModTags.Items.ANIMAL_FINDER_TOOL)
            .add(ModItems.BASIC_ANIMAL_FINDER.get())
            .add(ModItems.COPPER_ANIMAL_FINDER.get())
            .add(ModItems.IRON_ANIMAL_FINDER.get())
            .add(ModItems.GOLD_ANIMAL_FINDER.get())
            .add(ModItems.DIAMOND_ANIMAL_FINDER.get());
    }
}