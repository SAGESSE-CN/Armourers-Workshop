package moe.plushie.armourers_workshop.core.skin.serializer.v20.coder.v2;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.data.transform.SkinBasicTransform;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkColorSection;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkCubeSection;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkCubeSelector;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.coder.ChunkCubeDecoder;
import moe.plushie.armourers_workshop.utils.DirectionUtils;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import moe.plushie.armourers_workshop.utils.texture.TextureBox;
import moe.plushie.armourers_workshop.utils.texture.TextureKey;
import net.minecraft.core.Direction;

import java.util.BitSet;
import java.util.EnumMap;

import manifold.ext.rt.api.auto;

public class ChunkCubeDecoderV2 extends ChunkCubeDecoder {

    private Rectangle3f shape = Rectangle3f.ZERO;
    private SkinBasicTransform transform = SkinBasicTransform.IDENTITY;

    protected final BitSet flags = new BitSet();
    protected final EnumMap<Direction, Vector2f> startUVs = new EnumMap<>(Direction.class);
    protected final EnumMap<Direction, Vector2f> endUVs = new EnumMap<>(Direction.class);
    protected final EnumMap<Direction, ITextureKey> textureKeys = new EnumMap<>(Direction.class);

    public ChunkCubeDecoderV2(int startIndex, int endIndex, ChunkCubeSelector selector, ChunkCubeSection.Immutable section) {
        super(startIndex, endIndex, selector, section);
    }

    public static int getStride(int options, ChunkPaletteData palette) {
        int faceCount = options & 0x0F;
        return calcStride(palette.getTextureIndexBytes(), faceCount);
    }

    public static int calcStride(int usedBytes, int size) {
        // origin(12B)/size(12B) + translate(12B)/rotation(12B)/scale(12B)/pivot(12B) + (face flag + texture ref) * faceCount;
        return 72 + (1 + usedBytes * 2) * size;
    }

    @Override
    public Rectangle3f getShape() {
        if (setBit(0)) {
            float x = getFloat(0);
            float y = getFloat(4);
            float z = getFloat(8);
            float width = getFloat(12);
            float height = getFloat(16);
            float depth = getFloat(20);
            shape = new Rectangle3f(x, y, z, width, height, depth);
        }
        return shape;
    }

    @Override
    public SkinBasicTransform getTransform() {
        if (setBit(1)) {
            Vector3f translate = getVector3f(24);
            Vector3f rotation = getVector3f(36);
            Vector3f scale = getVector3f(48);
            Vector3f pivot = getVector3f(60);
            transform = SkinBasicTransform.create(translate, rotation, scale, pivot);
        }
        return transform;
    }

    @Override
    public IPaintColor getPaintColor(Direction dir) {
        ITextureKey key = getTexture(dir);
        if (key != null) {
            return PaintColor.WHITE;
        }
        return PaintColor.CLEAR;
    }

    @Override
    public ITextureKey getTexture(Direction dir) {
        if (setBit(2)) {
            parseTextures();
        }
        return textureKeys.get(dir);
    }

    protected void parseTextures() {
        startUVs.clear();
        endUVs.clear();
        textureKeys.clear();
        TextureBox textureBox = null;
        int usedBytes = palette.getTextureIndexBytes();
        for (int i = 0; i < faceCount; ++i) {
            int face = getByte(calcStride(usedBytes, i));
            auto pos = ChunkColorSection.TextureRef.readFromStream(usedBytes, readerIndex + calcStride(usedBytes, i) + 1, bytes);
            for (Direction dir : DirectionUtils.valuesFromSet(face)) {
                endUVs.put(dir, pos);
                if (!startUVs.containsKey(dir)) {
                    startUVs.put(dir, pos);
                }
            }
            if ((face & 0x80) != 0) {
                auto ref = palette.readTexture(pos);
                if (ref == null) {
                    continue;
                }
                auto shape = getShape();
                float width = shape.getWidth();
                float height = shape.getHeight();
                float depth = shape.getDepth();
                textureBox = new TextureBox(width, height, depth, false, ref.getPos(), ref.getProvider());
            }
        }
        for (Direction dir : Direction.values()) {
            Vector2f start = startUVs.get(dir);
            Vector2f end = endUVs.get(dir);
            if (start != null && end != null) {
                auto ref = palette.readTexture(start);
                if (ref == null) {
                    continue;
                }
                float u = ref.getU();
                float v = ref.getV();
                float width = end.getX() - start.getX();
                float height = end.getY() - start.getY();
                textureKeys.put(dir, new TextureKey(u, v, width, height, ref.getProvider()));
            } else if (textureBox != null) {
                textureKeys.put(dir, textureBox.getTexture(dir));
            }
        }
    }

    @Override
    protected void reset() {
        flags.clear();
    }

    protected boolean setBit(int index) {
        if (flags.get(index)) {
            return false;
        }
        flags.set(index);
        return true;
    }
}
