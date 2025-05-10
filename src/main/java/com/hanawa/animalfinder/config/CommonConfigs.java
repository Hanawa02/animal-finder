package com.hanawa.animalfinder.config;

import java.util.Map;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_1_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_2_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_3_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_4_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_5_RANGE;

    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_1_SLOTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_2_SLOTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_3_SLOTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_4_SLOTS;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_TIER_5_SLOTS;

    public static final ForgeConfigSpec.ConfigValue<Integer> ANIMAL_FINDER_MAX_RESULTS_PER_ANIMAL;

static {
    BUILDER.push("Configs for Animal Finder");

    ANIMAL_FINDER_TIER_1_RANGE = BUILDER.comment("Distance covered by Basic Animal Finder").define("Basic Animal Finder Range", 32);
    ANIMAL_FINDER_TIER_1_SLOTS = BUILDER.comment("Number of Animal Type Slots for Basic Animal Finder").define("Basic Animal Finder Slots", 1);

    ANIMAL_FINDER_TIER_2_RANGE = BUILDER.comment("Distance covered by Copper Animal Finder").define("Copper Animal Finder Range", 64);
    ANIMAL_FINDER_TIER_2_SLOTS = BUILDER.comment("Number of Animal Type Slots for Copper Animal Finder").define("Copper Animal Finder Slots", 2);

    ANIMAL_FINDER_TIER_3_RANGE = BUILDER.comment("Distance covered by Iron Animal Finder").define("Iron Animal Finder Range", 96);
    ANIMAL_FINDER_TIER_3_SLOTS = BUILDER.comment("Number of Animal Type Slots for Iron Animal Finder").define("Iron Animal Finder Slots", 4);

    ANIMAL_FINDER_TIER_4_RANGE = BUILDER.comment("Distance covered by Gold Animal Finder").define("Gold Animal Finder Range", 128);
    ANIMAL_FINDER_TIER_4_SLOTS = BUILDER.comment("Number of Animal Type Slots for Gold Animal Finder").define("Gold Animal Finder Slots", 8);

    ANIMAL_FINDER_TIER_5_RANGE = BUILDER.comment("Distance covered by Diamond Animal Finder").define("Diamond Animal Finder Range", 256);
    ANIMAL_FINDER_TIER_5_SLOTS = BUILDER.comment("Number of Animal Type Slots for Diamond Animal Finder").define("Diamond Animal Finder Slots", 256);

    ANIMAL_FINDER_MAX_RESULTS_PER_ANIMAL = BUILDER.comment("The max number of animals to be highlighted (by type of animal)").define("Animal Finder Max Results per animal", 5);

    BUILDER.pop();

    SPEC = BUILDER.build();
}
}
