package com.hanawa.animalfinder.util;

import com.hanawa.animalfinder.tag.ForgeExtraModTags;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ItemToAnimalMap {
    private static ItemToAnimalMap INSTANCE;
    private final Map<String, String> itemToAnimalMap;

    private static final Logger LOGGER = LogUtils.getLogger();

    private ItemToAnimalMap() {
        this.itemToAnimalMap = new HashMap<>();
    }

    public static ItemToAnimalMap getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemToAnimalMap();
        }

        return INSTANCE;
    }

    public void addEntry(@NotNull String itemId, @NotNull String animalId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(animalId));

        if (entity == null || item == null) {
            return;
        }

        boolean isAnimal = entity.getTags().toList().contains(ForgeExtraModTags.EntityTypes.ANIMALS);
        if (!isAnimal) {
            return;
        }

        if (itemToAnimalMap.containsKey(itemId)) {
            LOGGER.warn("[Animal Finder] item already mapped to an animal. Mapping to animal '{}' not saved.", animalId);
            return;
        }

        LOGGER.info(String.format("[Animal Finder] item '%s' mapped to animal '%s'.", itemId, animalId));
        itemToAnimalMap.put(itemId, animalId);
    }

    public Map<String, String> getMap() {
        return new HashMap<>(itemToAnimalMap);
    }
}
