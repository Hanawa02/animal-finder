package com.hanawa.animalfinder.item.custom;


import com.hanawa.animalfinder.util.AnimalFinderModTags;
import com.hanawa.animalfinder.util.CompoundTagUtil;
import com.hanawa.animalfinder.util.ForgeExtraModTags;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimalFinderToolItem extends Item {
    private final int distance;
    private final int maxSlots;

    private final String MODE_ALL = "animalfinder.tool.search_mode.all";

    private final String STORAGE_KEY_MODE = "MODE";
    private final String STORAGE_KEY_INDEXED_ENTITIES = "INDEXED_ENTITIES";

    public AnimalFinderToolItem(Properties properties, int distance, int maxSlots ) {
        super(properties.stacksTo(1));
        this.distance = distance;
        this.maxSlots = maxSlots;
    }

    @Override
    public void appendHoverText(ItemStack item, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        List<String> registeredEntities = getRegisteredEntities(item);

        tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.base"));
        tooltipComponents.add(Component.literal("")); // line break

        String searchMode = getSearchMode(item);
        tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.search_mode", I18n.get(searchMode)));
        tooltipComponents.add(Component.literal("")); // line break
        
        if (registeredEntities.isEmpty()) {
            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        String lastAnimal = I18n.get(registeredEntities.get(registeredEntities.size() - 1));

        if (registeredEntities.size() == 1) {
            tooltipComponents.add(Component.translatable("animalfinder.tool.tooltip.one_animal", lastAnimal));
            super.appendHoverText(item, level, tooltipComponents, isAdvanced);
            return;
        }

        String firstAnimals = getTranslatedEntitiesName(registeredEntities.subList(0, registeredEntities.size() - 1));

        tooltipComponents.add(
            Component.translatable(
            "animalfinder.tool.tooltip.more_animals",
                firstAnimals,
                lastAnimal
            )
        );
        super.appendHoverText(item, level, tooltipComponents, isAdvanced);
    }

    /* Interactions Entrypoints */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (context.getLevel().isClientSide() || player == null) {
            return InteractionResult.PASS;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty() || !mainHandItem.is(AnimalFinderModTags.Items.ANIMAL_FINDER_TOOL)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            changeToolMode(player, mainHandItem);
            return  InteractionResult.SUCCESS;
        }

        ItemStack offSetHandItem = player.getOffhandItem();
        if (offSetHandItem.isEmpty() || !offSetHandItem.is(AnimalFinderModTags.Items.ANIMAL_FINDER_TOOL)) {
            executeAnimalSearch(context, player, mainHandItem);
        }
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack item, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        return executeSlotAttribution(player, entity);
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


    /* Actions */

    private InteractionResult executeSlotAttribution(Player player, LivingEntity entity) {
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        List<String> registeredEntities = getRegisteredEntities(mainHandItem);
        if (registeredEntities.size() >= maxSlots) {
            sendMessage(player,"animalfinder.tool.message.full_index");
            return InteractionResult.PASS;
        }

        if (entity.getType().is(ForgeExtraModTags.EntityTypes.ANIMALS)) {
            addEntityToSlottedAnimals(entity.getType().toString(), player, mainHandItem, registeredEntities);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void changeToolMode(Player player, ItemStack mainHandItem) {
        CompoundTag tagCompound = mainHandItem.getOrCreateTag();

        String modeBeforeChange = getSearchMode(mainHandItem);

        List<String> registeredEntities = getRegisteredEntities(mainHandItem);
        
        if(registeredEntities.size() > 1){
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


    private void addEntityToSlottedAnimals(String entity, Player player, ItemStack item, List<String> registeredEntities) {
        if (entity != null && !registeredEntities.contains(entity)) {
            registeredEntities.add(entity);
            setRegisteredEntities(item, registeredEntities);
            sendMessage(player,  "animalfinder.tool.message.animal_added_to_index", I18n.get(entity));
        }
    }

    private void executeAnimalSearch(UseOnContext context, Player player, ItemStack mainHandItem) {
        List<String> registeredEntities = getRegisteredEntities(mainHandItem);

        if (registeredEntities.isEmpty()) {
            sendMessage(player, "animalfinder.tool.message.empty_index");
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
        List<Entity> validEntities = new ArrayList<Entity>();

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

    private void sendMessage(Player player, String messageKey, Object... pArgs) {
        if (player != null ) {
            player.sendSystemMessage(Component.translatable(messageKey, pArgs));
        }
    }

    private String formatVectorForMessage(Vec3 vector) {
        return String.format("(X: %.0f, Y: %.0f, Z: %.0f)", vector.x, vector.y, vector.z);
    }

    private String getTranslatedEntitiesName(List<String> entities) {
        List<String> entitiesName = new ArrayList<String>();

        for(String entity: entities) {
            entitiesName.add(I18n.get(entity));
        }

        return  String.join(", ", entitiesName);
    }
}
