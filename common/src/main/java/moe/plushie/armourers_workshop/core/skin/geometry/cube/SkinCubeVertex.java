package moe.plushie.armourers_workshop.core.skin.geometry.cube;

import moe.plushie.armourers_workshop.api.math.IRectangle3f;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.minecraft.core.Direction;

public class SkinCubeVertex extends SkinGeometryVertex {

    private final SkinCubeFace face;

    public SkinCubeVertex(SkinCubeFace face, Vector3f position, Vector3f normal, Vector2f textureCoords, Color color) {
        this.face = face;
        this.position = position;
        this.normal = normal;
        this.textureCoords = textureCoords;
        this.color = color;
    }

    public IRectangle3f getBoundingBox() {
        return face.getBoundingBox();
    }

    public Direction getDirection() {
        return face.getDirection();
    }
}
