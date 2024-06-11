package com.hanawa.animalfinder.datagen;


import com.hanawa.animalfinder.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }


    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        finderToolRecipe(recipeOutput, ModItems.BASIC_ANIMAL_FINDER.get(), Items.COMPASS, ItemTags.PLANKS, Tags.Items.INGOTS_COPPER);
        finderToolRecipe(recipeOutput, ModItems.COPPER_ANIMAL_FINDER.get(), ModItems.BASIC_ANIMAL_FINDER.get(), Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_IRON);
        finderToolRecipe(recipeOutput, ModItems.IRON_ANIMAL_FINDER.get(), ModItems.COPPER_ANIMAL_FINDER.get(), Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_GOLD);
        finderToolRecipe(recipeOutput, ModItems.GOLD_ANIMAL_FINDER.get(), ModItems.IRON_ANIMAL_FINDER.get(), Tags.Items.INGOTS_GOLD, Tags.Items.GEMS_DIAMOND);
        finderToolRecipe(recipeOutput, ModItems.DIAMOND_ANIMAL_FINDER.get(), ModItems.GOLD_ANIMAL_FINDER.get(), Tags.Items.GEMS_DIAMOND, Tags.Items.INGOTS_NETHERITE);
    }


    protected static void finderToolRecipe(RecipeOutput recipeOutput, Item recipeItem, Item core, TagKey<Item> frame, TagKey<Item> connector ) {
        ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, recipeItem, 1)
                .define('A', frame)
                .define('B', core)
                .define('C', connector)
                .pattern("AAA")
                .pattern("ABA")
                .pattern(" C ")
                .group("animal_finder_tool")
                .unlockedBy("has_connector_" + connector.getClass().getName(), has(connector))
                .save(recipeOutput);
    }
}
