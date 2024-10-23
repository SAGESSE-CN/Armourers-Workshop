package moe.plushie.armourers_workshop.core.skin.geometry.mesh;

import com.google.common.collect.Lists;
import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometry;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.utils.math.OpenVoxelShape;

import java.util.ArrayList;
import java.util.List;

public abstract class SkinMesh extends SkinGeometry {

    protected ITextureKey textureKey;

    public ITextureKey getTextureKey() {
        return textureKey;
    }

    @Override
    public ISkinGeometryType getType() {
        return SkinGeometryTypes.MESH;
    }

    @Override
    public OpenVoxelShape getShape() {
        var shape = new OpenVoxelShape();
        getVertices().forEach(it -> shape.add(it.getPosition()));
        return OpenVoxelShape.box(shape.bounds());
    }

    @Override
    public List<SkinMeshFace> getFaces() {
        var transform = getTransform();
        var textureKey = getTextureKey();
        var allVertices = Lists.newArrayList(getVertices());

        var faces = new ArrayList<SkinMeshFace>();
        var faceCount = allVertices.size() / 3;
        for (int i = 0; i < faceCount; i++) {
            var faceVertices = new ArrayList<SkinGeometryVertex>();
            faceVertices.add(allVertices.get(i * 3));
            faceVertices.add(allVertices.get(i * 3 + 1));
            faceVertices.add(allVertices.get(i * 3 + 2));
            faces.add(new SkinMeshFace(transform, textureKey, faceVertices));
        }
        return faces;
    }

    public abstract Iterable<? extends SkinGeometryVertex> getVertices();

}
