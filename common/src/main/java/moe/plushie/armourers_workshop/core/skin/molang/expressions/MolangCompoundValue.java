package moe.plushie.armourers_workshop.core.skin.molang.expressions;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import moe.plushie.armourers_workshop.core.skin.molang.math.LazyVariable;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * An extension of the {@link MolangValue} class, allowing for compound expressions.
 */
public class MolangCompoundValue extends MolangValue {

    public final List<MolangValue> values = new ObjectArrayList<>();

    public final Map<String, LazyVariable> locals = new Object2ObjectOpenHashMap<>();

    public MolangCompoundValue(MolangValue baseValue) {
        super(baseValue);
        this.values.add(baseValue);
    }

    @Override
    public double get() {
        double value = 0;
        for (MolangValue molangValue : this.values) {
            value = molangValue.get();
        }
        return value;
    }

    @Override
    public boolean isConstant() {
        // when use any local variables, we can't deduce the conclusion.
        if (!locals.isEmpty()) {
            return false;
        }
        for (var value : this.values) {
            if (!value.isConstant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        var builder = new StringJoiner("; ");
        for (var molangValue : values) {
            builder.add(molangValue.toString());
            if (molangValue.isReturnValue()) {
                break;
            }
        }
        return builder.toString();
    }
}