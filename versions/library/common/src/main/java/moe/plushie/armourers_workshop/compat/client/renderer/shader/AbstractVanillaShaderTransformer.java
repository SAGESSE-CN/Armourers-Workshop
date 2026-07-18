package moe.plushie.armourers_workshop.compat.client.renderer.shader;

import moe.plushie.armourers_workshop.compat.core.AbstractResourceTransformer;
import moe.plushie.armourers_workshop.core.client.shader.Shader;
import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;

import java.util.function.Function;

public class AbstractVanillaShaderTransformer implements AbstractResourceTransformer {

    private final int version;

    public AbstractVanillaShaderTransformer(int version) {
        this.version = version;
    }

    @Override
    public Function<String, String> withString(OpenResourceKey key) {
        // only apply the vanilla shaders with a withe list.
        if (!AbstractDefaultShaderSelector.DEFAULT.contains(key)) {
            return null;
        }
        // get the shader type from the file entry.
        var type = getType(key.toString());
        if (type == null) {
            return null;
        }
        return source -> Shader.patch(source, type, version);
    }

    protected String getType(String registryName) {
        if (registryName.endsWith(".vsh")) {
            return "vanilla/vertex";
        }
        if (registryName.endsWith(".gsh")) {
            return "vanilla/geometry";
        }
        if (registryName.endsWith(".fsh")) {
            return "vanilla/fragment";
        }
        return null;
    }
}
