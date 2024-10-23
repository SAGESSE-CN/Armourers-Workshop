package moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.common.ITextureProvider;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCube;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCubeFace;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOConsumer2;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkGeometrySlice;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.ChunkGeometrySerializer;
import moe.plushie.armourers_workshop.core.texture.TextureBox;
import moe.plushie.armourers_workshop.core.texture.TextureKey;
import moe.plushie.armourers_workshop.core.texture.TextureOptions;
import moe.plushie.armourers_workshop.utils.DirectionUtils;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;

public class ChunkGeometrySerializerV2 extends ChunkGeometrySerializer {

    @Override
    public int stride(ISkinGeometryType geometryType, int options, ChunkPaletteData palette) {
        int faceCount = options & 0x0F;
        return Decoder.calcStride(palette.getTextureIndexBytes(), faceCount);
    }

    @Override
    public ChunkGeometrySerializer.Encoder<?> encoder(ISkinGeometryType geometryType) {
        return new Encoder();
    }

    @Override
    public ChunkGeometrySerializer.Decoder<?> decoder(ISkinGeometryType geometryType, ChunkGeometrySlice slice) {
        return new Decoder(geometryType, slice);
    }

    protected static class Decoder extends SkinCube implements ChunkGeometrySerializer.Decoder<SkinCube> {

        private final int faceCount;

        private final ChunkGeometrySlice slice;
        private final ChunkPaletteData palette;

        private final EnumMap<Direction, Vector2f> startUVs = new EnumMap<>(Direction.class);
        private final EnumMap<Direction, Vector2f> endUVs = new EnumMap<>(Direction.class);
        private final EnumMap<Direction, TextureOptions> optionsValues = new EnumMap<>(Direction.class);
        private final EnumMap<Direction, ITextureKey> textureKeys = new EnumMap<>(Direction.class);

        private SkinTransform transform = SkinTransform.IDENTITY;

        public Decoder(ISkinGeometryType type, ChunkGeometrySlice slice) {
            this.palette = slice.getPalette();
            this.slice = slice;
            this.faceCount = slice.getGeometryOptions() & 0x0F;
        }

        public static int calcStride(int usedBytes, int size) {
            // rectangle(24B) + transform(64b) + (face flag + texture ref) * faceCount;
            return Rectangle3f.BYTES + SkinTransform.BYTES + (1 + usedBytes * 2) * size;
        }

        @Override
        public SkinCube begin() {
            return this;
        }

        @Override
        public ISkinGeometryType getType() {
            return SkinGeometryTypes.CUBE;
        }

        @Override
        public Rectangle3f getBoundingBox() {
            if (slice.once(0)) {
                boundingBox = slice.getRectangle3f(0);
            }
            return boundingBox;
        }

        @Override
        public SkinTransform getTransform() {
            if (slice.once(1)) {
                transform = slice.getTransform(24);
            }
            return transform;
        }

        @Override
        public IPaintColor getPaintColor(Direction dir) {
            var key = getTexture(dir);
            if (key != null) {
                return PaintColor.WHITE;
            }
            return PaintColor.CLEAR;
        }

        @Override
        public ITextureKey getTexture(Direction dir) {
            if (slice.once(2)) {
                parseTextures();
            }
            return textureKeys.get(dir);
        }


        @Override
        public SkinCubeFace getFace(Direction dir) {
            if (getTexture(dir) != null) {
                return super.getFace(dir);
            }
            return null;
        }

        protected void parseTextures() {
            startUVs.clear();
            endUVs.clear();
            optionsValues.clear();
            textureKeys.clear();
            TextureBox textureBox = null;
            int usedBytes = palette.getTextureIndexBytes();
            for (int i = 0; i < faceCount; ++i) {
                int index = calcStride(usedBytes, i);
                int face = slice.getByte(index);
                if ((face & 0x40) != 0) {
                    var opt = slice.getTextureOptions(index + 1);
                    for (var dir : DirectionUtils.valuesFromSet(face)) {
                        optionsValues.put(dir, opt);
                    }
                    continue;
                }
                var pos = slice.getTexturePos(index + 1);
                for (var dir : DirectionUtils.valuesFromSet(face)) {
                    endUVs.put(dir, pos);
                    if (!startUVs.containsKey(dir)) {
                        startUVs.put(dir, pos);
                    }
                }
                if ((face & 0x80) != 0) {
                    var ref = palette.readTexture(pos);
                    if (ref == null) {
                        continue;
                    }
                    var rect = getBoundingBox();
                    float width = rect.getWidth();
                    float height = rect.getHeight();
                    float depth = rect.getDepth();
                    textureBox = new TextureBox(width, height, depth, false, ref.getPos(), ref.getProvider());
                }
            }
            for (var dir : Direction.values()) {
                var start = startUVs.get(dir);
                var end = endUVs.get(dir);
                if (start != null && end != null) {
                    var opt = optionsValues.get(dir);
                    var ref = palette.readTexture(start);
                    if (ref == null) {
                        continue;
                    }
                    float u = ref.getU();
                    float v = ref.getV();
                    float width = end.getX() - start.getX();
                    float height = end.getY() - start.getY();
                    textureKeys.put(dir, new TextureKey(u, v, width, height, opt, ref.getProvider()));
                } else if (textureBox != null) {
                    textureKeys.put(dir, textureBox.getTexture(dir));
                }
            }
        }
    }

    protected static class Encoder implements ChunkGeometrySerializer.Encoder<SkinCube> {

        private Rectangle3f boundingBox = Rectangle3f.ZERO;
        private SkinTransform transform = SkinTransform.IDENTITY;

        private final SortedMap<Vector2f> startValues = new SortedMap<>();
        private final SortedMap<Vector2f> endValues = new SortedMap<>();
        private final SortedMap<TextureOptions> optionsValues = new SortedMap<>();

        @Override
        public int begin(SkinCube geometry) {
            // merge all values
            for (var dir : Direction.values()) {
                var value = geometry.getTexture(dir);
                if (value == null) {
                    continue;
                }
                var provider = value.getProvider();
                var entry = ObjectUtils.safeCast(value, TextureBox.Entry.class);
                if (entry != null) {
                    startValues.put(0x80, entry.getParent(), provider);
                    // box need options?
                    continue;
                }
                int face = 1 << dir.get3DDataValue();
                float u = value.getU();
                float v = value.getV();
                float s = value.getWidth();
                float t = value.getHeight();
                startValues.put(face, new Vector2f(u, v), provider);
                endValues.put(face, new Vector2f(u + s, v + t), provider);
                if (value.getOptions() instanceof TextureOptions textureOptions) {
                    optionsValues.put(face, textureOptions, provider);
                }
            }
            transform = geometry.getTransform();
            boundingBox = geometry.getBoundingBox();
            return startValues.size() + endValues.size() + optionsValues.size();
        }

        @Override
        public void end(ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
            // rectangle(24B) + transform(64b)
            stream.writeRectangle3f(boundingBox);
            stream.writeTransformf(transform);

            // face: <texture ref>
            optionsValues.forEach((key, value) -> {
                stream.writeByte(0x40 | value);
                stream.writeVariable(palette.writeTextureOptions(key.getLeft(), key.getRight()));
            });
            startValues.forEach((key, value) -> {
                stream.writeByte(value);
                stream.writeVariable(palette.writeTexture(key.getLeft(), key.getRight()));
            });
            endValues.forEach((key, value) -> {
                stream.writeByte(value);
                stream.writeVariable(palette.writeTexture(key.getLeft(), key.getRight()));
            });

            startValues.clear();
            endValues.clear();
            optionsValues.clear();
        }
    }

    protected static class SortedMap<T> {

        private final LinkedHashMap<Pair<T, ITextureProvider>, Integer> impl = new LinkedHashMap<>();

        public void forEach(IOConsumer2<Pair<T, ITextureProvider>, Integer> consumer) throws IOException {
            for (var entry : impl.entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }

        public void put(int face, T pos, ITextureProvider provider) {
            var index = Pair.of(pos, provider);
            int newFace = impl.getOrDefault(index, 0);
            newFace |= face;
            impl.put(index, newFace);
        }

        public void clear() {
            impl.clear();
        }

        public int size() {
            return impl.size();
        }
    }
}
