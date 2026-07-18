package moe.plushie.armourers_workshop.compat.client.renderer.shader;

import moe.plushie.armourers_workshop.compat.core.AbstractResourceTransformer;
import moe.plushie.armourers_workshop.core.client.shader.Shader;
import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;

import java.util.function.Function;

public class AbstractIrisShaderTransformer implements AbstractResourceTransformer {

    private final int version;

    public AbstractIrisShaderTransformer(int version) {
        this.version = version;
    }

    @Override
    public Function<String, String> withString(OpenResourceKey key) {
        // get the shader type from the file entry.
        var type = getType(key.toString());
        if (type == null) {
            return null;
        }
        return source -> Shader.patch(source, type, version);
    }

    protected String getType(String registryName) {
        if (registryName.endsWith(".vsh")) {
            return "iris/vertex";
        }
        if (registryName.endsWith(".gsh")) {
            return "iris/geometry";
        }
        if (registryName.endsWith(".fsh")) {
            return "iris/fragment";
        }
        return null;
    }
}
