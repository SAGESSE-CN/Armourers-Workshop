package moe.plushie.armourers_workshop.compat.core;

import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;
import moe.plushie.armourers_workshop.core.utils.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public interface AbstractResourceTransformer {

    default Function<String, String> withString(OpenResourceKey key) {
        return null;
    }

    default Function<InputStream, InputStream> withStream(OpenResourceKey key) {
        var transformer = withString(key);
        if (transformer == null) {
            return null;
        }
        return inputStream -> {
            try {
                var source = StreamUtils.readStreamToString(inputStream, StandardCharsets.UTF_8);
                source = transformer.apply(source);
                return new ByteArrayInputStream(source.getBytes());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return inputStream;
        };
    }
}
