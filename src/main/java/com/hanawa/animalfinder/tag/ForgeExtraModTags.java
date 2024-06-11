package com.hanawa.animalfinder.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;

public class ForgeExtraModTags {
    public static class EntityTypes extends Tags.EntityTypes {
        public static final TagKey<EntityType<?>> ANIMALS = tag("animals");

        private static void init() {
        }

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge", name));
        }
    }
}
