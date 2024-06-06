package com.hanawa.animalfinder.util;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompoundTagUtil {
    private static String getTotalItemsKey(String baseKey) {
        return baseKey + "_TOTAL";
    }

    private static String getItemKey(String baseKey, int itemIndex) {
        return baseKey + "_" + itemIndex;
    }

    public static List<String> getStringArray(CompoundTag tagCompound, String key) {
        if (tagCompound == null) {
            return new ArrayList<>();
        }

        int arraySize = tagCompound.getInt(getTotalItemsKey(key));
        List<String> arrayItems = new ArrayList<>();

        for(int index = 0; index < arraySize; index++) {
            String entity = tagCompound.getString(getItemKey(key, index));

            if (!entity.isBlank()) {
                arrayItems.add(entity);
            }
        }

        return arrayItems;
    }

    public static void putStringArray(@NotNull CompoundTag tagCompound, String key, List<String> entities) {
        int arraySize = tagCompound.getInt(getTotalItemsKey(key));

        // removes all registered entities
        for(int index = 0; index < arraySize; index++) {
            tagCompound.remove(getItemKey(key, index));
        }

        // register list again
        for(int index = 0; index < entities.size(); index++) {
            tagCompound.putString(getItemKey(key, index), entities.get(index));
        }

        tagCompound.putInt(getTotalItemsKey(key), entities.size());
    }

}
