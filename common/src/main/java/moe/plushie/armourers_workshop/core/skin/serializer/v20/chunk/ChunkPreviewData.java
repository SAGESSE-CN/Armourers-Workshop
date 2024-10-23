package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import moe.plushie.armourers_workshop.api.skin.ISkinTransform;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometrySet;
import moe.plushie.armourers_workshop.core.texture.SkinPreviewData;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChunkPreviewData {

    private final ChunkGeometryData geometryData;

    public ChunkPreviewData(ChunkGeometryData geometryData) {
        this.geometryData = geometryData;
    }

    public SkinPreviewData readFromStream(ChunkInputStream stream) throws IOException {
        var chunkTransform = new ChunkTransform();
        var sections = new ArrayList<Pair<ISkinTransform, SkinGeometrySet<?>>>();
        while (true) {
            int count = stream.readVarInt();
            if (count == 0) {
                break;
            }
            int id = stream.readVarInt();
            chunkTransform.readFromStream(stream);
            for (int i = 0; i < count; ++i) {
                var geometries = geometryData.readReferenceFromStream(stream);
                sections.add(Pair.of(chunkTransform.build(), geometries));
            }
        }
        return new SkinPreviewData(sections);
    }

    public void writeToStream(SkinPreviewData previewData, ChunkOutputStream stream) throws IOException {
        // freeze and combine the transform/geometries data.
        var sections = new LinkedHashMap<ChunkTransform, ArrayList<SkinGeometrySet<?>>>();
        previewData.forEach((transform, geometries) -> {
            var chunkTransform = ChunkTransform.flat(transform);
            sections.computeIfAbsent(chunkTransform, k -> new ArrayList<>()).add(geometries);
        });
        for (var section : sections.entrySet()) {
            stream.writeVarInt(section.getValue().size());
            stream.writeVarInt(0);
            section.getKey().writeToStream(stream);
            for (var geometries : section.getValue()) {
                geometryData.writeReferenceToStream(geometries, stream);
            }
        }
        stream.writeVarInt(0);
    }
}
