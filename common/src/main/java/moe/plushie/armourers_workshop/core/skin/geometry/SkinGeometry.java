package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometry;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.math.OpenVoxelShape;

import java.util.Collections;

public abstract class SkinGeometry implements ISkinGeometry {

    protected SkinTransform transform = SkinTransform.IDENTITY;

    @Override
    public SkinTransform getTransform() {
        return transform;
    }

    @Override
    public abstract OpenVoxelShape getShape();

    @Override
    public Iterable<? extends SkinGeometryFace> getFaces() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return ObjectUtils.makeDescription(this, "type", getType(), "shape", getShape().bounds());
    }
}
