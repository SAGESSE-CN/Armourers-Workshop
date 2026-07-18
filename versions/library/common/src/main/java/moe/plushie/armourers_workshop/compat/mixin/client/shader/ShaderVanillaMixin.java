package moe.plushie.armourers_workshop.compat.mixin.client.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.compat.client.AbstractClientHooks;
import moe.plushie.armourers_workshop.compat.client.renderer.shader.AbstractVanillaShaderTransformer;
import moe.plushie.armourers_workshop.compat.core.AbstractResourceProvider;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Available("[18, 26)")
@Mixin(ShaderInstance.class)
public abstract class ShaderVanillaMixin {

    @ModifyVariable(method = "<init>", at = @At(value = "HEAD"), argsOnly = true)
    private static ResourceProvider aw2$createVanillaShader(ResourceProvider provider, ResourceProvider arg2, String arg3, VertexFormat arg4) {
        AbstractClientHooks.createShaders();
        // this is a iris shader resource?
        if (provider instanceof AbstractResourceProvider) {
            return provider;
        }
        // this is a vanilla shader resource.
        return new AbstractResourceProvider(provider, new AbstractVanillaShaderTransformer(2));
    }
}
