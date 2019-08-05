package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Branch to another location in code if a certain condition applies to two values.
 */
public class IfInstruction implements BranchInstruction {
    /**
     * How to compare the two values.
     */
    private Comparison comparison;

    /**
     * Register containing the first value to be compared.
     */
    private Register op1;

    /**
     * The first register is either compared against this register or <tt>zero</tt>/<tt>null</tt>.
     */
    private Optional<Register> op2;

    /**
     * Branch to this location if the condition applied
     */
    private Instruction target;

    public IfInstruction(Comparison comparison, Register op1, Optional<Register> op2, Instruction target) {
        this.comparison = comparison;
        this.op1 = op1;
        this.op2 = op2;
        this.target = target;
    }

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public Register getOp1() {
        return op1;
    }

    public void setOp1(Register op1) {
        this.op1 = op1;
    }

    public RWCell<Register> getOp1Cell() {
        return RWCell.of(this::getOp1, this::setOp1, Register.class);
    }

    public Optional<Register> getOp2() {
        return op2;
    }

    public void setOp2(Optional<Register> op2) {
        this.op2 = op2;
    }

    public Optional<RWCell<Register>> getOp2Cell() {
        return op2.map(x -> RWCell.of(() -> this.op2.get(), op2 -> this.op2 = Optional.of(op2), Register.class));
    }

    public Instruction getTarget() {
        return target;
    }

    public void setTarget(Instruction target) {
        this.target = target;
    }

    public RWCell<Instruction> getTargetCell() {
        return RWCell.of(this::getTarget, this::setTarget, Instruction.class);
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.of(target);
    }

    @Override
    public List<RWCell<Instruction>> getBranchTargetCells() {
        return List.of(getTargetCell());
    }

    @Override
    public List<Register> getReadRegisters() {
        return Stream.concat(Stream.of(op1), op2.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return Stream.concat(Stream.of(getOp1Cell()), getOp2Cell().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }

    public enum Comparison {
        EQUAL, NON_EQUAL, LESS_THAN, LESS_EQUAL, GREATER_THAN, GREATER_EQUAL;

        /**
         * Get the comparison that would result in the opposite value.
         *
         * @return get the negated comparison
         */
        public Comparison negate() {
            switch (this) {
                case EQUAL: return NON_EQUAL;
                case NON_EQUAL: return EQUAL;
                case LESS_THAN: return GREATER_EQUAL;
                case LESS_EQUAL: return GREATER_THAN;
                case GREATER_THAN: return LESS_EQUAL;
                case GREATER_EQUAL: return LESS_THAN;
                default: throw new AssertionError();
            }
        }
    }
}
