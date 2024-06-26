package moe.plushie.armourers_workshop.init.mixin.forge;

import moe.plushie.armourers_workshop.init.client.ClientMenuHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.shedaniel.rei.RoughlyEnoughItemsCoreClient")
public class ForgeScreenREIMixin {

    // https://github.com/shedaniel/RoughlyEnoughItems/blob/6.x/runtime/src/main/java/me/shedaniel/rei/RoughlyEnoughItemsCoreClient.java#L285
    @Inject(method = "shouldReturn", at = @At("HEAD"), cancellable = true, remap = false)
    private static void aw2$shouldReturn(Screen screen, CallbackInfoReturnable<Boolean> cir) {
        if (!ClientMenuHandler.shouldRenderExtendScreen(screen)) {
            cir.setReturnValue(true);
        }
    }
}
