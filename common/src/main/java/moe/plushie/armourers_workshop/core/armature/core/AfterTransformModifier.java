package moe.plushie.armourers_workshop.core.armature.core;

import moe.plushie.armourers_workshop.api.armature.IJoint;
import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.api.client.model.IModel;
import moe.plushie.armourers_workshop.api.math.IQuaternionf;
import moe.plushie.armourers_workshop.core.armature.ArmatureModifier;
import moe.plushie.armourers_workshop.utils.math.Vector3f;

public class AfterTransformModifier extends ArmatureModifier {

    private final Vector3f translate;
    private final Vector3f scale;
    private final Vector3f rotate;

    public AfterTransformModifier(Vector3f translate, Vector3f scale, Vector3f rotate) {
        this.translate = translate;
        this.scale = scale;
        this.rotate = rotate;
    }

    @Override
    public IJointTransform apply(IJoint joint, IModel model, IJointTransform transform) {
        transform = _translate(transform);
        transform = _rotate(transform);
        transform = _scale(transform);
        return transform;
    }

    private IJointTransform _translate(IJointTransform transform) {
        if (translate.equals(Vector3f.ZERO)) {
            return transform;
        }
        return poseStack -> {
            transform.apply(poseStack);
            poseStack.translate(translate.getX(), translate.getY(), translate.getZ());
        };
    }

    private IJointTransform _rotate(IJointTransform transform) {
        if (rotate.equals(Vector3f.ZERO)) {
            return transform;
        }
        IQuaternionf xRot = Vector3f.ZP.rotation(rotate.getX());
        IQuaternionf yRot = Vector3f.YP.rotation(rotate.getY());
        IQuaternionf zRot = Vector3f.XP.rotation(rotate.getZ());
        return poseStack -> {
            transform.apply(poseStack);
            poseStack.rotate(xRot);
            poseStack.rotate(yRot);
            poseStack.rotate(zRot);
        };
    }

    private IJointTransform _scale(IJointTransform transform) {
        if (scale.equals(Vector3f.ONE)) {
            return transform;
        }
        return poseStack -> {
            transform.apply(poseStack);
            poseStack.scale(scale.getX(), scale.getY(), scale.getZ());
        };
    }
}
