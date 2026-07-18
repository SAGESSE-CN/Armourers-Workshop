package moe.plushie.armourers_workshop.core.client.shader;

public class Shader {

    /**
     * Apply the shader source patch.
     * <code>
     * vanilla/geometry
     * vanilla/vertex
     * vanilla/fragment
     * iris/geometry
     * iris/vertex
     * iris/fragment
     * optifine/geometry
     * optifine/vertex
     * optifine/fragment
     * </code>
     */
    public static String patch(String source, String type, int version) {
        // the preprocessor requires the source must not be null or empty.
        if (source == null || source.isEmpty()) {
            return source;
        }
        var preprocessor = ShaderPreprocessor.create(type, version);
        if (preprocessor != null) {
            return preprocessor.process(source);
        }
        return source; // ignore.
    }
}
