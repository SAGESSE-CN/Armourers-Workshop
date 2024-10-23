package moe.plushie.armourers_workshop.core.armature.thirdparty;

import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.utils.DataContainerKey;

public interface EpicFlightTransformProvider {

    DataContainerKey<EpicFlightTransformProvider> KEY = DataContainerKey.of("transforms", EpicFlightTransformProvider.class);

    IJointTransform apply(String name);
}
