package moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.geometry.cube.SkinCube;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkGeometrySlice;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.ChunkGeometrySerializer;
import moe.plushie.armourers_workshop.utils.DirectionUtils;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Vector3i;
import net.minecraft.core.Direction;

import java.io.IOException;
import java.util.LinkedHashMap;

public class ChunkGeometrySerializerV1 extends ChunkGeometrySerializer {

    @Override
    public int stride(ISkinGeometryType geometryType, int options, ChunkPaletteData palette) {
        int faceCount = options & 0x0F;
        return Decoder.calcStride(palette.getColorIndexBytes(), faceCount);
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

        private final ISkinGeometryType type;
        private final ChunkGeometrySlice slice;
        private final ChunkPaletteData palette;

        public Decoder(ISkinGeometryType type, ChunkGeometrySlice slice) {
            this.type = type;
            this.palette = slice.getPalette();
            this.slice = slice;
            this.faceCount = slice.getGeometryOptions() & 0x0F;
        }

        public static int calcStride(int usedBytes, int size) {
            // x/y/z (face + color ref) * faceCount
            return 3 + (1 + usedBytes) * size;
        }

        @Override
        public SkinCube begin() {
            return this;
        }

        @Override
        public ISkinGeometryType getType() {
            return type;
        }

        @Override
        public Rectangle3f getBoundingBox() {
            if (slice.once(0)) {
                float x = slice.getByte(0);
                float y = slice.getByte(1);
                float z = slice.getByte(2);
                boundingBox = new Rectangle3f(x, y, z, 1, 1, 1);
            }
            return boundingBox;
        }

        @Override
        public IPaintColor getPaintColor(Direction dir) {
            if (slice.once(1)) {
                parseColors();
            }
            return super.getPaintColor(dir);
        }

        @Override
        public ITextureKey getTexture(Direction dir) {
            return null;
        }

        protected void parseColors() {
            int usedBytes = palette.getColorIndexBytes();
            for (int i = 0; i < faceCount; ++i) {
                int face = slice.getByte(calcStride(usedBytes, i));
                var color = slice.getColor(calcStride(usedBytes, i) + 1);
                for (var dir1 : DirectionUtils.valuesFromSet(face)) {
                    paintColors.put(dir1, color);
                }
            }
        }
    }

    protected static class Encoder implements ChunkGeometrySerializer.Encoder<SkinCube> {

        private Vector3i pos = Vector3i.ZERO;
        private final LinkedHashMap<IPaintColor, Integer> values = new LinkedHashMap<>();

        @Override
        public int begin(SkinCube cube) {
            // merge all values
            pos = cube.getBlockPos();
            for (var dir : Direction.values()) {
                var value = cube.getPaintColor(dir);
                int face = values.getOrDefault(value, 0);
                face |= 1 << dir.get3DDataValue();
                values.put(value, face);
            }
            return values.size();
        }

        @Override
        public void end(ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
            // position(3B)
            stream.writeByte(pos.getX());
            stream.writeByte(pos.getY());
            stream.writeByte(pos.getZ());

            // face: <color ref>
            for (var entry : values.entrySet()) {
                stream.writeByte(entry.getValue());
                stream.writeVariable(palette.writeColor(entry.getKey()));
            }

            values.clear();
        }
    }
}
