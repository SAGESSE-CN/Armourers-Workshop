package moe.plushie.armourers_workshop.core.skin.geometry.cube;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.math.IRectangle3f;
import moe.plushie.armourers_workshop.api.math.ITransformf;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.ISkinPaintType;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryFace;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;
import moe.plushie.armourers_workshop.core.skin.painting.SkinPaintTypes;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.minecraft.core.Direction;

import java.util.ArrayList;

public class SkinCubeFace extends SkinGeometryFace {

    private static final float[][][] UVS = new float[][][]{
            {{1, 0}, {1, 1}, {0, 1}, {0, 0}}, // -y <- down
            {{0, 0}, {0, 1}, {1, 1}, {1, 0}}, // +y <- up
            {{0, 0}, {0, 1}, {1, 1}, {1, 0}}, // -z <- north
            {{0, 0}, {0, 1}, {1, 1}, {1, 0}}, // +z <- south
            {{0, 0}, {0, 1}, {1, 1}, {1, 0}}, // -x <- west
            {{0, 0}, {0, 1}, {1, 1}, {1, 0}}, // +x <- east
//            {{1, 0}, {1, 1}, {0, 1}, {0, 0}},
//            {{1, 0}, {1, 1}, {0, 1}, {0, 0}},
    };

    private static final float[][][] VERTICES = new float[][][]{
            {{1, 1, 1}, {1, 1, 0}, {0, 1, 0}, {0, 1, 1}, {0, 1, 0}},  // -y <- down
            {{0, 0, 1}, {0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, -1, 0}}, // +y <- up
            {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, -1}}, // -z <- north
            {{1, 0, 1}, {1, 1, 1}, {0, 1, 1}, {0, 0, 1}, {0, 0, 1}},  // +z <- south
            {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 0}},  // -x <- west
            {{0, 0, 1}, {0, 1, 1}, {0, 1, 0}, {0, 0, 0}, {-1, 0, 0}}, // +x <- east
    };

    public final int alpha;

    private final ISkinGeometryType type;
    private final Direction direction;
    private final IPaintColor paintColor;

    private final IRectangle3f boundingBox;

    public SkinCubeFace(ISkinGeometryType type, ITransformf transform, ITextureKey textureKey, IRectangle3f boundingBox, Direction direction, IPaintColor color, int alpha) {
        this.type = type;
        this.transform = transform;
        this.textureKey = textureKey;
        this.paintColor = color;
        this.alpha = alpha;
        this.direction = direction;
        this.boundingBox = boundingBox;
    }

    public static float[][] getBaseUVs(Direction direction) {
        return UVS[direction.get3DDataValue()];
    }

    public static float[][] getBaseVertices(Direction direction) {
        return VERTICES[direction.get3DDataValue()];
    }

    public IRectangle3f getBoundingBox() {
        return boundingBox;
    }

    public IPaintColor getColor() {
        return paintColor;
    }

    public int getAlpha() {
        return alpha;
    }

    public Direction getDirection() {
        return direction;
    }

    public ISkinPaintType getPaintType() {
        return paintColor.getPaintType();
    }

    @Override
    public ISkinGeometryType getType() {
        return type;
    }

    @Override
    public ITextureKey getTextureKey() {
        if (textureKey != null) {
            return textureKey;
        }
        return paintColor.getPaintType().getTextureKey();
    }

    @Override
    public float getPriority() {
        return direction.get3DDataValue();
    }

    @Override
    public boolean isVisible() {
        return paintColor.getPaintType() != SkinPaintTypes.NONE;
    }

    @Override
    public Iterable<? extends SkinGeometryVertex> getVertices() {
        var textureKey = getTextureKey();

        // https://learnopengl.com/Getting-started/Coordinate-Systems
        var x = boundingBox.getX();
        var y = boundingBox.getY();
        var z = boundingBox.getZ();
        var w = roundUp(boundingBox.getWidth());
        var h = roundUp(boundingBox.getHeight());
        var d = roundUp(boundingBox.getDepth());

        var u = textureKey.getU();
        var v = textureKey.getV();
        var s = roundDown(textureKey.getWidth());
        var t = roundDown(textureKey.getHeight());

        var color = new SkinGeometryVertex.Color(paintColor, alpha);
        var vertices = new ArrayList<SkinGeometryVertex>();

        var vertexes = getBaseVertices(direction);
        var uvs = getBaseUVs(getTextureDirection(direction, textureKey));

        for (int i = 0; i < 4; ++i) {
            var position = new Vector3f(x + w * vertexes[i][0], y + h * vertexes[i][1], z + d * vertexes[i][2]);
            var normal = new Vector3f(vertexes[4][0], vertexes[4][1], vertexes[4][2]);
            var textureCoords = new Vector2f(u + s * uvs[i][0], v + t * uvs[i][1]);
            vertices.add(new SkinCubeVertex(this, position, normal, textureCoords, color));
        }

        return vertices;
    }

    private float roundUp(float edg) {
        if (edg == 0) {
            return 0.002f;
        }
        return edg;
    }

    // avoid out-of-bounds behavior caused by floating point precision.
    private float roundDown(float edg) {
        if (edg < 0) {
            return edg + 0.002f;
        } else {
            return edg - 0.002f;
        }
    }

    private Direction getTextureDirection(Direction direction, ITextureKey key) {
        var options = key.getOptions();
        if (options != null) {
            return switch (options.getRotation()) {
                case 90 -> switch (direction) {
                    case DOWN -> Direction.SOUTH;
                    case UP -> Direction.DOWN;
                    case NORTH -> Direction.UP;
                    case SOUTH -> Direction.NORTH;
                    default -> direction;
                };
                case 180 -> switch (direction) {
                    case DOWN -> Direction.NORTH;
                    case UP -> Direction.SOUTH;
                    case NORTH -> Direction.DOWN;
                    case SOUTH -> Direction.UP;
                    default -> direction;
                };
                case 270 -> switch (direction) {
                    case DOWN -> Direction.UP;
                    case UP -> Direction.NORTH;
                    case NORTH -> Direction.SOUTH;
                    case SOUTH -> Direction.DOWN;
                    default -> direction;
                };
                default -> direction;
            };
        }
        return direction;
    }
}
