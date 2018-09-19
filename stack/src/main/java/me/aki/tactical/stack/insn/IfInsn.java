package me.aki.tactical.stack.insn;

import java.util.Objects;

/**
 * Branch to another instruction if a certain condition applies to the top value of the stack.
 */
public class IfInsn extends AbstractInstruction implements BranchInsn {
    // Pop one reference value and compare it with null
    public static final ReferenceCondition IF_NULL = new ReferenceCondition(EQ.getInstance(), NullValue.getInstance());
    public static final ReferenceCondition IF_NONNULL = new ReferenceCondition(NE.getInstance(), NullValue.getInstance());

    // Pop one value and compare it with zero
    public static final IntCondition IF_EQ_ZERO = new IntCondition(EQ.getInstance(), ZeroValue.getInstance());
    public static final IntCondition IF_NE_ZERO = new IntCondition(NE.getInstance(), ZeroValue.getInstance());
    public static final IntCondition IF_LT_ZERO = new IntCondition(LT.getInstance(), ZeroValue.getInstance());
    public static final IntCondition IF_LE_ZERO = new IntCondition(LE.getInstance(), ZeroValue.getInstance());
    public static final IntCondition IF_GT_ZERO = new IntCondition(GT.getInstance(), ZeroValue.getInstance());
    public static final IntCondition IF_GE_ZERO = new IntCondition(GE.getInstance(), ZeroValue.getInstance());

    // Pop two reference values and compare them
    public static final ReferenceCondition IF_REF_EQ = new ReferenceCondition(EQ.getInstance(), StackValue.getInstance());
    public static final ReferenceCondition IF_REF_NE = new ReferenceCondition(NE.getInstance(), StackValue.getInstance());

    // Pop two ints and compare them
    public static final IntCondition IF_INT_EQ = new IntCondition(EQ.getInstance(), StackValue.getInstance());
    public static final IntCondition IF_INT_NE = new IntCondition(NE.getInstance(), StackValue.getInstance());
    public static final IntCondition IF_INT_LT = new IntCondition(LT.getInstance(), StackValue.getInstance());
    public static final IntCondition IF_INT_LE = new IntCondition(LE.getInstance(), StackValue.getInstance());
    public static final IntCondition IF_INT_GT = new IntCondition(GT.getInstance(), StackValue.getInstance());
    public static final IntCondition IF_INT_GE = new IntCondition(GE.getInstance(), StackValue.getInstance());

    /**
     * Instruction that will be executed next, if the condition is true.
     */
    private Instruction target;

    /**
     * Do a branch if this condition applies.
     */
    private Condition condition;

    public IfInsn(Instruction target, Condition condition) {
        this.target = target;
        this.condition = condition;
    }

    public Instruction getTarget() {
        return target;
    }

    public void setTarget(Instruction target) {
        this.target = target;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return getCondition().getCompareValue() == StackValue.getInstance() ? 2 : 1;
    }

    public static interface Condition {
        /**
         * Against what value should the popped value be compared.
         */
        public CompareValue getCompareValue();

        /**
         * How should the values be compared
         */
        public Comparison getComparison();
    }

    /**
     * Pop an int and compare it against another value.
     */
    public static class IntCondition implements Condition {
        private final IntComparison comparison;
        private final IntCompareValue compareValue;

        public IntCondition(IntComparison comparison, IntCompareValue compareValue) {
            this.comparison = comparison;
            this.compareValue = compareValue;
        }

        @Override
        public IntComparison getComparison() {
            return comparison;
        }

        @Override
        public IntCompareValue getCompareValue() {
            return compareValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntCondition that = (IntCondition) o;
            return Objects.equals(comparison, that.comparison) &&
                    Objects.equals(compareValue, that.compareValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(comparison, compareValue);
        }

        @Override
        public String toString() {
            return IntCondition.class.getSimpleName() + '{' +
                    "comparison=" + comparison +
                    ", compareValue=" + compareValue +
                    '}';
        }
    }

    /**
     * Pop an object or array value and compare it against another value.
     */
    public static class ReferenceCondition implements Condition {
        private final ReferenceComparison comparison;
        private final ReferenceCompareValue compareValue;

        public ReferenceCondition(ReferenceComparison comparison, ReferenceCompareValue compareValue) {
            this.comparison = comparison;
            this.compareValue = compareValue;
        }

        @Override
        public ReferenceComparison getComparison() {
            return comparison;
        }

        @Override
        public ReferenceCompareValue getCompareValue() {
            return compareValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReferenceCondition that = (ReferenceCondition) o;
            return Objects.equals(comparison, that.comparison) &&
                    Objects.equals(compareValue, that.compareValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(comparison, compareValue);
        }

        @Override
        public String toString() {
            return ReferenceCondition.class.getSimpleName() + '{' +
                    "comparison=" + comparison +
                    ", compareValue=" + compareValue +
                    '}';
        }
    }

    public static interface CompareValue {}
    public static interface IntCompareValue extends CompareValue {}
    public static interface ReferenceCompareValue extends CompareValue {}

    /**
     * Compare with another value popped from the stack
     */
    public static class StackValue implements IntCompareValue, ReferenceCompareValue {
        private static final StackValue INSTANCE = new StackValue();

        public static StackValue getInstance() {
            return INSTANCE;
        }

        private StackValue() {}

        @Override
        public String toString() {
            return StackValue.class.getSimpleName() + "{}";
        }
    }

    /**
     * Compare with the number zero.
     */
    public static class ZeroValue implements IntCompareValue {
        private static final ZeroValue INSTANCE = new ZeroValue();

        public static ZeroValue getInstance() {
            return INSTANCE;
        }

        private ZeroValue() {}

        @Override
        public String toString() {
            return ZeroValue.class.getSimpleName() + "{}";
        }
    }

    /**
     * Compare with {@code null}.
     */
    public static class NullValue implements ReferenceCompareValue {
        private static final NullValue INSTANCE = new NullValue();

        public static NullValue getInstance() {
            return INSTANCE;
        }

        private NullValue() {}

        @Override
        public String toString() {
            return NullValue.class.getSimpleName() + "{}";
        }
    }

    public static  interface Comparison {
        public Comparison negate();
    }

    public static  interface IntComparison extends Comparison {
        @Override
        public IntComparison negate();
    }

    public static interface ReferenceComparison extends Comparison {
        @Override
        public ReferenceComparison negate();
    }

    /**
     * The values to be compared are equal
     */
    public static class EQ implements IntComparison, ReferenceComparison {
        private static final EQ INSTANCE = new EQ();

        public static EQ getInstance() {
            return INSTANCE;
        }

        private EQ() {}

        @Override
        public NE negate() {
            return NE.getInstance();
        }

        @Override
        public String toString() {
            return EQ.class.getSimpleName() + "{}";
        }
    }

    /**
     * The values to be compared are not equal
     */
    public static class NE implements IntComparison, ReferenceComparison {
        private static final NE INSTANCE = new NE();

        public static NE getInstance() {
            return INSTANCE;
        }

        private NE() {}

        @Override
        public EQ negate() {
            return EQ.getInstance();
        }

        @Override
        public String toString() {
            return NE.class.getSimpleName() + "{}";
        }
    }

    /**
     * The int popped from the stack is greater than the other.
     */
    public static class GT implements IntComparison {
        private static final GT INSTANCE = new GT();

        public static GT getInstance() {
            return INSTANCE;
        }

        private GT() {}

        @Override
        public LE negate() {
            return LE.getInstance();
        }

        @Override
        public String toString() {
            return GT.class.getSimpleName() + "{}";
        }
    }

    /**
     * The int popped from the stack is greater or equal than the other.
     */
    public static class GE implements IntComparison {
        private static final GE INSTANCE = new GE();

        public static GE getInstance() {
            return INSTANCE;
        }

        private GE() {}

        @Override
        public LT negate() {
            return LT.getInstance();
        }

        @Override
        public String toString() {
            return GE.class.getSimpleName() + "{}";
        }
    }

    /**
     * The int popped from the stack is less than the other.
     */
    public static class LT implements IntComparison {
        private static final LT INSTANCE = new LT();

        public static LT getInstance() {
            return INSTANCE;
        }

        private LT() {}

        @Override
        public GE negate() {
            return GE.getInstance();
        }

        @Override
        public String toString() {
            return LT.class.getSimpleName() + "{}";
        }
    }

    /**
     * The int popped from the stack is less or equal than the other.
     */
    public static class LE implements IntComparison {
        private static final LE INSTANCE = new LE();

        public static LE getInstance() {
            return INSTANCE;
        }

        private LE() {}

        @Override
        public GT negate() {
            return GT.getInstance();
        }

        @Override
        public String toString() {
            return LE.class.getSimpleName() + "{}";
        }
    }
}
