package com.hanawa.animalfinder.datagen;

import com.hanawa.animalfinder.AnimalFinder;
import com.hanawa.animalfinder.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AnimalFinder.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.BASIC_ANIMAL_FINDER);
        simpleItem(ModItems.COPPER_ANIMAL_FINDER);
        simpleItem(ModItems.IRON_ANIMAL_FINDER);
        simpleItem(ModItems.GOLD_ANIMAL_FINDER);
        simpleItem(ModItems.DIAMOND_ANIMAL_FINDER);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(
                item.getId().getPath(),
                new ResourceLocation("item/generated")
        ).texture(
                "layer0",
                new ResourceLocation(AnimalFinder.MOD_ID, "item/" + item.getId().getPath())
        );
    }
}
