package me.aki.tactical.conversion.smali2dex.typing;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;

/**
 * More type information is required to set a type within an instruction.
 * Once the types that certain registers will have before and after the
 * instructions have been inferred, the type of the instruction can be set.
 */
public abstract class UntypedInfo {
    /**
     * Instruction at which we need some additional type information.
     */
    private final Instruction instruction;

    /**
     * Type information of these registers before the execution of
     * the {@link #instruction} is required to compute the type.
     */
    private final Set<RCell<Register>> before;

    /**
     * Type information of these registers after the execution of
     * the {@link #instruction} is required to compute the type.
     */
    private final Optional<RCell<Register>> after;

    public UntypedInfo(Instruction instruction, Set<RCell<Register>> before, Optional<RCell<Register>> after) {
        this.instruction = instruction;
        this.before = before;
        this.after = after;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Set<RCell<Register>> getBefore() {
        return before;
    }

    public Optional<RCell<Register>> getAfter() {
        return after;
    }

    /**
     * Find one common supertypes for a list of types.
     *
     * @param types a list of type
     * @return a subtype of all types
     */
    protected Optional<Type> mergeTypes(Stream<Type> types) {
        return types.reduce((a, b) -> {
            if (a.equals(b)) {
                return a;
            } else if (a instanceof IntLikeType && b instanceof IntLikeType) {
                // We've got e.g. int and short
                return IntType.getInstance();
            } else if (a instanceof RefType && b instanceof RefType) {
                // We've got different classes or arrays with different base types
                return ObjectType.OBJECT;
            } else {
                throw new RuntimeException("Cannot merge types " + a + " and " + b);
            }
        });
    }

    protected Optional<ArrayType> findArrayType(Stream<Type> types) {
        Stream<Type> arrayLowerTypes = types
                .map(this::requireRefType) // If we expect the local to contain an array type, it cannot contain a primitive type
                .filter(typ -> typ instanceof ArrayType)
                .map(typ -> ((ArrayType) typ).getLowerType());

        return mergeTypes(arrayLowerTypes).map(lowerType -> new ArrayType(lowerType, 1));
    }

    private Type requireRefType(Type type) {
        if (type instanceof RefType) {
            return type;
        } else {
            throw new IllegalStateException("Expected a ref type");
        }
    }

    /**
     * Compute and set the type based on the computed type information.
     *
     * @param typesBefore map the required {@link #before} registers to their types
     * @param typeAfter type of the required {@link #after} register
     * @return is more type information required to infer the type.
     */
    public abstract boolean setType(Map<Register, Set<Type>> typesBefore, Optional<Set<Type>> typeAfter);

    /**
     * Has this instruction no side effects, only stores a value into a register and
     * can therefore be safely removed if that value is never used.
     *
     * @return has this instruction any side effects
     */
    public abstract boolean hasSideEffects();
}
