package moe.plushie.armourers_workshop.core;

import moe.plushie.armourers_workshop.core.skin.SkinLoader;
import moe.plushie.armourers_workshop.core.skin.data.SkinDescriptor;
import moe.plushie.armourers_workshop.core.utils.SkinSlotType;
import moe.plushie.armourers_workshop.core.wardrobe.SkinWardrobe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class AWCore {

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(getModId(), path);
    }

    public static String getModId() {
        return "armourers_workshop";
    }

    public static void init() {
    }


    public static ResourceLocation getSlotIcon(String name) {
        return AWCore.resource("textures/items/slot/" + name + ".png");
    }

    public static ResourceLocation getItemIcon(String name) {
        return AWCore.resource("textures/items/template/" + name + ".png");
    }

    public static ItemStack getSkinFromEquipment(@Nullable Entity entity, SkinSlotType skinSlotType, EquipmentSlotType equipmentSlotType) {
        ItemStack itemStack = ItemStack.EMPTY;
        if (entity instanceof LivingEntity) {
            itemStack = ((LivingEntity) entity).getItemBySlot(equipmentSlotType);
        }
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        // embedded skin is the highest priority
        SkinDescriptor descriptor = SkinDescriptor.of(itemStack);
        if (descriptor.accept(itemStack)) {
            return itemStack;
        }
        SkinWardrobe wardrobe = SkinWardrobe.of(entity);
        if (wardrobe != null) {
            ItemStack itemStack1 = wardrobe.getItem(skinSlotType, 0);
            descriptor = SkinDescriptor.of(itemStack1);
            if (descriptor.accept(itemStack)) {
                return itemStack1;
            }
        }
        return itemStack;
    }

}