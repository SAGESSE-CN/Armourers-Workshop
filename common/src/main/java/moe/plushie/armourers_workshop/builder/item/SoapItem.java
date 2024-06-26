package moe.plushie.armourers_workshop.builder.item;

import moe.plushie.armourers_workshop.builder.item.impl.IPaintToolAction;
import moe.plushie.armourers_workshop.builder.item.impl.IPaintToolApplier;
import moe.plushie.armourers_workshop.builder.item.impl.IPaintToolSelector;
import moe.plushie.armourers_workshop.builder.other.CubePaintingEvent;
import moe.plushie.armourers_workshop.builder.other.CubeSelector;
import moe.plushie.armourers_workshop.core.item.FlavouredItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public class SoapItem extends FlavouredItem implements IPaintToolApplier {

    public SoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var resultType = usePaintTool(context);
        if (resultType.consumesAction()) {
            return resultType;
        }
        return super.useOn(context);
    }

    @Override
    public IPaintToolSelector createPaintToolSelector(UseOnContext context) {
        return CubeSelector.box(context.getClickedPos(), false);
    }

    @Override
    public IPaintToolAction createPaintToolAction(UseOnContext context) {
        return new CubePaintingEvent.ClearAction();
    }

    @Override
    public boolean shouldUseTool(UseOnContext context) {
        // by default, applying colors only execute on the client side,
        // and then send the final color to the server side.
        return context.getLevel().isClientSide();
    }
}
