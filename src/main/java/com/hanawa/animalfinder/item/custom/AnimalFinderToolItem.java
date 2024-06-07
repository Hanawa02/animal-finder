package com.hanawa.animalfinder.item.custom;

import com.hanawa.animalfinder.util.AnimalFinderModTags;
import com.hanawa.animalfinder.util.CompoundTagUtil;
import com.hanawa.animalfinder.util.ForgeExtraModTags;
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

public class AnimalFinderToolItem extends Item {
    private final int distance;
    private final int maxSlots;

    private final String MODE_ALL = "animalfinder.tool.search_mode.all";

    private final String STORAGE_KEY_MODE = "MODE";
    private final String STORAGE_KEY_INDEXED_ENTITIES = "INDEXED_ENTITIES";

    enum TOOL_ACTIONS {
        CHANGE_MODE,
        REGISTER_ENTITY,
        UNREGISTER_ENTITY,
        REGISTER_ENTITY_FROM_ITEM,
        UNREGISTER_ENTITY_FROM_ITEM,
        SEARCH
    }

    public AnimalFinderToolItem(Properties properties, int distance, int maxSlots ) {
        super(properties.stacksTo(1));
        this.distance = distance;
        this.maxSlots = maxSlots;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack item, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        List<String> registeredEntities = getRegisteredEntities(item);

        tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.base"));
        tooltipComponents.add(Component.literal("")); // line break

        CompoundTag tagCompound = item.getOrCreateTag();
        String searchMode = tagCompound.getString(STORAGE_KEY_MODE);

        if (!searchMode.isBlank()) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.search_mode", I18n.get(searchMode)));
            tooltipComponents.add(Component.literal("")); // line break
        }

        if (registeredEntities.isEmpty()) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.no_animal"));

            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        List<String> registeredEntitiesName = new ArrayList<>(registeredEntities.stream().map(I18n::get).toList());
        Collections.sort(registeredEntitiesName);

        String lastAnimal = I18n.get(registeredEntitiesName.get(registeredEntitiesName.size() - 1));

        if (registeredEntitiesName.size() == 1) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.one_animal", lastAnimal));
            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        String firstAnimals = String.join(", ", registeredEntitiesName.subList(0, registeredEntitiesName.size() - 1));

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

        if (mainHandItem.isEmpty() || !mainHandItem.is(AnimalFinderModTags.Items.ANIMAL_FINDER_TOOL)) {
            return InteractionResult.PASS;
        }

        TOOL_ACTIONS action = TOOL_ACTIONS.SEARCH;
        boolean isPlayerSneaking = player.isShiftKeyDown();
        boolean userHasItemOnBothHands = !mainHandItem.isEmpty() && !offSetHandItem.isEmpty();

        if (userHasItemOnBothHands) {
            action = isPlayerSneaking ? TOOL_ACTIONS.UNREGISTER_ENTITY_FROM_ITEM : TOOL_ACTIONS.REGISTER_ENTITY_FROM_ITEM;
        } else if (isPlayerSneaking) {
            action = TOOL_ACTIONS.CHANGE_MODE;
        }

        switch (action) {
            case SEARCH -> executeAnimalSearch(context, player, mainHandItem);
            case CHANGE_MODE -> changeToolSearchMode(player, mainHandItem);
            case REGISTER_ENTITY_FROM_ITEM -> registerEntityFromItem(player, mainHandItem, offSetHandItem);
            case UNREGISTER_ENTITY_FROM_ITEM -> unregisterEntityFromItem(player, mainHandItem, offSetHandItem);
        }
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack item, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        TOOL_ACTIONS action =  TOOL_ACTIONS.REGISTER_ENTITY;

        if (player.isShiftKeyDown()) {
            action = TOOL_ACTIONS.UNREGISTER_ENTITY;
        }

        switch (action) {
            case REGISTER_ENTITY -> registerEntityFromLivingEntity(player, entity);
            case UNREGISTER_ENTITY -> unregisterEntityFromLivingEntity(player, entity);
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

        List<String> registeredEntities = getRegisteredEntities(mainHandItem);

        if(!registeredEntities.isEmpty()){
            String currentMode = getSearchMode(mainHandItem);
            int currentModeIndex = registeredEntities.indexOf(currentMode);

            if (currentModeIndex == registeredEntities.size() - 1) {
                setSearchMode(mainHandItem, MODE_ALL);
            } else {
                setSearchMode(mainHandItem, registeredEntities.get(currentModeIndex + 1));
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

    private void registerEntity(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull String entityId) {
        List<String> registeredEntities = getRegisteredEntities(searchToolItem);

        if (registeredEntities.size() >= maxSlots) {
            sendMessage(player,"animalfinder.tool.message.full_storage");
            return;
        }

        EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));

        if (entity == null) {
            sendMessage(player,"animalfinder.tool.message.entity_not_found");
            return;
        }

        boolean isAnimal = entity.getTags().toList().contains(ForgeExtraModTags.EntityTypes.ANIMALS);

        if (!isAnimal) {
            sendMessage(player,"animalfinder.tool.message.not_an_animal");
            return;
        }

        if (!registeredEntities.contains(entityId)) {
            registeredEntities.add(entityId);
            setRegisteredEntities(searchToolItem, registeredEntities);
            sendMessage(player,  "animalfinder.tool.message.animal_registered", I18n.get(entityId));
        }
    }

    private void unregisterEntity(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull String entityId) {
        List<String> registeredEntities = getRegisteredEntities(searchToolItem);

        if (registeredEntities.contains(entityId)) {
            registeredEntities.remove(entityId);
            setRegisteredEntities(searchToolItem, registeredEntities);
            sendMessage(player,"animalfinder.tool.message.animal_unregistered", I18n.get(entityId));
            return;
        }

        sendMessage(player,"animalfinder.tool.message.animal_not_registered");
    }

    private void registerEntityFromLivingEntity(@NotNull Player player, @NotNull LivingEntity entity) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return;
        }

        registerEntity(player, mainHandItem, entity.getType().toString());
    }

    private void unregisterEntityFromLivingEntity(Player player, LivingEntity entity) {
        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return;
        }

        unregisterEntity(player, mainHandItem, entity.getType().toString());
    }

    private void registerEntityFromItem(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull ItemStack item) {
//        registerEntity(player, searchToolItem, entityId);
    }

    private void unregisterEntityFromItem(@NotNull Player player, @NotNull ItemStack searchToolItem, @NotNull ItemStack item) {
//        unregisterEntity(player, searchToolItem, entityId);
    }


    /* Search */
    private void executeAnimalSearch(UseOnContext context, Player player, ItemStack mainHandItem) {
        List<String> registeredEntities = getRegisteredEntities(mainHandItem);

        if (registeredEntities.isEmpty()) {
            sendMessage(player, "animalfinder.tool.message.empty_storage");
            return;
        }

        BlockPos searchingPosition = context.getClickedPos();
        sendMessage(
            player,
 "animalfinder.tool.message.searching",
            getTranslatedEntitiesName(registeredEntities),
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
        List<Entity> validEntities = new ArrayList<>();

        List<String> registeredEntities = getRegisteredEntities(mainHandItem);

        CompoundTag compoundTag = mainHandItem.getOrCreateTag();
        String search_mode = compoundTag.getString(STORAGE_KEY_MODE);
        if (search_mode.isEmpty()) {
            search_mode = MODE_ALL;
        }
        
        for(Entity entity: entities) {
            String entityType = entity.getType().toString();
            if (search_mode.equals(MODE_ALL) && registeredEntities.contains(entityType)) {
                validEntities.add(entity);
            } else if (search_mode.equals(entityType)) {
                validEntities.add(entity);
            }
        }

        return validEntities;
    }

    private void notifyUserAboutEntitiesFound(List<Entity> entities, Player player) {
        for(Entity entity: entities) {
            String animalName = entity.getType().getDescriptionId();

            if (I18n.exists(animalName)) {
                animalName = I18n.get(animalName);
            }

            sendMessage(
                player,
    "animalfinder.tool.message.search_result_found",
                animalName,
                formatVectorForMessage(entity.trackingPosition())
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
