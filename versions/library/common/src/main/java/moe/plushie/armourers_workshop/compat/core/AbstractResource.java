package moe.plushie.armourers_workshop.compat.core;

import moe.plushie.armourers_workshop.api.annotation.Available;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.function.Function;

@Available("[20, )")
public class AbstractResource extends Resource {

    public AbstractResource(Resource resource, Function<InputStream, InputStream> transformer) {
        super(resource.source(), () -> transformer.apply(resource.open()), resource::metadata);
    }
}
