package com.hanawa.animalfinder.item.custom;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalFinderItem extends Item {

    private int distance = 32;
    private int maxSlots = 1;
    private final List<String> slottedEntities;
    private final Map<String, String> itemSlotEntityMap = new HashMap<String, String>() {
        {
            put("item.minecraft.porkchop", "entity.minecraft.pig");
            put("item.minecraft.feather", "entity.minecraft.chicken");
        }
    };

    public AnimalFinderItem(Properties pProperties, int distance, int maxSlots) {
        super(pProperties);
        this.distance = distance;
        this.maxSlots = maxSlots;
        this.slottedEntities = new ArrayList<>();
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        executeAnimalSearch(pContext);
        executeSlotAttribution(pContext);

        return InteractionResult.SUCCESS;
    }

    private void executeSlotAttribution(UseOnContext pContext) {

        Player player = pContext.getPlayer();
        if (player == null || slottedEntities.size() >= maxSlots) {
            return;
        }

        ItemStack secondaryItem = player.getOffhandItem();

        String entity = itemSlotEntityMap.get(secondaryItem.getDescriptionId());

        if (entity != null && !slottedEntities.contains(entity)) {
            sendMessage(player, "Added animal to search index: " + entity);

            slottedEntities.add(entity);
        }
    }


    private void executeAnimalSearch(UseOnContext pContext) {
        List<Entity> entitiesFound = getEntities(pContext);
        if (entitiesFound == null) {
            return;
        }

        List<Entity> animalsFound = filterValidEntities(entitiesFound);

        Player player = pContext.getPlayer();

        notifyUserAboutEntitiesFound(animalsFound, player);
    }

    private List<Entity> getEntities(UseOnContext pContext) {
        if(!pContext.getLevel().isClientSide()) {
            BlockPos positionClicked = pContext.getClickedPos();


            AABB area = new AABB(
                    positionClicked.getX() - distance, positionClicked.getY() - distance, positionClicked.getZ() - distance,
                    positionClicked.getX() + distance, positionClicked.getY() + distance, positionClicked.getZ() + distance);

           return pContext.getLevel().getEntities(null, area);
        }

        return null;
    }

    private List<Entity> filterValidEntities(List<Entity> entities) {
        List<Entity> validEntities = new ArrayList<Entity>();

        for(Entity entity: entities) {
            if (slottedEntities.contains(entity.getType().toString())) {
                validEntities.add(entity);
            }
        }

        return validEntities;
    }

    private void notifyUserAboutEntitiesFound(List<Entity> entities, Player player ) {
        for(Entity entity: entities) {
            String animalName = entity.getType().getDescriptionId();

            if (I18n.exists(animalName)) {
                animalName = I18n.get(animalName);
            }

            sendMessage(player,animalName + " found at " + formatVectorForMessage(entity.trackingPosition()));
        }

        if (entities.isEmpty()) {
            sendMessage(player, "No animals found!");
        };
    }

    private void sendMessage(Player player, String message) {
        if (player != null ) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    private String formatVectorForMessage(Vec3 vector) {
        return String.format("X: %.0f,Y: %.0f,Z: %.0f", vector.x, vector.y, vector.z);
    }

    public Map<String, String> getItemSlotEntityMap() {
        return itemSlotEntityMap;
    }
}
