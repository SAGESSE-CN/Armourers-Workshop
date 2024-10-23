package moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry;

import com.google.common.collect.ImmutableMap;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import moe.plushie.armourers_workshop.core.skin.serializer.SkinFileOptions;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkContext;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkGeometrySlice;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl.ChunkGeometrySerializerV1;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl.ChunkGeometrySerializerV2;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.geometry.impl.ChunkGeometrySerializerV3;
import moe.plushie.armourers_workshop.utils.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ChunkGeometrySerializers {

    private static final Map<ISkinGeometryType, ChunkGeometrySerializer> SERIALIZERS = ImmutableMap.<ISkinGeometryType, ChunkGeometrySerializer>builder()
            .put(SkinGeometryTypes.BLOCK_SOLID, new ChunkGeometrySerializerV1())
            .put(SkinGeometryTypes.BLOCK_GLOWING, new ChunkGeometrySerializerV1())
            .put(SkinGeometryTypes.BLOCK_GLASS, new ChunkGeometrySerializerV1())
            .put(SkinGeometryTypes.BLOCK_GLASS_GLOWING, new ChunkGeometrySerializerV1())
            .put(SkinGeometryTypes.CUBE, new ChunkGeometrySerializerV2())
            .put(SkinGeometryTypes.MESH, new ChunkGeometrySerializerV3())
            .build();

    public static ChunkGeometrySerializer getSerializer(ISkinGeometryType geometryType) {
        return SERIALIZERS.get(geometryType);
    }

    public static int getStride(ISkinGeometryType geometryType, int options, ChunkPaletteData palette) {
        return getSerializer(geometryType).stride(geometryType, options, palette);
    }

    public static ChunkGeometrySerializer.Encoder<?> createEncoder(ISkinGeometryType geometryType) {
        return getSerializer(geometryType).encoder(geometryType);
    }

    public static ChunkGeometrySerializer.Decoder<?> createDecoder(ISkinGeometryType geometryType, ChunkGeometrySlice slice) {
        return getSerializer(geometryType).decoder(geometryType, slice);
    }

    public static ChunkContext createEncodeContext(Skin skin, SkinFileOptions options) {
        var context = new ChunkContext(options);
        context.setFastEncoder(canFastEncoding(skin.getId(), skin.getParts()));
        return context;
    }

    public static ChunkContext createDecodeContext(SkinFileOptions options) {
        return new ChunkContext(options);
    }

    public static boolean canFastEncoding(int skinOwner, List<SkinPart> parts) {
        // when the skin have multiple data owner, we can't enable fast encoder,
        // because it must to recompile and resort it.
        var owners = new HashSet<>();
        owners.add(skinOwner);
        ObjectUtils.eachTree(parts, SkinPart::getChildren, part -> {
            var geometries = part.getGeometries();
            if (!geometries.isEmpty()) {
                owners.add(geometries.getId());
            }
        });
        return owners.size() <= 1;
    }
}
