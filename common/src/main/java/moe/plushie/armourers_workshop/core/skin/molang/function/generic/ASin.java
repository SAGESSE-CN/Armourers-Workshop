package moe.plushie.armourers_workshop.core.skin.molang.function.generic;

import moe.plushie.armourers_workshop.core.skin.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.molang.core.Function;
import moe.plushie.armourers_workshop.core.skin.molang.impl.MathHelper;

import java.util.List;

/**
 * {@link Function} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the arc-sine of the input value angle, with the input angle converted to radians
 */
public final class ASin extends Function {

    private final Expression value;

    public ASin(String name, List<Expression> arguments) {
        super(name, 1, arguments);
        this.value = arguments.get(0);
    }

    @Override
    public double getAsDouble() {
        return Math.asin(value.getAsDouble() * MathHelper.DEG_TO_RAD);
    }
}