package com.hanawa.animalfinder;

import com.hanawa.animalfinder.item.ModItems;
import com.hanawa.animalfinder.particle.ModParticles;
import com.hanawa.animalfinder.util.ItemToAnimalMap;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AnimalFinder.MOD_ID)
public class AnimalFinder
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "animalfinder";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public AnimalFinder()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModParticles.register(modEventBus);
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
//        BLOCKS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so items get registered
//        ITEMS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so tabs get registered
//        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BASIC_ANIMAL_FINDER);
            event.accept(ModItems.COPPER_ANIMAL_FINDER);
            event.accept(ModItems.IRON_ANIMAL_FINDER);
            event.accept(ModItems.GOLD_ANIMAL_FINDER);
            event.accept(ModItems.DIAMOND_ANIMAL_FINDER);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        ItemToAnimalMap map = ItemToAnimalMap.getInstance();
        
        map.addEntry(Items.CLAY_BALL.getDescriptionId(), EntityType.AXOLOTL.getDescriptionId());
        map.addEntry(Items.COBBLESTONE.getDescriptionId(), EntityType.BAT.getDescriptionId());
        map.addEntry(Items.OAK_LEAVES.getDescriptionId(), EntityType.BEE.getDescriptionId());
        map.addEntry(Items.SAND.getDescriptionId(), EntityType.CAMEL.getDescriptionId());
        map.addEntry(Items.HAY_BLOCK.getDescriptionId(), EntityType.CAT.getDescriptionId());
        map.addEntry(Items.SPIDER_EYE.getDescriptionId(), EntityType.CAVE_SPIDER.getDescriptionId());
        map.addEntry(Items.FEATHER.getDescriptionId(), EntityType.CHICKEN.getDescriptionId());
        map.addEntry(Items.COD.getDescriptionId(), EntityType.COD.getDescriptionId());
        map.addEntry(Items.BEEF.getDescriptionId(), EntityType.COW.getDescriptionId());
        map.addEntry(Items.COD.getDescriptionId(), EntityType.DOLPHIN.getDescriptionId());
        map.addEntry(Items.CARROT.getDescriptionId(), EntityType.DONKEY.getDescriptionId());
        map.addEntry(Items.CHICKEN.getDescriptionId(), EntityType.FOX.getDescriptionId());
        map.addEntry(Items.LILY_PAD.getDescriptionId(), EntityType.FROG.getDescriptionId());
        map.addEntry(Items.GLOW_INK_SAC.getDescriptionId(), EntityType.GLOW_SQUID.getDescriptionId());
        map.addEntry(Items.MILK_BUCKET.getDescriptionId(), EntityType.GOAT.getDescriptionId());
        map.addEntry(Items.LEATHER.getDescriptionId(), EntityType.HORSE.getDescriptionId());
        map.addEntry(Items.WHITE_CARPET.getDescriptionId(), EntityType.LLAMA.getDescriptionId());
        map.addEntry(Items.POTATO.getDescriptionId(), EntityType.MULE.getDescriptionId());
        map.addEntry(Items.BAMBOO.getDescriptionId(), EntityType.PANDA.getDescriptionId());
        map.addEntry(Items.COOKIE.getDescriptionId(), EntityType.PARROT.getDescriptionId());
        map.addEntry(Items.PORKCHOP.getDescriptionId(), EntityType.PIG.getDescriptionId());
        map.addEntry(Items.SALMON.getDescriptionId(), EntityType.POLAR_BEAR.getDescriptionId());
        map.addEntry(Items.PUFFERFISH.getDescriptionId(), EntityType.PUFFERFISH.getDescriptionId());
        map.addEntry(Items.RABBIT_HIDE.getDescriptionId(), EntityType.RABBIT.getDescriptionId());
        map.addEntry(Items.KELP.getDescriptionId(), EntityType.SALMON.getDescriptionId());
        map.addEntry(Items.MUTTON.getDescriptionId(), EntityType.SHEEP.getDescriptionId());
        map.addEntry(Items.WHITE_WOOL.getDescriptionId(), EntityType.SHEEP.getDescriptionId());
        map.addEntry(Items.STONE.getDescriptionId(), EntityType.SILVERFISH.getDescriptionId());
        map.addEntry(Items.STRING.getDescriptionId(), EntityType.SPIDER.getDescriptionId());
        map.addEntry(Items.INK_SAC.getDescriptionId(), EntityType.SQUID.getDescriptionId());
        map.addEntry(Items.WATER_BUCKET.getDescriptionId(), EntityType.TADPOLE.getDescriptionId());
        map.addEntry(Items.TROPICAL_FISH.getDescriptionId(), EntityType.TROPICAL_FISH.getDescriptionId());
        map.addEntry(Items.SEAGRASS.getDescriptionId(), EntityType.TURTLE.getDescriptionId());
        map.addEntry(Items.MUTTON.getDescriptionId(), EntityType.WOLF.getDescriptionId());
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
