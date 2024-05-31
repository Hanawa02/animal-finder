package com.hanawa.animalfinder.item;

import com.hanawa.animalfinder.AnimalFinder;
import com.hanawa.animalfinder.item.custom.ChickenFinderItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AnimalFinder.MOD_ID);

    public static final RegistryObject<Item> CHICKEN_FINDER = ITEMS.register("chicken_finder", () -> new ChickenFinderItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
