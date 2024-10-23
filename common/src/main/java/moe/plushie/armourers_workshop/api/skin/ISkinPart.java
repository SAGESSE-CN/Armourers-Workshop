package moe.plushie.armourers_workshop.api.skin;

import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometrySet;

import java.util.Collection;

public interface ISkinPart {

    /**
     * Gets the part type.
     */
    ISkinPartType getType();

    /**
     * Gets the transform.
     */
    ISkinTransform getTransform();

    /**
     * Gets the geometry set.
     */
    ISkinGeometrySet<?> getGeometries();

    /**
     * Gets the children.
     */
    Collection<? extends ISkinPart> getChildren();

    /**
     * Gets the markers.
     */
    Collection<? extends ISkinMarker> getMarkers();
}
