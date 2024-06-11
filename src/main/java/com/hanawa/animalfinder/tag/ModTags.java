package com.hanawa.animalfinder.tag;

import com.hanawa.animalfinder.AnimalFinder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> ANIMAL_FINDER_TOOL = tag("animal_finder_tools");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(AnimalFinder.MOD_ID, name));
        }
    }
}
