package com.hanawa.animalfinder.item;

import com.hanawa.animalfinder.AnimalFinder;
import com.hanawa.animalfinder.item.custom.AnimalFinderToolItem;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimalFinderModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AnimalFinder.MOD_ID);

    public static final RegistryObject<Item> BASIC_ANIMAL_FINDER = ITEMS.register("basic_animal_finder", () -> new AnimalFinderToolItem(new Item.Properties(), 32, 1));
    public static final RegistryObject<Item> COPPER_ANIMAL_FINDER = ITEMS.register("copper_animal_finder", () -> new AnimalFinderToolItem(new Item.Properties(), 48, 2));
    public static final RegistryObject<Item> IRON_ANIMAL_FINDER = ITEMS.register("iron_animal_finder", () -> new AnimalFinderToolItem(new Item.Properties(), 64, 4));
    public static final RegistryObject<Item> GOLD_ANIMAL_FINDER = ITEMS.register("gold_animal_finder", () -> new AnimalFinderToolItem(new Item.Properties(), 128, 8));
    public static final RegistryObject<Item> DIAMOND_ANIMAL_FINDER = ITEMS.register("diamond_animal_finder", () -> new AnimalFinderToolItem(new Item.Properties(), 256, 256));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
