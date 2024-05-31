package com.hanawa.animalfinder.item.custom;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChickenFinderItem extends Item {
    public ChickenFinderItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if(!pContext.getLevel().isClientSide()) {
            BlockPos positionClicked = pContext.getClickedPos();
            Player player = pContext.getPlayer();

            int distance = 32;

            AABB area = new AABB(
            positionClicked.getX() - distance, positionClicked.getY() - distance, positionClicked.getZ() - distance,
            positionClicked.getX() + distance, positionClicked.getY() + distance, positionClicked.getZ() + distance);

            List<Entity> entities = pContext.getLevel().getEntities(null, area);

            for(Entity entity: entities) {
                if (!entity.getType().toString().equals("entity.minecraft.chicken")) {
                    continue;
                }

                String animalName = entity.getType().getDescriptionId();

                if (I18n.exists(animalName)) {
                    animalName = I18n.get(animalName);
                }
                sendMessage(player,animalName + " found at " + entity.trackingPosition());
            }

            if (entities.isEmpty()) {
                sendMessage(player, "No animals found!");
            };
        }

        return InteractionResult.SUCCESS;
    }

    private void sendMessage(Player player, String message) {
        if (player != null ) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
