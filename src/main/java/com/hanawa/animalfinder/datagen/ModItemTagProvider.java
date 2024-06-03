package com.hanawa.animalfinder.datagen;


import com.hanawa.animalfinder.AnimalFinder;
import com.hanawa.animalfinder.item.AnimalFinderModItems;
import com.hanawa.animalfinder.util.AnimalFinderModTags;
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
        this.tag(AnimalFinderModTags.Items.ANIMAL_FINDER_TOOL)
            .add(AnimalFinderModItems.BASIC_ANIMAL_FINDER.get())
            .add(AnimalFinderModItems.COPPER_ANIMAL_FINDER.get())
            .add(AnimalFinderModItems.IRON_ANIMAL_FINDER.get())
            .add(AnimalFinderModItems.GOLD_ANIMAL_FINDER.get())
            .add(AnimalFinderModItems.DIAMOND_ANIMAL_FINDER.get());
    }
}