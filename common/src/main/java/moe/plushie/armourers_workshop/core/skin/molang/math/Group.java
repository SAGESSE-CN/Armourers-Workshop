package moe.plushie.armourers_workshop.core.skin.molang.math;

/**
 * Group class Simply wraps given {@link IValue} into parenthesis in the {@link #toString()} method.
 */
public class Group implements IValue {

    private final IValue value;

    public Group(IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return value.get();
    }

    @Override
    public String toString() {
        return "(" + value.toString() + ")";
    }

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }
}
