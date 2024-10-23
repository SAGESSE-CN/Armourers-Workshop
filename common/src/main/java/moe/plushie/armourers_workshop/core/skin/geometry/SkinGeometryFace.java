package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.math.ITransformf;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryFace;
import moe.plushie.armourers_workshop.core.data.transform.SkinTransform;
import moe.plushie.armourers_workshop.utils.ObjectUtils;

public abstract class SkinGeometryFace implements ISkinGeometryFace {

    protected ITransformf transform = SkinTransform.IDENTITY;
    protected ITextureKey textureKey;

    @Override
    public ITransformf getTransform() {
        return transform;
    }

    @Override
    public ITextureKey getTextureKey() {
        return textureKey;
    }

    @Override
    public abstract Iterable<? extends SkinGeometryVertex> getVertices();

    public float getPriority() {
        return 0;
    }

    public boolean isVisible() {
        return true;
    }

    @Override
    public String toString() {
        return ObjectUtils.makeDescription(this, "type", getType());
    }
}
