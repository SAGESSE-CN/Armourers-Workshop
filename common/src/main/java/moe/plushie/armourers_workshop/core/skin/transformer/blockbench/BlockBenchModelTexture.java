package moe.plushie.armourers_workshop.core.skin.transformer.blockbench;

import io.netty.buffer.Unpooled;
import moe.plushie.armourers_workshop.core.skin.transformer.bedrock.BedrockModelCube;
import moe.plushie.armourers_workshop.core.skin.transformer.bedrock.BedrockModelTexture;
import moe.plushie.armourers_workshop.core.texture.TextureAnimation;
import moe.plushie.armourers_workshop.core.texture.TextureData;
import moe.plushie.armourers_workshop.utils.MathUtils;
import moe.plushie.armourers_workshop.utils.math.Size2f;
import net.minecraft.core.Direction;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BlockBenchModelTexture extends BedrockModelTexture {

    private final Size2f resolution;
    private final List<BlockBenchTexture> inputs;
    private final HashMap<Integer, TextureData> allTexture = new HashMap<>();
    private final HashMap<String, TextureData> loadedTextures = new HashMap<>();

    protected TextureData defaultTextureData;

    public BlockBenchModelTexture(Size2f resolution, List<BlockBenchTexture> textureInputs) {
        this.resolution = resolution;
        this.inputs = textureInputs;
    }

    public void load(HashSet<Integer> usedTextureIds) throws IOException {
        if (defaultTextureData != null) {
            return;
        }
        for (var textureId : usedTextureIds) {
            // ignore invalid textures.
            if (textureId < 0 || textureId >= inputs.size()) {
                continue;
            }
            var texture = inputs.get(textureId);
            var data = loadTextureData(texture);
            allTexture.put(textureId, data);
            if (defaultTextureData == null) {
                defaultTextureData = data;
            }
            // some models only support single texture, so load additional textures by special file names.
            var additionalTexture = getAdditionalTexture(texture);
            if (additionalTexture != null) {
                var variant = loadTextureData(additionalTexture);
                variant.getProperties().setEmissive(true);
                data.setVariants(Collections.singleton(variant));
            }
        }
        textureData = defaultTextureData;
        if (textureData == null) {
            throw new IOException("error.bb.loadModel.noTexture");
        }
    }

    @Override
    protected TextureData getTextureData(BedrockModelCube cube) {
        if (cube.getUV() instanceof BlockBenchModelUV uv1) {
            return allTexture.get(uv1.getDefaultTextureId());
        }
        return null;
    }

    @Override
    protected TextureData getTextureData(BedrockModelCube cube, Direction dir) {
        if (cube.getUV() instanceof BlockBenchModelUV uv1) {
            return allTexture.get(uv1.getTextureId(dir));
        }
        return null;
    }

    private BlockBenchTexture getAdditionalTexture(BlockBenchTexture texture) {
        var name = texture.getName();
        for (var input : inputs) {
            var target = input.getName();
            if (name.length() < target.length() && name.equals(target.replaceAll("(?i)_s", ""))) {
                return input;
            }
        }
        return null;
    }

    private TextureData loadTextureData(BlockBenchTexture texture) throws IOException {
        var textureData = loadedTextures.get(texture.getUUID());
        if (textureData != null) {
            return textureData;
        }
        var str = texture.getSource();
        var parts = str.split(";base64,");
        if (parts.length != 2) {
            throw new IOException("error.bb.loadModel.textureNotSupported");
        }
        var imageBytes = Base64.getDecoder().decode(parts[1]);
        var imageFrame = resolveTextureFrame(texture, imageBytes);
        var size = resolveTextureSize(texture, imageFrame);
        var animation = resolveTextureAnimation(texture, imageFrame);
        var properties = texture.getProperties();
        textureData = new TextureData(texture.getName(), size.getWidth(), size.getHeight(), animation, properties);
        textureData.load(Unpooled.wrappedBuffer(imageBytes));
        loadedTextures.put(texture.getUUID(), textureData);
        return textureData;
    }

    private int resolveTextureFrame(BlockBenchTexture texture, byte[] imageBytes) throws IOException {
        // in new version block bench provides image size.
        var imageSize = texture.getImageSize();
        if (imageSize == null) {
            var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            imageSize = new Size2f(image.getWidth(), image.getHeight());
        }
        var width = MathUtils.floor(imageSize.getWidth());
        var height = MathUtils.floor(imageSize.getHeight());
        var frame = height / width;
        if (frame * width == height) {
            return frame;
        }
        return 0;
    }

    private Size2f resolveTextureSize(BlockBenchTexture texture, int frameCount) {
        var width = resolution.getWidth();
        var height = resolution.getHeight();
        // in new version block bench provides texture size.
        if (texture.getTextureSize() != null) {
            width = texture.getTextureSize().getWidth();
            height = texture.getTextureSize().getHeight();
        }
        if (frameCount > 1) {
            height = width * frameCount;
        }
        return new Size2f(width, height);
    }

    private TextureAnimation resolveTextureAnimation(BlockBenchTexture texture, int frameCount) {
        if (frameCount > 1) {
            var time = texture.getFrameTime() * 50;
            var interpolate = texture.getFrameInterpolate();
            var mode = texture.getFrameMode();
            return new TextureAnimation(time, frameCount, mode, interpolate);
        }
        return TextureAnimation.EMPTY;
    }
}
