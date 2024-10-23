package moe.plushie.armourers_workshop.core.skin.geometry;

import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometrySet;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.core.skin.serializer.SkinUsedCounter;
import moe.plushie.armourers_workshop.utils.ObjectUtils;
import moe.plushie.armourers_workshop.utils.math.OpenPoseStack;
import moe.plushie.armourers_workshop.utils.math.OpenVoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class SkinGeometrySet<T extends SkinGeometry> implements ISkinGeometrySet<T> {

    protected final SkinUsedCounter usedCounter = new SkinUsedCounter();

    protected int id = -1;

    public int getId() {
        return id;
    }

    @Override
    public OpenVoxelShape getShape() {
        var poseStack = new OpenPoseStack();
        var combinedShape = new OpenVoxelShape();
        for (var geometry : this) {
            var shape = geometry.getShape();
            var transform = geometry.getTransform();
            if (transform.isIdentity()) {
                combinedShape.add(shape);
                continue;
            }
            poseStack.pushPose();
            var shape1 = shape.copy();
            transform.apply(poseStack);
            shape1.mul(poseStack.last().pose());
            combinedShape.add(shape1);
            poseStack.popPose();
        }
        combinedShape.optimize();
        return combinedShape;
    }

    @Nullable
    @Override
    public Collection<ISkinGeometryType> getSupportedTypes() {
        // we don't know the included cube types.
        return null;
    }

    public SkinUsedCounter getUsedCounter() {
        return usedCounter;
    }

    @Override
    public String toString() {
        return ObjectUtils.makeDescription(this, "id", getId(), "size", size(), "types", getSupportedTypes());
    }
}
