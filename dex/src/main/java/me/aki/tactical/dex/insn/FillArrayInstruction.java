package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
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
     * The bytes size of values in the {@link FillArrayInstruction#values} array.
     */
    private NumberSize elementSize;

    /**
     * The numbers that should be stored in the array.
     */
    private List<NumericConstant> values;

    public FillArrayInstruction(Register array, NumberSize elementSize, List<NumericConstant> values) {
        this.array = array;
        this.elementSize = elementSize;
        this.values = values;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
    }

    public RWCell<Register> getArrayCell() {
        return RWCell.of(this::getArray, this::setArray, Register.class);
    }

    public NumberSize getElementSize() {
        return elementSize;
    }

    public void setElementSize(NumberSize elementSize) {
        this.elementSize = elementSize;
    }

    public List<NumericConstant> getValues() {
        return values;
    }

    public void setValues(List<NumericConstant> values) {
        this.values = values;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(array);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getArrayCell());
    }

    public static enum NumberSize {
        BYTE(1),   // 1 byte
        SHORT(2),  // 2 bytes
        INT(4),    // 4 bytes
        LONG(8);   // 8 bytes

        private final int byteSize;

        NumberSize(int byteSize) {
            this.byteSize = byteSize;
        }

        /**
         * Get the size of this kind of numbers in bytes
         * @return
         */
        public int getByteSize() {
            return byteSize;
        }

        public static NumberSize fromByteSize(int bytesize) {
            for (NumberSize value : values()) {
                if (value.byteSize == bytesize) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Expected 1, 2, 4 or 8, got " + bytesize);
        }
    }

    /**
     * A numeric constant of int, long, float or double type.
     */
    public static class NumericConstant {
        private long value;

        public NumericConstant(int value) {
            this.value = value;
        }

        public NumericConstant(long value) {
            this.value = value;
        }

        public NumericConstant(float value) {
            this.value = Float.floatToRawIntBits(value);
        }

        public NumericConstant(double value) {
            this.value = Double.doubleToLongBits(value);
        }

        public int intValue() {
            return (int) value;
        }

        public long longValue() {
            return value;
        }

        public float floatValue() {
            return Float.intBitsToFloat(intValue());
        }

        public double doubleValue() {
            return Double.longBitsToDouble(longValue());
        }

        @Override
        public String toString() {
            return NumericConstant.class.getSimpleName() + '{' +
                    "intValue()=" + intValue() +
                    ", longValue()=" + longValue() +
                    ", floatValue()=" + floatValue() +
                    ", doubleValue()=" + doubleValue() +
                    '}';
        }
    }
}
