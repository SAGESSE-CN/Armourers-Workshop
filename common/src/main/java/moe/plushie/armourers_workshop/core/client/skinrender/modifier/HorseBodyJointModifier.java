package moe.plushie.armourers_workshop.core.client.skinrender.modifier;

import moe.plushie.armourers_workshop.api.armature.IJoint;
import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.api.client.model.IModel;
import moe.plushie.armourers_workshop.core.armature.JointModifier;

public class HorseBodyJointModifier extends JointModifier {

    @Override
    public IJointTransform apply(IJoint joint, IModel model, IJointTransform transform) {
        // ...
        var modelPart = model.getPart("body");
        if (modelPart == null) {
            return transform;
        }
        var pose = modelPart.pose();
        return poseStack -> {
            pose.transform(poseStack);
            transform.apply(poseStack);
        };
    }
}
