package moe.plushie.armourers_workshop.api.skin.geometry;

import moe.plushie.armourers_workshop.api.math.ITransformf;
import moe.plushie.armourers_workshop.api.math.IVoxelShape;

public interface ISkinGeometry {

    /**
     * Gets the geometry type.
     */
    ISkinGeometryType getType();

    /**
     * Gets the geometry transform.
     */
    ITransformf getTransform();

    /**
     * Gets the geometry shape.
     */
    IVoxelShape getShape();

    /**
     * Gets the geometry all faces.
     */
    Iterable<? extends ISkinGeometryFace> getFaces();
}
