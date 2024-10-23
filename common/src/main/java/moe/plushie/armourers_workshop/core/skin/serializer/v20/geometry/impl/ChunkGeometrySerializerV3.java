package moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl;

import com.google.common.collect.Lists;
import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.common.ITextureProvider;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.core.skin.geometry.mesh.SkinMesh;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkGeometrySlice;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.ChunkGeometrySerializer;
import moe.plushie.armourers_workshop.core.texture.TextureKey;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChunkGeometrySerializerV3 extends ChunkGeometrySerializer {

    @Override
    public int stride(ISkinGeometryType geometryType, int options, ChunkPaletteData palette) {
        return Decoder.calcStride(palette.getTextureIndexBytes(), options);
    }

    @Override
    public ChunkGeometrySerializer.Encoder<?> encoder(ISkinGeometryType geometryType) {
        return new Encoder();
    }

    @Override
    public ChunkGeometrySerializer.Decoder<?> decoder(ISkinGeometryType geometryType, ChunkGeometrySlice slice) {
        return new Decoder(geometryType, slice);
    }

    protected static class Decoder extends SkinMesh implements ChunkGeometrySerializer.Decoder<SkinMesh> {

        private final int vertexCount;

        private final ChunkGeometrySlice slice;
        private final ChunkPaletteData palette;

        private final ArrayList<SkinGeometryVertex> vertices = new ArrayList<>();

        private ITextureProvider textureProvider;

        public Decoder(ISkinGeometryType type, ChunkGeometrySlice slice) {
            this.palette = slice.getPalette();
            this.slice = slice;
            this.vertexCount = slice.getGeometryOptions();
        }

        public static int calcStride(int usedBytes, int size) {
            // header(128B) + (id(4B) + vertex(12B) + normal(12B) + uv(VB) + weight(4B)) * size
            return 128 + (4 + Vector3f.BYTES + Vector3f.BYTES + usedBytes * 2 + 4) * size;
        }

        @Override
        public SkinMesh begin() {
            return this;
        }

        @Override
        public SkinTransform getTransform() {
            if (slice.once(0)) {
                transform = slice.getTransform(4);
            }
            return super.getTransform();
        }

        @Override
        public ITextureKey getTextureKey() {
            if (slice.once(1)) {
                var ignored = getVertices();
                textureKey = new TextureKey(0, 0, 0, 0, textureProvider);
            }
            return textureKey;
        }

        @Override
        public List<SkinGeometryVertex> getVertices() {
            if (slice.once(2)) {
                parseVertices();
            }
            return vertices;
        }


        protected void parseVertices() {
            vertices.clear();
            int usedBytes = palette.getTextureIndexBytes();
            for (int i = 0; i < vertexCount; i++) {
                vertices.add(parseVertex(i, usedBytes));
            }
        }

        protected SkinGeometryVertex parseVertex(int i, int usedBytes) {
            int offset = calcStride(usedBytes, i);

            var id = slice.getInt(offset);
            var position = slice.getVector3f(offset + 4);
            var normal = slice.getVector3f(offset + 16);
            var textureCoords = slice.getTexturePos(offset + 28);
            var weight = slice.getFloat(offset + 36);

            // fix texture location.
            textureCoords = parseTextureCoords(textureCoords);

            return new SkinGeometryVertex(id, weight, position, normal, textureCoords);
        }

        protected Vector2f parseTextureCoords(Vector2f uv) {
            var ref = palette.readTexture(uv);
            textureProvider = ref.getProvider();
            return ref.getPos();
        }
    }

    protected static class Encoder implements ChunkGeometrySerializer.Encoder<SkinMesh> {

        private SkinTransform transform = SkinTransform.IDENTITY;
        private ITextureKey textureKey;
        private ArrayList<? extends SkinGeometryVertex> vertices;
        private final byte[] reserved = new byte[60];

        @Override
        public int begin(SkinMesh mesh) {
            transform = mesh.getTransform();
            textureKey = mesh.getTextureKey();
            vertices = Lists.newArrayList(mesh.getVertices());
            return vertices.size();
        }

        @Override
        public void end(ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
            // type(4b) + transform(64b) + reserved(60B)
            stream.writeInt(0);
            stream.writeTransformf(transform);
            stream.write(reserved);
            // vertices: id(4B) + vertex(12B) + normal(12B) + uv(VB) + weight(4B)
            for (var vertex : vertices) {
                stream.writeInt(vertex.getId());
                stream.writeVector3f(vertex.getPosition());
                stream.writeVector3f(vertex.getNormal());
                stream.writeVariable(palette.writeTexture(vertex.getTextureCoords(), textureKey.getProvider()));
                stream.writeFloat(vertex.getWeight());
            }
            vertices = null;
            textureKey = null;
        }
    }
}
