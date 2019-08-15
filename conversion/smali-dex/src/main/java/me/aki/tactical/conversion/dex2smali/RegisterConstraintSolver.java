package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.InstructionProvider;
import me.aki.tactical.conversion.dex2smali.provider.MoveLikeInsnProvider;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that ensures that all register constrains can be fulfilled.
 *
 * If it is impossible to align one list of registers as a sequence, a new sequence of registers is allocated.
 * Move instructions get inserted that move the values into this new register sequence.
 */
public class RegisterConstraintSolver {
    private <T> Set<T> newIdentityHashSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private final DexBody body;
    private final InsertList<InstructionProvider<? extends Instruction>> instructionProviders;
    private final List<RegisterConstraint> registerConstraints;

    /**
     * All ranges that can be encoded without any issues
     */
    private final Set<InsertList<Register>> ranges = newIdentityHashSet();

    /**
     * Map {@link Register Registers} to all ranges that they are a member of.
     */
    private final Map<Register, InsertList<Register>> rangeByRegister = new HashMap<>();

    public RegisterConstraintSolver(DexBody body, InsertList<InstructionProvider<? extends Instruction>> instructionProviders, List<RegisterConstraint> registerConstraints) {
        this.body = body;
        this.instructionProviders = instructionProviders;
        this.registerConstraints = registerConstraints;
    }

    public void solve() {
        for (RegisterConstraint collision : findAllCollidingRanges()) {
            solveCollision(collision);
        }
    }

    /**
     * Assign a new sequence/range of registers to a constraint.
     * Move instructions get inserted that move the values into that sequence.
     *
     * @param constraint a constraint whose registers cannot be represented as a range
     */
    private void solveCollision(RegisterConstraint constraint) {
        List<Register> oldRegisters = constraint.getRegisters();
        InsertList<Register> newRegisters = oldRegisters.stream().map(oldRegister -> {
            Register register = new Register(oldRegister.getType());
            body.getRegisters().add(register);
            return register;
        }).collect(Collectors.toCollection(LinkedInsertList::new));

        constraint.setRegisters(newRegisters);
        constraint.getFirstRegisterCell().setRegister(newRegisters.get(0));
        addRange(newRegisters);

        List<InstructionProvider<? extends Instruction>> moveInsns = new ArrayList<>(oldRegisters.size());
        Iterator<Register> oldRegisterIter = oldRegisters.iterator();
        for (Register newRegister : newRegisters) {
            Register oldRegister = oldRegisterIter.next();
            moveInsns.add(newMoveInsnProvider(newRegister, oldRegister));
        }

        this.instructionProviders.insertBefore(constraint.getInstruction(), moveInsns);
    }

    private MoveLikeInsnProvider newMoveInsnProvider(Register to, Register from) {
        Opcode opcode;
        Opcode opcodeFrom16;
        Opcode opcode16;

        Type type = from.getType();
        if (type instanceof ObjectType || type instanceof ArrayType) {
            opcode = Opcode.MOVE_OBJECT;
            opcodeFrom16 = Opcode.MOVE_OBJECT_FROM16;
            opcode16 = Opcode.MOVE_OBJECT_16;
        } else if (type instanceof DoubleType || type instanceof LongType) {
            opcode = Opcode.MOVE_WIDE;
            opcodeFrom16 = Opcode.MOVE_WIDE_FROM16;
            opcode16 = Opcode.MOVE_WIDE_16;
        } else if (type instanceof IntLikeType || type instanceof FloatType) {
            opcode = Opcode.MOVE;
            opcodeFrom16 = Opcode.MOVE_FROM16;
            opcode16 = Opcode.MOVE_16;
        } else {
            throw new RuntimeException("Unreachable");
        }

        return new MoveLikeInsnProvider(opcode, opcodeFrom16, opcode16, to, from);
    }

    private Set<RegisterConstraint> findAllCollidingRanges() {
        // Ranges that somehow collide with parameter or this registers
        Set<RegisterConstraint> collisions = getParameterCollisions();

        // other ranges that have not yet been checked for collisions
        Set<RegisterConstraint> uncheckedConstraints = diff(this.registerConstraints, collisions);

        collisions.addAll(findCollisions(uncheckedConstraints));

        return collisions;
    }

    private <A> Set<A> diff(Collection<A> all, Collection<A> notContained) {
        Set<A> result = new HashSet<>(all);
        result.removeAll(notContained);
        return result;
    }

    private List<RegisterConstraint> findCollisions(Set<RegisterConstraint> unchecked) {
        return unchecked.stream().filter(constraint -> {
            Set<InsertList<Register>> possibleOverlaps = findRangesWithAnyCommonRegisters(constraint);

            InsertList<Register> constraintRange = new LinkedInsertList<>(constraint.getRegisters());
            Set<InsertList<Register>> overlaps = possibleOverlaps.stream()
                    .map(rangeB -> getOverlap(constraintRange, rangeB))
                    .collect(Collectors.toCollection(this::newIdentityHashSet));

            if (overlaps.contains(null)) {
                // at least one range does not overlap
                return true;
            } else {
                addRange(constraintRange);

                // remove the old possibleOverlaps and merge them into this new range
                possibleOverlaps.forEach(this::removeRange);
                possibleOverlaps.forEach(range -> mergeIntoRange(constraintRange, range));
                return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Add all registers of one range into another range
     *
     * @param mergeInto
     * @param range
     */
    void mergeIntoRange(InsertList<Register> mergeInto, InsertList<Register> range) {
        Register commonRegister = getAnyCommonRegister(mergeInto, range);

        Register succeeding = commonRegister;
        for (Register register = range.getPrevious(commonRegister); register != null; register = range.getPrevious(succeeding = register)) {
            if (!mergeInto.contains(register)) {
                mergeInto.insertBefore(succeeding, register);
            }
        }

        Register preceding = commonRegister;
        for (Register register = range.getNext(commonRegister); register != null; register = range.getNext(preceding = register)) {
            if (!mergeInto.contains(register)) {
                mergeInto.insertAfter(preceding, register);
            }
        }
    }

    /**
     * Find a Register that is present in both ranges
     *
     * @return register contained in both ranges
     */
    private Register getAnyCommonRegister(InsertList<Register> a, InsertList<Register> b) {
        Stream<Register> commonRegisters = Stream.concat(
                a.stream().filter(b::contains),
                b.stream().filter(a::contains)
        );

        return commonRegisters.findAny()
                .orElseThrow(() -> new IllegalStateException("The provided ranges do not have any instructions in common"));
    }

    /**
     * Find all ranges that contain a register that is also contained in the register list of a {@link RegisterConstraint}.
     *
     * @param constraint find ranges containing registers of this constraint
     * @return all ranges that contain registers that are also part of the constraint
     */
    private Set<InsertList<Register>> findRangesWithAnyCommonRegisters(RegisterConstraint constraint) {
        Set<InsertList<Register>> ranges = newIdentityHashSet();
        for (Register register : constraint.getRegisters()) {
            InsertList<Register> range = this.rangeByRegister.get(register);
            if (range != null) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    private void addRange(InsertList<Register> range) {
        this.ranges.add(range);
        range.forEach(register -> this.rangeByRegister.put(register, range));
    }

    private void removeRange(InsertList<Register> range) {
        this.ranges.remove(range);
        range.forEach(register -> this.rangeByRegister.remove(register, range));
    }

    InsertList<Register> getOverlap(InsertList<Register> rangeA, InsertList<Register> rangeB) {
        class OverlapComputation {
            private Register first = searchFirst();
            private Register last = searchLast();

            private Register searchFirst() {
                if (startsAInMiddleOfB(rangeA, rangeB))return rangeA.getFirst();
                if (startsAInMiddleOfB(rangeB, rangeA))return rangeB.getFirst();

                // The registers are not overlapping
                return null;
            }

            /**
             * Check whether the rangeA starts in the middle of rangeB.
             *
             * This requires that the first instruction of range A is in range B.
             * If any instruction in range B precedes the head of range A and is also a member in range A,
             * then the two registers do not overlap, they just have some instructions in common.
             *
             * @param rangeA
             * @param rangeB
             * @return starts rangeA in the middle of rangeB and can they possible be overlapping ranges
             */
            private boolean startsAInMiddleOfB(InsertList<Register> rangeA, InsertList<Register> rangeB) {
                Register headA = rangeA.getFirst();
                if (rangeB.contains(headA)) {
                    while ((headA = rangeB.getPrevious(headA)) != null) {
                        if (rangeA.contains(headA)) {
                            // The registers do not overlap
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            private Register searchLast() {
                if (endsAInMiddleOfB(rangeA, rangeB))return rangeA.getLast();
                if (endsAInMiddleOfB(rangeB, rangeA))return rangeB.getLast();

                // The registers are not overlapping
                return null;
            }

            /**
             * Check whether the rangeA starts in the middle of rangeB.
             *
             * This requires that the last instruction of range A is in range B.
             * If any instruction in range B succeeds the last element of range A and is also a member in range A,
             * then the two registers do not overlap, they just have some instructions in common.
             *
             * @param rangeA
             * @param rangeB
             * @return starts rangeA in the middle of rangeB and can they possible be overlapping ranges
             */
            private boolean endsAInMiddleOfB(InsertList<Register> rangeA, InsertList<Register> rangeB) {
                Register lastA = rangeA.getLast();
                if (rangeB.contains(lastA)) {
                    while ((lastA = rangeB.getNext(lastA)) != null) {
                        if (rangeA.contains(lastA)) {
                            // The registers do not overlap
                            return false;
                        }
                    }
                    return true;
                }

                return false;
            }

            private InsertList<Register> getRange() {
                if (first == null || last == null) {
                    return null;
                }

                InsertList<Register> range = new LinkedInsertList<>();
                Register register = first;
                while(true) {
                    range.add(register);

                    if (register == last) break;

                    Register nextA = rangeA.getNext(register);
                    Register nextB = rangeB.getNext(register);
                    if (nextA == nextB) {
                        register = nextA;
                    } else {
                        // The ranges differ in the possibly overlapping are
                        return null;
                    }
                }

                return range;
            }

            private Register getSuccessor(Register register) {
                Register nextA = rangeA.getNext(register);
                Register nextB = rangeB.getNext(register);
                if (nextA == null || nextB == null) {
                    requireEqual(register, last);
                    return null;
                } else {
                    return requireEqual(nextA, nextB);
                }
            }

            private Register requireEqual(Register a, Register b) {
                if (a == b) {
                    return a;
                } else {
                    throw new IllegalStateException(a + " and " + b + " are not equal");
                }
            }
        }
        return new OverlapComputation().getRange();
    }

    /**
     * Get ranges that include parameter or this registers in a different order that they will initially be present.
     *
     * @return all ranges that must be extracted for that reason
     */
    Set<RegisterConstraint> getParameterCollisions() {
        class ParameterCollision {
            Map<Register, RegisterKind> registerKindMap = getRegisterKindMap();

            private Set<RegisterConstraint> getCollisions() {
                return registerConstraints.stream()
                        .filter(constraint ->
                                !checkRegistersBeforeSpecialRegisters(constraint.getRegisters()) ||
                                !areSpecialRegistersCorrectlyAligned(constraint.getRegisters()))
                        .collect(Collectors.toSet());
            }

            private Map<Register, RegisterKind> getRegisterKindMap() {
                Map<Register, RegisterKind> registerKindMap = new HashMap<>();

                body.getThisRegister().ifPresent(thisRegister ->
                        registerKindMap.put(thisRegister, new RegisterKind.This()));

                int paramIndex = 0;
                for (Register parameterRegister : body.getParameterRegisters()) {
                    registerKindMap.put(parameterRegister, new RegisterKind.Parameter(paramIndex++));
                }

                return registerKindMap;
            }

            /**
             * Check whether a range contains the this and parameter registers only in the order that they will initially present.
             */
            private boolean areSpecialRegistersCorrectlyAligned(List<Register> registers) {
                Iterator<Register> registerIterator = registers.iterator();

                RegisterKind prevKind = findFirstSpecialRegister(registerIterator);

                while (registerIterator.hasNext()) {
                    RegisterKind register = this.registerKindMap.get(registerIterator.next());

                    if (prevKind instanceof RegisterKind.This) {
                        if (!(register instanceof RegisterKind.Parameter) || ((RegisterKind.Parameter) register).index != 0) {
                            // the this is not succeeded by parameter 0.
                            return false;
                        }
                    } else if (prevKind instanceof RegisterKind.Parameter) {
                        if (!(register instanceof RegisterKind.Parameter)) {
                            // A parameter register must always be succeeded by another parameter register
                            return false;
                        }

                        if (((RegisterKind.Parameter) prevKind).index + 1 != ((RegisterKind.Parameter) register).index) {
                            // the 'n'-the parameter is not succeeded by the 'n+1'-th parameter
                            return false;
                        }
                    } else {
                        DexUtils.unreachable();
                    }

                    prevKind = register;
                }
                return true;
            }

            /**
             * If there are any non-special registers, then the first special register must be the <tt>this</tt> register
             * for non-static methods or the <tt>parameter 0</tt> register for static methods.
             */
            private boolean checkRegistersBeforeSpecialRegisters(List<Register> registers) {
                if (registerKindMap.get(registers.get(0)) != null) {
                    // The range starts with a this or parameter register,
                    // so there cannot be any conflicts with regular registers
                    return true;
                } else {
                    RegisterKind firstSpecialRegister = findFirstSpecialRegister(registers.iterator());

                    if (firstSpecialRegister == null) {
                        return true;
                    } else if (body.getThisRegister().isPresent()) {
                        // This is a non-static method, so the last regular register must be succeeded by the this register.
                        return firstSpecialRegister instanceof RegisterKind.This;
                    } else {
                        // This is a static method, so the last regular register must be succeeded by the parameter 0 register.
                        return firstSpecialRegister instanceof RegisterKind.Parameter &&
                                ((RegisterKind.Parameter) firstSpecialRegister).index == 0;
                    }
                }
            }

            private RegisterKind findFirstSpecialRegister(Iterator<Register> registerIterator) {
                while (registerIterator.hasNext()) {
                    RegisterKind registerKind = this.registerKindMap.get(registerIterator.next());
                    if (registerKind != null) {
                        return registerKind;
                    }
                }
                return null;
            }
        }
        return new ParameterCollision().getCollisions();
    }

    interface RegisterKind {
        class This implements RegisterKind {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof This;
            }
        }

        class Parameter implements RegisterKind {
            private final int index;
            public Parameter(int index) {
                this.index = index;
            }
        }
    }
}
