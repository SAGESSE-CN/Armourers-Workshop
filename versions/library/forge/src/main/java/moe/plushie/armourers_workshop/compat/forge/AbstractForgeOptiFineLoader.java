package moe.plushie.armourers_workshop.compat.forge;

import com.google.common.primitives.Bytes;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.Reflect;
import moe.plushie.armourers_workshop.core.utils.StreamUtils;
import moe.plushie.armourers_workshop.init.ModLog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Available("[16, )")
public class AbstractForgeOptiFineLoader {

    /// String(0x01) + Length(0x00, 0x07) + VERSION(0x56, 0x45, 0x52, 0x53, 0x49, 0x4F, 0x4E) + String(0x01)
    private static final byte[] PATTERN = {0x01, 0x00, 0x07, 0x56, 0x45, 0x52, 0x53, 0x49, 0x4F, 0x4E, 0x01};

    /// Get the optifine version from the environment.
    public static String getVersion() {
        // the optifine will register loader class into environments.
        var clazz = (Class<?>) System.getProperties().get("optifine.OptiFineResourceLocator.class");
        if (clazz == null) {
            return null; // the optifine is not loaded.
        }
        // the optifine maybe has multiple versions implements.
        var configs = Collections.newList("net/optifine/Config.class", "srg/net/optifine/Config.class", "notch/net/optifine/Config.class");
        for (var config : configs) {
            var result = Reflect.of(clazz).invoke("getOptiFineResourceStream", config);
            if (result == null) {
                continue; // can't found the config class, ignore.
            }
            try (var inputStream = (InputStream) result) {
                return readVersion(inputStream);
            } catch (Exception exception) {
                ModLog.error("Can't parse the optifine version from the config class.", exception);
                return null;
            }
        }
        ModLog.error("Can't found the optifine config class.");
        return null;
    }

    ///  Get the optifine version from the byte code.
    private static String readVersion(InputStream inputStream) throws IOException {
        var bytes = StreamUtils.readStreamToByteArray(inputStream);
        var pos = Bytes.indexOf(bytes, PATTERN);
        if (pos == -1) {
            throw new RuntimeException("Can't found the optifine version in the config class.");
        }
        pos += PATTERN.length + 2;
        var length = bytes[pos - 2] << 8 | bytes[pos - 1];
        return new String(bytes, pos, length, StandardCharsets.UTF_8);
    }
}
