package moe.plushie.armourers_workshop.api.skin.geometry;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.math.ITransformf;

public interface ISkinGeometryFace {

    /**
     * Gets the geometry type.
     */
    ISkinGeometryType getType();

    /**
     * Gets the face transform.
     */
    ITransformf getTransform();

    /**
     * Get the face used texture key.
     */
    ITextureKey getTextureKey();

    /**
     * Gets the all vertices of the face.
     */
    Iterable<? extends ISkinGeometryVertex> getVertices();
}
