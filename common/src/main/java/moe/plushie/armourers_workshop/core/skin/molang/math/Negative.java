package moe.plushie.armourers_workshop.core.skin.molang.math;

/**
 * Negative operator class This class is responsible for inverting given value
 */
public class Negative implements IValue {

    public IValue value;

    public Negative(IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return -this.value.get();
    }

    @Override
    public String toString() {
        return "-" + this.value.toString();
    }

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }
}
