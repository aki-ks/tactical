package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.DexNumber32Constant;
import me.aki.tactical.core.constant.DexNumber64Constant;
import me.aki.tactical.core.constant.StringConstant;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.invoke.InvokeStatic;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DexTypeAnalysisTest {
    @Test
    public void testBasicTyping() {
        MethodRef intMethod = new MethodRef(Path.of("Test"), "foo", List.of(IntType.getInstance()), Optional.empty());
        MethodRef longMethod = new MethodRef(Path.of("Test"), "foo", List.of(LongType.getInstance()), Optional.empty());
        MethodRef stringMethod = new MethodRef(Path.of("Test"), "foo", List.of(new ObjectType(Path.of("java", "lang", "String"))), Optional.empty());

        Register registerA = new Register(null);
        Register registerB = new Register(null);
        Register registerC = new Register(null);

        DexBody body = new DexBody();
        List<Instruction> insns = body.getInstructions();
        /* 0 */ insns.add(new ConstInstruction(new DexNumber32Constant(10), registerA));
        /* 1 */ insns.add(new ConstInstruction(new DexNumber64Constant(20L), registerB));
        /* 2 */ insns.add(new ConstInstruction(new StringConstant("foo"), registerC));
        /* 3 */ insns.add(new InvokeInstruction(new InvokeStatic(intMethod, List.of(registerA))));
        /* 4 */ insns.add(new InvokeInstruction(new InvokeStatic(longMethod, List.of(registerB))));
        /* 5 */ insns.add(new InvokeInstruction(new InvokeStatic(stringMethod, List.of(registerC))));
        /* 6 */ insns.add(new ReturnVoidInstruction());

        DexTypeAnalysis analysis = new DexTypeAnalysis(new DexCfgGraph(body));


        DexTypeAnalysis.RegisterState state;
        state = analysis.getRegisterStates(insns.get(0));
        assertState(state, registerA, Set.of(), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of());
        assertState(state, registerC, Set.of(), Set.of());

        state = analysis.getRegisterStates(insns.get(1));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of(DexType.WIDE));
        assertState(state, registerC, Set.of(), Set.of());

        state = analysis.getRegisterStates(insns.get(2));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(DexType.WIDE), Set.of(DexType.WIDE));
        assertState(state, registerC, Set.of(), Set.of(DexType.OBJECT));

        for (int i = 3; i <= 6; i++) {
            state = analysis.getRegisterStates(insns.get(i));
            assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
            assertState(state, registerB, Set.of(DexType.WIDE), Set.of(DexType.WIDE));
            assertState(state, registerC, Set.of(DexType.OBJECT), Set.of(DexType.OBJECT));
        }
    }

    @Test
    public void testBasicBranchTyping() {
        FieldRef fieldRef = new FieldRef(Path.of("Test"), "foo", IntType.getInstance());

        Register registerA = new Register(null);

        DexBody body = new DexBody();
        List<Instruction> insns = body.getInstructions();
        /* 0 */ insns.add(new FieldGetInstruction(fieldRef, Optional.empty(), registerA));
        /* 1 */ insns.add(new IfInstruction(IfInstruction.Comparison.EQUAL, registerA, Optional.empty(), null /* 4 */));

        /* 2 */ insns.add(new ConstInstruction(new DexNumber64Constant(10), registerA));
        /* 3 */ insns.add(new ReturnVoidInstruction());

        /* 4 */ insns.add(new ConstInstruction(new StringConstant("foo"), registerA));
        /* 5 */ insns.add(new ReturnVoidInstruction());

        ((IfInstruction) insns.get(1)).setTarget(insns.get(4));

        DexTypeAnalysis analysis = new DexTypeAnalysis(new DexCfgGraph(body));

        DexTypeAnalysis.RegisterState state;
        state = analysis.getRegisterStates(insns.get(0));
        assertState(state, registerA, Set.of(), Set.of(DexType.NORMAL));

        state = analysis.getRegisterStates(insns.get(1));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));

        state = analysis.getRegisterStates(insns.get(2));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.WIDE));

        state = analysis.getRegisterStates(insns.get(3));
        assertState(state, registerA, Set.of(DexType.WIDE), Set.of(DexType.WIDE));

        state = analysis.getRegisterStates(insns.get(4));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.OBJECT));

        state = analysis.getRegisterStates(insns.get(5));
        assertState(state, registerA, Set.of(DexType.OBJECT), Set.of(DexType.OBJECT));
    }

    @Test
    public void testMergeBranchTyping() {
        FieldRef fieldRef = new FieldRef(Path.of("Test"), "foo", IntType.getInstance());

        Register registerA = new Register(null);
        Register registerB = new Register(null);

        DexBody body = new DexBody();
        List<Instruction> insns = body.getInstructions();
        /* 0 */ insns.add(new FieldGetInstruction(fieldRef, Optional.empty(), registerA));
        /* 1 */ insns.add(new IfInstruction(IfInstruction.Comparison.EQUAL, registerA, Optional.empty(), null /* 4 */));

        /* 2 */ insns.add(new ConstInstruction(new DexNumber64Constant(10), registerB));
        /* 3 */ insns.add(new GotoInstruction(null /* 5 */));

        /* 4 */ insns.add(new ConstInstruction(new DexNumber32Constant(10), registerB));
        /* 5 */ insns.add(new ReturnVoidInstruction());

        ((IfInstruction) insns.get(1)).setTarget(insns.get(4));
        ((GotoInstruction) insns.get(3)).setTarget(insns.get(5));

        DexTypeAnalysis analysis = new DexTypeAnalysis(new DexCfgGraph(body));

        DexTypeAnalysis.RegisterState state;
        state = analysis.getRegisterStates(insns.get(0));
        assertState(state, registerA, Set.of(), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of());

        state = analysis.getRegisterStates(insns.get(1));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of());

        state = analysis.getRegisterStates(insns.get(2));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of(DexType.WIDE));

        state = analysis.getRegisterStates(insns.get(3));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(DexType.WIDE), Set.of(DexType.WIDE));

        state = analysis.getRegisterStates(insns.get(4));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(), Set.of(DexType.NORMAL));

        state = analysis.getRegisterStates(insns.get(5));
        assertState(state, registerA, Set.of(DexType.NORMAL), Set.of(DexType.NORMAL));
        assertState(state, registerB, Set.of(DexType.WIDE, DexType.NORMAL), Set.of(DexType.WIDE, DexType.NORMAL));
    }

    private static void assertState(DexTypeAnalysis.RegisterState state, Register register, Set<DexType> before, Set<DexType> after) {
        assertEquals(before, state.getTypesBefore(register));
        assertEquals(after, state.getTypesAfter(register));
    }
}
