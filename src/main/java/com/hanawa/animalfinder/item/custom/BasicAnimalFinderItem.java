package com.hanawa.animalfinder.item.custom;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicAnimalFinderItem extends AnimalFinderItem {
    public BasicAnimalFinderItem(Properties pProperties) { super(pProperties, 32, 1); }
}
