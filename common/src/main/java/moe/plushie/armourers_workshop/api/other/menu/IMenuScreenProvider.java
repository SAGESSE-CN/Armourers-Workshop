package moe.plushie.armourers_workshop.api.other.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface IMenuScreenProvider<T extends AbstractContainerMenu, S> {

    S createMenuScreen(T var1, Inventory var2, Component var3);
}