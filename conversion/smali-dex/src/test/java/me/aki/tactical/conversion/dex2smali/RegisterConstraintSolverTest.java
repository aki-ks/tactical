package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterConstraintSolverTest {
    @Test
    public void testParameterCollisionDetection() {
        // Uses of only one parameter are okay
        testParamCollision(false, 0, false, 3,
                (register, thisReg, arg) -> List.of(arg[0]),
                (register, thisReg, arg) -> List.of(arg[1]),
                (register, thisReg, arg) -> List.of(arg[2])
        );

        // Use of parameters in order are allowed
        testParamCollision(false, 0, false, 3,
                (register, thisReg, arg) -> List.of(arg[0], arg[1]         ),
                (register, thisReg, arg) -> List.of(         arg[1], arg[2]),
                (register, thisReg, arg) -> List.of(arg[0], arg[1], arg[2])
        );

        // Use of parameters out of order are not allowed
        testParamCollision(true, 0, false, 3,
                (register, thisReg, arg) -> List.of(arg[0], arg[2]),
                (register, thisReg, arg) -> List.of(arg[1], arg[0]),
                (register, thisReg, arg) -> List.of(arg[2], arg[0]),
                (register, thisReg, arg) -> List.of(arg[2], arg[1]),
                (register, thisReg, arg) -> List.of(arg[0], arg[2], arg[1])
        );

        // Multiple uses of parameters are not allowed
        testParamCollision(true, 0, false, 3,
                (register, thisReg, arg) -> List.of(arg[0], arg[0]),
                (register, thisReg, arg) -> List.of(arg[1], arg[1]),
                (register, thisReg, arg) -> List.of(arg[2], arg[2]),
                (register, thisReg, arg) -> List.of(arg[0], arg[0], arg[1], arg[2]),
                (register, thisReg, arg) -> List.of(arg[0], arg[1], arg[1], arg[2]),
                (register, thisReg, arg) -> List.of(arg[0], arg[1], arg[2], arg[2])
        );

        // If a this register is present it must precede parameter 0
        testParamCollision(false, 0, true, 3,
                (register, thisReg, arg) -> List.of(thisReg, arg[0]),
                (register, thisReg, arg) -> List.of(thisReg, arg[0], arg[1], arg[2])
        );
        testParamCollision(true, 1, true, 3,
                (register, thisReg, arg) -> List.of(thisReg, arg[1]),
                (register, thisReg, arg) -> List.of(thisReg, arg[1], arg[2]),
                (register, thisReg, arg) -> List.of(thisReg, arg[2]),
                (register, thisReg, arg) -> List.of(arg[0], thisReg),
                (register, thisReg, arg) -> List.of(arg[1], thisReg),
                (register, thisReg, arg) -> List.of(arg[2], thisReg)
        );

        // Regular register may not succeed a this or parameter register
        testParamCollision(true, 1, true, 3,
                (register, thisReg, arg) -> List.of(thisReg, register[0])
        );
        testParamCollision(true, 1, false, 3,
                (register, thisReg, arg) -> List.of(arg[0], register[0]),
                (register, thisReg, arg) -> List.of(arg[1], register[0]),
                (register, thisReg, arg) -> List.of(arg[2], register[0])
        );

        // Regular register may precede the first parameter register if no this register is present
        testParamCollision(false, 3, false, 3,
                (register, thisReg, arg) -> List.of(register[0], arg[0]),
                (register, thisReg, arg) -> List.of(register[1], arg[0]),
                (register, thisReg, arg) -> List.of(register[2], arg[0])
        );
        testParamCollision(true, 3, false, 3,
                (register, thisReg, arg) -> List.of(register[0], arg[1]),
                (register, thisReg, arg) -> List.of(register[0], arg[2]),
                (register, thisReg, arg) -> List.of(register[1], arg[1]),
                (register, thisReg, arg) -> List.of(register[1], arg[2]),
                (register, thisReg, arg) -> List.of(register[2], arg[1]),
                (register, thisReg, arg) -> List.of(register[2], arg[2])
        );

        // If a this register is present, regular register may precede it.
        testParamCollision(false, 3, true, 3,
                (register, thisReg, arg) -> List.of(register[0], thisReg),
                (register, thisReg, arg) -> List.of(register[1], thisReg),
                (register, thisReg, arg) -> List.of(register[2], thisReg),
                (register, thisReg, arg) -> List.of(register[0], register[1], register[2], thisReg)
        );
        // If a this register is present, regular register may not precede argument registers.
        testParamCollision(true, 3, true, 3,
                (register, thisReg, arg) -> List.of(register[0], arg[0]),
                (register, thisReg, arg) -> List.of(register[0], arg[1]),
                (register, thisReg, arg) -> List.of(register[0], arg[2]),
                (register, thisReg, arg) -> List.of(register[1], arg[0]),
                (register, thisReg, arg) -> List.of(register[1], arg[1]),
                (register, thisReg, arg) -> List.of(register[1], arg[2]),
                (register, thisReg, arg) -> List.of(register[2], arg[0]),
                (register, thisReg, arg) -> List.of(register[2], arg[1]),
                (register, thisReg, arg) -> List.of(register[2], arg[2])
        );
    }

    private void testParamCollision(boolean doesCollide, int registerCount, boolean hasThisRegister, int paramCount, ConstraintDsl... constraints) {
        DexBody body = createBodyWithRegisters(hasThisRegister, paramCount, registerCount);

        Map<ConstraintDsl, RegisterConstraint> constraintMap = Arrays.stream(constraints).collect(Collectors.toMap(
                Function.identity(),
                constraintDsl -> {
                    Register[] registers = body.getRegisters().toArray(new Register[0]);
                    Register thisRegister = body.getThisRegister().orElse(null);
                    Register[] args = body.getParameterRegisters().toArray(new Register[0]);
                    List<Register> constraint = constraintDsl.getConstraint(registers, thisRegister, args);
                    return new RegisterConstraint(null, constraint, null);
                }
        ));

        RegisterConstraintSolver solver = new RegisterConstraintSolver(body, null, new ArrayList<>(constraintMap.values()));
        Set<RegisterConstraint> collisions = solver.getParameterCollisions();

        for (ConstraintDsl constraint : constraints) {
            assertEquals(doesCollide, collisions.contains(constraintMap.get(constraint)));
        }
    }

    private DexBody createBodyWithRegisters(boolean hasThisRegister, int paramCount, int registerCount) {
        DexBody body = new DexBody();
        Supplier<Register> newRegister = () -> {
            Register register = new Register(null);
            body.getRegisters().add(register);
            return register;
        };

        for (int i = 0; i < registerCount; i++) {
            newRegister.get();
        }

        body.setThisRegister(hasThisRegister ? Optional.of(newRegister.get()) : Optional.empty());

        body.setParameterRegisters(IntStream.range(0, paramCount)
                .mapToObj(i -> newRegister.get())
                .collect(Collectors.toList()));
        return body;
    }

    @FunctionalInterface
    interface ConstraintDsl {
        List<Register> getConstraint(Register[] register, Register thisReg, Register[] arg);
    }

    @Test
    public void testOverlapComputation() {
        // no overlap at all
        overlapTest(
                new int[] { 0, 1,      },
                new int[] {       2, 3 },
                null
        );

        // Both ranges have some elements in common but do not overlap

        overlapTest(
                new int[] {    1, 2, 3, 4, 5 },
                new int[] { 0, 1, 9, 3, 4,   },
                null
        );

        overlapTest(
                new int[] {    1, 2, 3 },
                new int[] { 0, 9, 2,   },
                null
        );

        // There are multiple overlapping areas, so there is not one single overlap

        overlapTest(
                new int[] { 1,    3 },
                new int[] { 1, 2, 3 },
                null
        );

        overlapTest(
                new int[] {    1,    3, 4, 5 },
                new int[] { 0, 1, 2, 3, 4, 5 },
                null
        );

        // One range fully contains the other

        overlapTest(
                new int[] { 0, 1, 2, 3 },
                new int[] {    1, 2,   },
                new int[] {    1, 2    }
        );

        overlapTest(
                new int[] {    1, 2,   },
                new int[] { 0, 1, 2, 3 },
                new int[] {    1, 2    }
        );

        // The head of one range overlaps the tail of the other range

        overlapTest(
                new int[] { 0, 1, 2, 3       },
                new int[] {       2, 3, 4, 5 },
                new int[] {       2, 3       }
        );

        overlapTest(
                new int[] {       2, 3, 4, 5 },
                new int[] { 0, 1, 2, 3       },
                new int[] {       2, 3       }
        );

        overlapTest(
                new int[] {    1, 2, 3, 4, 5 },
                new int[] { 0, 1, 2, 3, 4,   },
                new int[] {    1, 2, 3, 4,   }
        );
    }

    private void overlapTest(int[] a, int[] b, int[] expectedOverlap) {
        List<Register> registers = newRegisterList(a, b);
        DexBody body = new DexBody();
        body.getRegisters().addAll(registers);

        RegisterConstraintSolver solver = new RegisterConstraintSolver(body, null, List.of());

        InsertList<Register> rangeA = toRange(a, registers);
        InsertList<Register> rangeB = toRange(b, registers);
        InsertList<Register> actualOverlap = solver.getOverlap(rangeA, rangeB);

        assertArrayEquals(expectedOverlap, fromRange(registers, actualOverlap));
    }

    @Test
    public void testMerging() {
        testMerge(
                new int[] { 0, 1, 2, 3 },
                new int[] {    1, 2,   },
                new int[] { 0, 1, 2, 3 }
        );

        testMerge(
                new int[] { 0, 1, 2, 3       },
                new int[] {       2, 3, 4, 5 },
                new int[] { 0, 1, 2, 3, 4, 5 }
        );

        testMerge(
                new int[] {       2, 3, 4, 5 },
                new int[] { 0, 1, 2, 3       },
                new int[] { 0, 1, 2, 3, 4, 5 }
        );

        testMerge(
                new int[] {    1, 2,   },
                new int[] { 0, 1, 2, 3 },
                new int[] { 0, 1, 2, 3 }
        );
    }

    /**
     * Combine two overlapping lists of registers and verify the result.
     *
     * Overlapping means that they have one slice of numbers in common.
     * Except that slice, there should no numbers that both array have in common.
     *
     * @param a an array overlapping with b
     * @param b an array overlapping with a
     * @param expectedMergeResult the expected merged result
     */
    private void testMerge(int[] a, int[] b, int[] expectedMergeResult) {
        List<Register> registers = newRegisterList(a, b);
        DexBody body = new DexBody();
        body.getRegisters().addAll(registers);

        InsertList<Register> mergeInto = toRange(a, registers);
        InsertList<Register> range = toRange(b, registers);

        RegisterConstraintSolver solver = new RegisterConstraintSolver(body, null, List.of());
        solver.mergeIntoRange(mergeInto, range);

        int[] actualMergeResult = fromRange(registers, mergeInto);
        assertArrayEquals(expectedMergeResult, actualMergeResult);
    }

    private List<Register> newRegisterList(int[] a, int[] b) {
        int maxRegisterIndex = IntStream.concat(Arrays.stream(a), Arrays.stream(b)).max().getAsInt();
        return Stream.generate(() -> new Register(null)).limit(maxRegisterIndex + 1).collect(Collectors.toList());
    }

    private LinkedInsertList<Register> toRange(int[] registerIndices, List<Register> registers) {
        return registerIndices == null ? null :
                Arrays.stream(registerIndices).mapToObj(registers::get).collect(Collectors.toCollection(LinkedInsertList::new));
    }

    private int[] fromRange(List<Register> registers, InsertList<Register> range) {
        return range == null ? null : range.stream().mapToInt(registers::indexOf).toArray();
    }
}
