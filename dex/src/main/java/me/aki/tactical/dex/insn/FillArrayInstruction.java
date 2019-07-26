package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Fill an array with numeric constants.
 */
public class FillArrayInstruction implements Instruction {
    /**
     * Register containing the array that should be filled.
     */
    private Register array;

    /**
     * The numbers that should be stored in the array.
     */
    private List<NumbericConstant> values;

    public FillArrayInstruction(Register array, List<NumbericConstant> values) {
        this.array = array;
        this.values = values;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
    }

    public List<NumbericConstant> getValues() {
        return values;
    }

    public void setValues(List<NumbericConstant> values) {
        this.values = values;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    /**
     * A numeric constant of int, long, float or double type.
     */
    public static class NumbericConstant {
        private long value;

        public NumbericConstant(int value) {
            this.value = value;
        }

        public NumbericConstant(long value) {
            this.value = value;
        }

        public NumbericConstant(float value) {
            this.value = Float.floatToRawIntBits(value);
        }

        public NumbericConstant(double value) {
            this.value = Double.doubleToLongBits(value);
        }

        public int intValue() {
            return (int) value;
        }

        public long longValue() {
            return value;
        }

        public float floatValue() {
            return Float.intBitsToFloat((int) value);
        }

        public double doubleValue() {
            return Double.longBitsToDouble(value);
        }

        @Override
        public String toString() {
            return NumbericConstant.class.getSimpleName() + '{' +
                    "intValue()=" + intValue() +
                    ", longValue()=" + longValue() +
                    ", floatValue()=" + floatValue() +
                    ", doubleValue()=" + doubleValue() +
                    '}';
        }
    }
}
