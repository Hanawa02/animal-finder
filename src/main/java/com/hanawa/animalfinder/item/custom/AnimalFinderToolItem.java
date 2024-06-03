package com.hanawa.animalfinder.item.custom;


import com.hanawa.animalfinder.util.AnimalFinderModTags;
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
    public final List<String> indexedEntities;

    private final String MODE_ALL = "animalfinder.tool.search_mode.all";

    private final String STORAGE_KEY_MODE = "MODE";
    
    public AnimalFinderToolItem(Properties properties, int distance, int maxSlots ) {
        super(properties.stacksTo(1));
        this.distance = distance;
        this.maxSlots = maxSlots;
        this.indexedEntities = new ArrayList<>();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComnponents, TooltipFlag isAdvanced) {
        String lastComponent = "";
        String firstComponents = "";

        if (indexedEntities.size() > 0) {
            lastComponent = I18n.get(indexedEntities.get(indexedEntities.size() - 1));
        }

        if (indexedEntities.size() > 1) {
            firstComponents = getTranslatedEntitiesName(indexedEntities.subList(0, indexedEntities.size() - 1));
        }

        tooltipComnponents.add(
            Component.translatable(
            "animalfinder.tool.tooltip",
                firstComponents,
                lastComponent
            )
        );
        super.appendHoverText(stack, level, tooltipComnponents, isAdvanced);
    }

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
        } else {
            executeSlotAttributionFromAnotherFinder(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity entity, InteractionHand hand) {
        executeSlotAttribution(playerIn, entity);
        return InteractionResult.SUCCESS;
    }

    private void executeSlotAttribution(Player player, LivingEntity entity) {
        if (player == null) {
            return;
        }

        if (indexedEntities.size() >= maxSlots) {
            sendMessage(player,"animalfinder.tool.message.full_index");
            return;
        }

        if (entity.getType().is(ForgeExtraModTags.EntityTypes.ANIMALS)) {
            addEntityToSlottedAnimals(entity.getType().toString(), player);
        }
    }
    
    private void executeSlotAttributionFromAnotherFinder(Player player) {
        if (player == null || indexedEntities.size() >= maxSlots) {
            return;
        }

        ItemStack offHandItem = player.getOffhandItem();

        if (offHandItem.isEmpty()) {
            return;
        }

        if (offHandItem.is(AnimalFinderModTags.Items.ANIMAL_FINDER_TOOL)) {
            List<String> entities = ((AnimalFinderToolItem) offHandItem.getItem()).indexedEntities;

            for(String entity: entities) {
                if (indexedEntities.size() >= maxSlots) {
                    sendMessage(player, "animalfinder.tool.message.full_index");
                    break;
                }

                addEntityToSlottedAnimals(entity, player);
            }
        }
    }

    private void changeToolMode(Player player, ItemStack mainHandItem) {
        CompoundTag tagCompound = mainHandItem.getTag();
        String modeBeforeChange = "";

        if (tagCompound == null) {
            tagCompound = new CompoundTag();

            tagCompound.putString(STORAGE_KEY_MODE, MODE_ALL);
            modeBeforeChange = MODE_ALL;
        } else {
            modeBeforeChange = tagCompound.getString(STORAGE_KEY_MODE);
        }

        if(indexedEntities.size() > 1){
            String currentMode = tagCompound.getString(STORAGE_KEY_MODE);
            int currentModeIndex = indexedEntities.indexOf(currentMode);


            if (currentModeIndex == indexedEntities.size() - 1) {
                tagCompound.putString(STORAGE_KEY_MODE, MODE_ALL);
            } else {
                tagCompound.putString(STORAGE_KEY_MODE, indexedEntities.get(currentModeIndex + 1));
            }
        }

        mainHandItem.setTag(tagCompound);

        if (!modeBeforeChange.equals(tagCompound.getString(STORAGE_KEY_MODE))) {
            sendMessage(player, "animalfinder.tool.message.search_mode_set", I18n.get(tagCompound.getString(STORAGE_KEY_MODE)));
        }
    }

    private void addEntityToSlottedAnimals(String entity, Player player) {
        if (entity != null && !indexedEntities.contains(entity)) {
            indexedEntities.add(entity);
            sendMessage(player,  "animalfinder.tool.message.animal_added_to_index", I18n.get(entity));
        }
    }

    private void executeAnimalSearch(UseOnContext context, Player player, ItemStack mainHandItem) {
        if (indexedEntities.isEmpty()) {
            sendMessage(player, "animalfinder.tool.message.empty_index");
            return;
        }

        BlockPos searchingPosition = context.getClickedPos();
        sendMessage(
            player,
 "animalfinder.tool.message.searching",
            getTranslatedEntitiesName(indexedEntities),
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

        CompoundTag compoundTag = mainHandItem.getTag();
        String search_mode = compoundTag.getString(STORAGE_KEY_MODE);
        for(Entity entity: entities) {
            String entityType = entity.getType().toString();
            if (search_mode.equals(MODE_ALL) && indexedEntities.contains(entityType)) {
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
        };
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
