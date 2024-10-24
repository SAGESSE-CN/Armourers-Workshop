package moe.plushie.armourers_workshop.compatibility.mixin;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.core.client.other.FindableSkinManager;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Available("[1.21, )")
public class ModelBakeryMixin {

    @Mixin(BlockModel.class)
    public static class ModelPatch {

        @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("RETURN"))
        private void aw2$bakeModel(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, CallbackInfoReturnable<BakedModel> cir) {
            var unbakedModel = BlockModel.class.cast(this);
            var bakedModel = cir.getReturnValue();
            FindableSkinManager.getInstance().bakeSkinModel(unbakedModel, bakedModel);
        }
    }

    @Mixin(ModelBakery.class)
    public static class BakeryPatch {

        @Inject(method = "loadBlockModel", at = @At("HEAD"), cancellable = true)
        private void aw2$loadBlockModel(ResourceLocation location, CallbackInfoReturnable<BlockModel> cir) {
            var unbakedModel = FindableSkinManager.getInstance().loadSkinModel(location);
            if (unbakedModel != null) {
                cir.setReturnValue(unbakedModel);
            }
        }
    }
}
