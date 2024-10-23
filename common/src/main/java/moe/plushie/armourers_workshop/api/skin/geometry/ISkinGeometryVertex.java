package moe.plushie.armourers_workshop.api.skin.geometry;

import moe.plushie.armourers_workshop.api.math.IVector2f;
import moe.plushie.armourers_workshop.api.math.IVector3f;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;

public interface ISkinGeometryVertex {

    int getId();

    float getWeight();

    IVector3f getPosition();

    IVector3f getNormal();

    IVector2f getTextureCoords();

    IPaintColor getColor();
}
