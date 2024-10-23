package moe.plushie.armourers_workshop.core.skin.geometry.cube;

import moe.plushie.armourers_workshop.api.common.ITextureKey;
import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.data.color.PaintColor;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometry;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.math.OpenVoxelShape;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Vector3i;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Objects;

public abstract class SkinCube extends SkinGeometry {

    protected Rectangle3f boundingBox = Rectangle3f.ZERO;

    protected final EnumMap<Direction, IPaintColor> paintColors = new EnumMap<>(Direction.class);


    public void setType(ISkinGeometryType type) {
        throw new UnsupportedOperationException();
    }

    public void setBoundingBox(Rectangle3f box) {
        throw new UnsupportedOperationException();
    }

    public void setPaintColor(Direction dir, IPaintColor paintColor) {
        throw new UnsupportedOperationException();
    }

    public Rectangle3f getBoundingBox() {
        return boundingBox;
    }

    public IPaintColor getPaintColor(Direction dir) {
        return paintColors.getOrDefault(dir, PaintColor.CLEAR);
    }

    public abstract ITextureKey getTexture(Direction dir);

    @Nullable
    public SkinCubeFace getFace(Direction dir) {
        var textureKey = getTexture(dir);
        var paintColor = getPaintColor(dir);
        var geometryType = getType();
        var alpha = 255;
        if (SkinGeometryTypes.isGlass(geometryType)) {
            alpha = 127;
        }
        var transform = getTransform();
        var boundingBox = getBoundingBox();
        return new SkinCubeFace(geometryType, transform, textureKey, boundingBox, dir, paintColor, alpha);
    }

    @Override
    public OpenVoxelShape getShape() {
        return OpenVoxelShape.box(getBoundingBox());
    }

    @Override
    public Iterable<SkinCubeFace> getFaces() {
        return ObjectUtils.makeIterable(Direction.values(), this::getFace, Objects::nonNull);
    }

    public Vector3i getBlockPos() {
        var boundingBox = getBoundingBox();
        return new Vector3i(boundingBox.getX(), boundingBox.getY(), boundingBox.getZ());
    }
}
