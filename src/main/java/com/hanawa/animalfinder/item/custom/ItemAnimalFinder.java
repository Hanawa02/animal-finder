package com.hanawa.animalfinder.item.custom;

import com.hanawa.animalfinder.tag.ModTags;
import com.hanawa.animalfinder.util.CompoundTagUtil;
import com.hanawa.animalfinder.tag.ForgeExtraModTags;
import com.hanawa.animalfinder.util.ItemToAnimalMap;
import com.hanawa.animalfinder.util.TimeOut;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ItemAnimalFinder extends Item {
    private final int distance;
    private final int maxSlots;

    private final int MAX_RESULTS_PER_ANIMAL = 3;
    private final int ANIMAL_GLOW_DURATION_MS = 30000;
    private final String MODE_ALL = "animalfinder.tool.search_mode.all";

    private final String STORAGE_KEY_MODE = "MODE";
    private final String STORAGE_KEY_INDEXED_ENTITIES = "INDEXED_ENTITIES";

    enum TOOL_ACTIONS {
        CHANGE_MODE,
        REGISTER_ANIMAL,
        UNREGISTER_ANIMAL,
        REGISTER_ANIMAL_FROM_ITEM,
        UNREGISTER_ANIMAL_FROM_ITEM,
        SEARCH
    }

    public ItemAnimalFinder(Properties properties, int distance, int maxSlots ) {
        super(properties.stacksTo(1));
        this.distance = distance;
        this.maxSlots = maxSlots;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack item, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        List<String> registeredAnimals = getRegisteredEntities(item);

        tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.base"));
        tooltipComponents.add(Component.literal("")); // line break

        CompoundTag tagCompound = item.getOrCreateTag();
        String searchMode = tagCompound.getString(STORAGE_KEY_MODE);

        if (!searchMode.isBlank()) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.search_mode", I18n.get(searchMode)));
            tooltipComponents.add(Component.literal("")); // line break
        }

        if (registeredAnimals.isEmpty()) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.no_animal"));

            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        List<String> registeredAnimalsName = new ArrayList<>(registeredAnimals.stream().map(I18n::get).toList());
        Collections.sort(registeredAnimalsName);

        String lastAnimal = I18n.get(registeredAnimalsName.get(registeredAnimalsName.size() - 1));

        if (registeredAnimalsName.size() == 1) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.one_animal", lastAnimal));
            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        String firstAnimals = String.join(", ", registeredAnimalsName.subList(0, registeredAnimalsName.size() - 1));

        tooltipComponents.add(
            Component.translatable(
            "animalfinder.tool.tooltip.more_animals",
                firstAnimals,
                lastAnimal
            )
        );
        super.appendHoverText(item, level, tooltipComponents, isAdvanced);
    }

    /* Interactions Entrypoint */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();

        if (context.getLevel().isClientSide() || player == null) {
            return InteractionResult.PASS;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offSetHandItem = player.getOffhandItem();

        if (mainHandItem.isEmpty() || !mainHandItem.is(ModTags.Items.ANIMAL_FINDER_TOOL)) {
            return InteractionResult.PASS;
        }

        TOOL_ACTIONS action = TOOL_ACTIONS.SEARCH;
        boolean isPlayerSneaking = player.isShiftKeyDown();
        boolean userHasItemOnBothHands = !mainHandItem.isEmpty() && !offSetHandItem.isEmpty();

        if (userHasItemOnBothHands) {
            action = isPlayerSneaking ? TOOL_ACTIONS.UNREGISTER_ANIMAL_FROM_ITEM : TOOL_ACTIONS.REGISTER_ANIMAL_FROM_ITEM;
        } else if (isPlayerSneaking) {
            action = TOOL_ACTIONS.CHANGE_MODE;
        }

        switch (action) {
            case SEARCH -> executeAnimalSearch(context, player, mainHandItem);
            case CHANGE_MODE -> changeToolSearchMode(player, mainHandItem);
            case REGISTER_ANIMAL_FROM_ITEM -> registerEntityFromItem(player, mainHandItem, offSetHandItem);
            case UNREGISTER_ANIMAL_FROM_ITEM -> unregisterEntityFromItem(player, mainHandItem, offSetHandItem);
        }
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack item, @NotNull Player player, @NotNull LivingEntity animal, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        TOOL_ACTIONS action =  TOOL_ACTIONS.REGISTER_ANIMAL;

        if (player.isShiftKeyDown()) {
            action = TOOL_ACTIONS.UNREGISTER_ANIMAL;
        }

        switch (action) {
            case REGISTER_ANIMAL -> registerEntityFromLivingEntity(player, animal);
            case UNREGISTER_ANIMAL -> unregisterEntityFromLivingEntity(player, animal);
        }

        return InteractionResult.SUCCESS;
    }

    /* Search mode */
    private String getSearchMode(ItemStack item) {
        CompoundTag tagCompound = item.getOrCreateTag();
        String searchMode = tagCompound.getString(STORAGE_KEY_MODE);
        if (searchMode.isBlank()) {
            setSearchMode(item, MODE_ALL);

            return MODE_ALL;
        }

        return searchMode;
    }

    private void setSearchMode(ItemStack item, String mode) {
        CompoundTag tagCompound = item.getOrCreateTag();
        tagCompound.putString(STORAGE_KEY_MODE, mode);
    }

    private void changeToolSearchMode(Player player, ItemStack mainHandItem) {
        String modeBeforeChange = getSearchMode(mainHandItem);

        List<String> registeredAnimals =  getRegisteredEntities(mainHandItem);

        if(!registeredAnimals.isEmpty()){
            String currentMode = getSearchMode(mainHandItem);
            int currentModeIndex = registeredAnimals.indexOf(currentMode);

            if (currentModeIndex == registeredAnimals.size() - 1) {
                setSearchMode(mainHandItem, MODE_ALL);
            } else {
                setSearchMode(mainHandItem, registeredAnimals.get(currentModeIndex + 1));
            }
        }

        if (!modeBeforeChange.equals(getSearchMode(mainHandItem))) {
            sendMessage(player, "animalfinder.tool.message.search_mode_set", I18n.get(getSearchMode(mainHandItem)));
        }
    }

    /* Registered Entities */
    private List<String> getRegisteredEntities(ItemStack item) {
        CompoundTag tagCompound = item.getOrCreateTag();
        return CompoundTagUtil.getStringArray(tagCompound, STORAGE_KEY_INDEXED_ENTITIES);
    }

    private void setRegisteredEntities(ItemStack item, List<String> entities) {
        CompoundTag tagCompound = item.getOrCreateTag();
        CompoundTagUtil.putStringArray(tagCompound, STORAGE_KEY_INDEXED_ENTITIES, entities);
    }

    private void registerEntity(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull String animalId) {
        List<String> registeredAnimals = getRegisteredEntities(searchToolItem);

        if (registeredAnimals.size() >= maxSlots) {
            sendMessage(player,"animalfinder.tool.message.full_storage");
            return;
        }

        EntityType<?> animal = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(animalId));

        if (animal == null) {
            sendMessage(player,"animalfinder.tool.message.animal_not_found");
            return;
        }

        boolean isAnimal = animal.getTags().toList().contains(ForgeExtraModTags.EntityTypes.ANIMALS);

        if (!isAnimal) {
            sendMessage(player,"animalfinder.tool.message.not_an_animal");
            return;
        }

        if (!registeredAnimals.contains(animalId)) {
            registeredAnimals.add(animalId);
            setRegisteredEntities(searchToolItem, registeredAnimals);
            sendMessage(player,  "animalfinder.tool.message.animal_registered", I18n.get(animalId));
        }
    }

    private void unregisterEntity(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull String animalId) {
        List<String> registeredAnimals = getRegisteredEntities(searchToolItem);

        if (registeredAnimals.contains(animalId)) {
            registeredAnimals.remove(animalId);
            setRegisteredEntities(searchToolItem, registeredAnimals);

            // Resets search mode in case the mode was set for the removed animal
            if (getSearchMode(searchToolItem).equals(animalId)) {
                setSearchMode(searchToolItem, MODE_ALL);
            }

            sendMessage(player,"animalfinder.tool.message.animal_unregistered", I18n.get(animalId));
            return;
        }

        sendMessage(player,"animalfinder.tool.message.animal_not_registered");
    }

    private void registerEntityFromLivingEntity(@NotNull Player player, @NotNull LivingEntity animal) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return;
        }

        registerEntity(player, mainHandItem, animal.getType().toString());
    }

    private void unregisterEntityFromLivingEntity(Player player, LivingEntity animal) {
        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return;
        }

        unregisterEntity(player, mainHandItem, animal.getType().toString());
    }

    private void registerEntityFromItem(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull ItemStack item) {
        String animalId = ItemToAnimalMap.getInstance().getMap().get(item.getDescriptionId());
        registerEntity(player, searchToolItem, animalId);
    }

    private void unregisterEntityFromItem(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull ItemStack item) {
        String animalId = ItemToAnimalMap.getInstance().getMap().get(item.getDescriptionId());
        unregisterEntity(player, searchToolItem, animalId);
    }

    /* Search */
    private void executeAnimalSearch(UseOnContext context, Player player, ItemStack mainHandItem) {
        List<String> registeredAnimals = getRegisteredEntities(mainHandItem);

        if (registeredAnimals.isEmpty()) {
            sendMessage(player, "animalfinder.tool.message.empty_storage");
            return;
        }

        BlockPos searchingPosition = context.getClickedPos();
        sendMessage(
            player,
 "animalfinder.tool.message.searching",
            getTranslatedEntitiesName(registeredAnimals),
            distance,
            formatVectorForMessage(searchingPosition.getCenter())
        );

        List<Entity> entitiesFound = getEntities(context);
        if (entitiesFound == null) {
            return;
        }

        List<Entity> animalsFound = filterValidEntities(entitiesFound, mainHandItem);
        notifyUserAboutEntitiesFound(animalsFound, player);
    }

    private List<Entity> getEntities(UseOnContext context) {
        if(context.getLevel().isClientSide()) {
            return null;
        }

        BlockPos positionClicked = context.getClickedPos();

        AABB area = new AABB(
            positionClicked.getX() - distance, positionClicked.getY() - distance, positionClicked.getZ() - distance,
            positionClicked.getX() + distance, positionClicked.getY() + distance, positionClicked.getZ() + distance);

       return context.getLevel().getEntities(null, area);
    }

    private List<Entity> filterValidEntities(List<Entity> entities, ItemStack mainHandItem) {
        Map<String, List<Entity>> validEntities = new HashMap<>();

        List<String> registeredAnimals = new ArrayList<>(getRegisteredEntities(mainHandItem));

        CompoundTag compoundTag = mainHandItem.getOrCreateTag();
        String search_mode = compoundTag.getString(STORAGE_KEY_MODE);
        if (search_mode.isEmpty()) {
            search_mode = MODE_ALL;
        }
        
        for(Entity animal: entities) {
            String animalType = animal.getType().toString();
            boolean addAnimal = false;
            if (search_mode.equals(MODE_ALL) && registeredAnimals.contains(animalType)) {
                addAnimal = true;
            } else if (search_mode.equals(animalType)) {
                addAnimal = true;
            }

            if (addAnimal) {
                List<Entity> animalsOfTypeFound = validEntities.get(animalType);
                if (animalsOfTypeFound == null) {
                    animalsOfTypeFound = new ArrayList<>();
                }

                if (animalsOfTypeFound.size() >= MAX_RESULTS_PER_ANIMAL) {
                    continue;
                }

                animalsOfTypeFound.add(animal);
                validEntities.put(animalType, animalsOfTypeFound);
            }

        }

        return validEntities.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void notifyUserAboutEntitiesFound(List<Entity> entities, Player player) {
        TimeOut.getInstance().setTimeout(() -> {
            for(Entity animal: entities) {
                animal.setGlowingTag(false);
            }
        }, ANIMAL_GLOW_DURATION_MS);

        for(Entity animal: entities) {
            animal.setGlowingTag(true);

            String animalName = animal.getType().getDescriptionId();

            if (I18n.exists(animalName)) {
                animalName = I18n.get(animalName);
            }

            sendMessage(
                player,
    "animalfinder.tool.message.search_result_found",
                animalName,
                formatVectorForMessage(animal.trackingPosition())
            );
        }

        if (entities.isEmpty()) {
            sendMessage(player, "animalfinder.tool.message.search_result_empty");
        }
    }

    /* Utils */
    private void sendMessage(Player player, String messageKey, Object... pArgs) {
        if (player != null ) {
            player.sendSystemMessage(Component.translatable(messageKey, pArgs));
        }
    }

    private String formatVectorForMessage(Vec3 vector) {
        return String.format("(X: %.0f, Y: %.0f, Z: %.0f)", vector.x, vector.y, vector.z);
    }

    private String getTranslatedEntitiesName(List<String> entities) {
        List<String> entitiesName = new ArrayList<>(entities.stream().map(I18n::get).toList());
        Collections.sort(entitiesName);
        return  String.join(", ", entitiesName);
    }
}
