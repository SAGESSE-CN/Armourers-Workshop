package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.ISkinPaintType;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryVertex;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.math.Vector2f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;

public class SkinGeometryVertex implements ISkinGeometryVertex {

    protected int id = 0;
    protected float weight = 0.0f;

    protected Vector3f position = Vector3f.ZERO;
    protected Vector3f normal = Vector3f.ZERO;
    protected Vector2f textureCoords = Vector2f.ZERO;

    protected Color color = Color.WHITE;

    public SkinGeometryVertex() {
    }

    public SkinGeometryVertex(int id, float weight, Vector3f position, Vector3f normal, Vector2f textureCoords) {
        this.id = id;
        this.weight = weight;
        this.position = position;
        this.normal = normal;
        this.textureCoords = textureCoords;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public Vector3f getNormal() {
        return normal;
    }

    @Override
    public Vector2f getTextureCoords() {
        return textureCoords;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return ObjectUtils.makeDescription(this, "position", getPosition(), "normal", getNormal(), "uv", getTextureCoords(), "color", getColor());
    }

    public static class Color implements IPaintColor {

        public static final Color WHITE = new Color(PaintColor.WHITE, 255);

        protected final int alpha;
        protected final IPaintColor paintColor;

        public Color(IPaintColor paintColor, int alpha) {
            this.paintColor = paintColor;
            this.alpha = alpha;
        }

        @Override
        public int getRGB() {
            return paintColor.getRGB();
        }

        @Override
        public int getRawValue() {
            return paintColor.getRawValue();
        }

        @Override
        public ISkinPaintType getPaintType() {
            return paintColor.getPaintType();
        }

        public int getAlpha() {
            return alpha;
        }

        @Override
        public String toString() {
            return String.format("%s * %f", paintColor, getAlpha() / 255f);
        }
    }
}
