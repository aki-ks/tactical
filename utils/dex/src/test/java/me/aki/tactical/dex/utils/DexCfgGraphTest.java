package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.FloatConstant;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.insn.litmath.MulLitInstruction;
import me.aki.tactical.dex.invoke.InvokeVirtual;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DexCfgGraphTest {
    @Test
    public void testRemovalOfNonBranchingInstruction() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(4), register);
        Instruction insn1 = new MulLitInstruction(register, (short) 2, register);
        Instruction insn2 = new ReturnInstruction(register);

        instructions.addAll(List.of(insn0, insn1, insn2));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        // 0
        // |
        // 1 ← To be removed
        // |
        // 2
        assertInstructions(cfg, List.of(insn0, insn1, insn2));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of());

        cfg.remove(cfg.getNode(insn1));

        // AFTER //
        // 0
        // |
        // 2
        assertInstructions(cfg, List.of(insn0, insn2));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn0), Set.of());
        //
    }

    @Test
    public void testRemovalOfBranchInstruction() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(4), register);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        Instruction insn2 = new ConstInstruction(new IntConstant(5), register);
        Instruction insn3 = new ReturnInstruction(register);
        insn1.setTarget(insn3);

        instructions.addAll(List.of(insn0, insn1, insn2, insn3));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        // 0
        // |
        // 1 ← To be removed
        // | \
        // 2  |
        // | /
        // 3
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn2, insn1), Set.of());

        cfg.remove(cfg.getNode(insn1));

        // AFTER //
        // 0
        // |
        // 2
        // |
        // 3
        assertInstructions(cfg, List.of(insn0, insn2, insn3));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn0), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn2), Set.of());
    }

    @Test
    public void testRemovalOfBranchedToInstruction() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        IfInstruction insn0 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.GREATER_EQUAL, register, Optional.empty(), null);
        Instruction insn2 = new ConstInstruction(new IntConstant(-1), register);
        Instruction insn3 = new ConstInstruction(new IntConstant(5), register);
        Instruction insn4 = new ReturnInstruction(register);
        insn0.setTarget(insn3);
        insn1.setTarget(insn3);

        instructions.addAll(List.of(insn0, insn1, insn2, insn3, insn4));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    3 ← To be removed
        //    |
        //    4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn3));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn0, insn1, insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());

        cfg.remove(cfg.getNode(insn3));

        // After //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn4));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn4));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn0, insn1, insn2), Set.of());
    }

    @Test
    public void testRemovalOfBranchedToBranchInstruction() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        IfInstruction insn0 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.GREATER_EQUAL, register, Optional.empty(), null);
        Instruction insn2 = new ConstInstruction(new IntConstant(-1), register);
        IfInstruction insn3 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        Instruction insn4 = new ReturnInstruction(register);
        insn0.setTarget(insn3);
        insn1.setTarget(insn3);
        insn3.setTarget(insn0);

        instructions.addAll(List.of(insn0, insn1, insn2, insn3, insn4));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE //
        //    .— 0 ← To be removed
        //  /    | \
        // |     1  |
        // |   / |  |
        // |  |  2  |
        //  \  \ | /
        //   `—— 3
        //       |
        //       4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4));
        assertCfgNode(cfg, insn0, Set.of(insn3), Set.of(insn1, insn3));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn0, insn1, insn2), Set.of(insn0, insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());

        cfg.remove(cfg.getNode(insn0));

        // After //
        //   . — 1
        //  /  / |
        // |  |  2
        //  \  \ |
        //   `—— 3
        //       |
        //       4

        assertInstructions(cfg, List.of(insn1, insn2, insn3, insn4));
        assertCfgNode(cfg, insn1, Set.of(insn3), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn1, insn2), Set.of(insn1, insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());
    }

    @Test
    public void testSwitchRemoval() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(0), register);
        SwitchInstruction insn1 = new SwitchInstruction(register, new LinkedHashMap<>());
        Instruction insn2 = new ConstInstruction(new IntConstant(3), register);
        Instruction insn3 = new ConstInstruction(new IntConstant(4), register);
        Instruction insn4 = new ReturnInstruction(register);
        insn1.getBranchTable().put(-1, insn3);
        insn1.getBranchTable().put(1, insn4);

        instructions.addAll(List.of(insn0, insn1, insn2, insn3, insn4));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        //    0
        //    |
        //    1 ← To be removed
        //  / | \
        // |  2  |
        //  \ |  |
        //    3  |
        //    | /
        //    4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3, insn4));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn1, insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn1, insn3), Set.of());

        cfg.remove(cfg.getNode(insn1));

        // AFTER
        //    0
        //    |
        //    2
        //    |
        //    3
        //    |
        //    4
        assertInstructions(cfg, List.of(insn0, insn2, insn3, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn0), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());
    }

    /**
     * Assert that the body and CFG contain a list of instructions in that order.
     *
     * @param cfg the control flow graph
     * @param insns the instructions in the expected order
     */
    private void assertInstructions(DexCfgGraph cfg, List<Instruction> insns) {
        assertEquals(insns, cfg.getBody().getInstructions());
        assertEquals(Set.copyOf(insns), cfg.getNodes().stream().map(AbstractCfgGraph.Node::getInstruction).collect(Collectors.toSet()));
    }

    /**
     * Make assertions about the predecessors and successors of an instruction in the control flow graph.
     *
     * @param cfg the control flow graph
     * @param instruction the instructions we make assertions of
     * @param preceding instructions the precede the instruction
     * @param succeeding instructions the succeed the instruction
     */
    private void assertCfgNode(DexCfgGraph cfg, Instruction instruction, Set<Instruction> preceding, Set<Instruction> succeeding) {
        DexCfgGraph.Node node = cfg.getNode(instruction);
        assertEquals(preceding, node.getPreceding().stream().map(DexCfgGraph.Node::getInstruction).collect(Collectors.toSet()));
        assertEquals(succeeding, node.getSucceeding().stream().map(DexCfgGraph.Node::getInstruction).collect(Collectors.toSet()));
    }

    private DexBody newBody(int registers, boolean hasThisRegister, int parameterCount) {
        DexBody body = new DexBody();

        Supplier<Register> newRegister = () -> {
            Register register = new Register(null);
            body.getRegisters().add(register);
            return register;
        };

        for (int i = 0; i < registers; i++) {
            newRegister.get();
        }

        if (hasThisRegister) {
            body.setThisRegister(Optional.of(newRegister.get()));
        }

        for (int i = 0; i < parameterCount; i++) {
            body.getParameterRegisters().add(newRegister.get());
        }

        return body;
    }

    @Test
    public void testSimpleInsertBefore() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(0), register);
        Instruction insn1 = new ConstInstruction(new IntConstant(4), register); // to be inserted
        Instruction insn2 = new ReturnInstruction(register);

        instructions.addAll(List.of(insn0, insn2));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        //    0
        //    |
        //    2
        assertInstructions(cfg, List.of(insn0, insn2));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn0), Set.of());

        cfg.insertBefore(cfg.getNode(insn2), insn1);

        // AFTER
        //    0
        //    |
        //    1
        //    |
        //    2
        assertInstructions(cfg, List.of(insn0, insn1, insn2));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of());
    }

    @Test
    public void testInsertBeforeFirstInstruction() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(0), register); // to be inserted
        Instruction insn1 = new ConstInstruction(new IntConstant(4), register);
        Instruction insn2 = new ReturnInstruction(register);

        instructions.addAll(List.of(insn1, insn2));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        //    1
        //    |
        //    2
        assertInstructions(cfg, List.of(insn1, insn2));
        assertCfgNode(cfg, insn1, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of());

        cfg.insertBefore(cfg.getNode(insn1), insn0);

        // AFTER
        //    0
        //    |
        //    1
        //    |
        //    2
        assertInstructions(cfg, List.of(insn0, insn1, insn2));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of());
    }

    @Test
    public void testInsertBeforeBranchedToInsn() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        IfInstruction insn0 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.GREATER_EQUAL, register, Optional.empty(), null);
        Instruction insn2 = new ConstInstruction(new IntConstant(-1), register);
        Instruction insn3 = new ConstInstruction(new IntConstant(5), register); // To be inserted
        Instruction insn4 = new ReturnInstruction(register);
        insn0.setTarget(insn4);
        insn1.setTarget(insn4);

        instructions.addAll(List.of(insn0, insn1, insn2, insn4));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn4));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn4));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn0, insn1, insn2), Set.of());

        cfg.insertBefore(cfg.getNode(insn4), insn3);

        // After //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    3
        //    |
        //    4
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn3));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn0, insn1, insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());

        assertEquals(insn3, insn0.getTarget());
        assertEquals(insn3, insn1.getTarget());
    }

    @Test
    public void testInsertBranchInsn() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        Instruction insn0 = new ConstInstruction(new IntConstant(0), register);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null); // to be inserted
        Instruction insn2 = new ConstInstruction(new IntConstant(4), register);
        Instruction insn3 = new ReturnInstruction(register);
        insn1.setTarget(insn3);

        instructions.addAll(List.of(insn0, insn2, insn3));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE
        //    0
        //    |
        //    2
        //    |
        //    3
        assertInstructions(cfg, List.of(insn0, insn2, insn3));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn0), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn2), Set.of());

        cfg.insertBefore(cfg.getNode(insn2), insn1);

        // AFTER
        //    0
        //    |
        //    1
        //    | \
        //    2  |
        //    | /
        //    3
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn1, insn2), Set.of());
        assertEquals(insn3, insn1.getTarget());
    }

    @Test
    public void testInsertBranchInstructionBeforeBranchedToInsn() {
        DexBody body = newBody(1, false, 0);
        Register register = body.getRegisters().get(0);
        InsertList<Instruction> instructions = body.getInstructions();

        IfInstruction insn0 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);
        IfInstruction insn1 = new IfInstruction(IfInstruction.Comparison.GREATER_EQUAL, register, Optional.empty(), null);
        Instruction insn2 = new ConstInstruction(new IntConstant(-1), register);
        IfInstruction insn3 = new IfInstruction(IfInstruction.Comparison.EQUAL, register, Optional.empty(), null);  // To be inserted
        Instruction insn4 = new ConstInstruction(new IntConstant(5), register);
        Instruction insn5 = new ReturnInstruction(register);
        insn0.setTarget(insn4);
        insn1.setTarget(insn4);
        insn3.setTarget(insn5);

        instructions.addAll(List.of(insn0, insn1, insn2, insn4, insn5));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    4
        //    |
        //    5
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn4, insn5));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn4));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn4));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn0, insn1, insn2), Set.of(insn5));
        assertCfgNode(cfg, insn5, Set.of(insn4), Set.of());

        cfg.insertBefore(cfg.getNode(insn4), insn3);

        // After //
        //    0
        //    | \
        //    1  |
        //  / |  |
        // |  2  |
        //  \ | /
        //    3
        //    | \
        //    4  |
        //    | /
        //    5
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4, insn5));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1, insn3));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2, insn3));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3));
        assertCfgNode(cfg, insn3, Set.of(insn0, insn1, insn2), Set.of(insn4, insn5));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of(insn5));
        assertCfgNode(cfg, insn5, Set.of(insn3, insn4), Set.of());
        assertEquals(insn3, insn0.getTarget());
        assertEquals(insn3, insn1.getTarget());
        assertEquals(insn5, insn3.getTarget());
    }

    @Test
    public void foo() {
        DexBody body = newBody(2, false, 0);
        Register out = body.getRegisters().get(0);
        Register number = body.getRegisters().get(1);
        InsertList<Instruction> instructions = body.getInstructions();

        Path printStream = Path.of("java", "io", "PrintStream");

        Instruction insn0 = new FieldGetInstruction(new FieldRef(Path.of("java", "lang", "System"), "out", new ObjectType(printStream)), Optional.empty(), out);
        Instruction insn1 = new ConstInstruction(new IntConstant(0), number);
        IfInstruction insn2 = new IfInstruction(IfInstruction.Comparison.EQUAL, out, Optional.empty(), null);

        Instruction insn3 = new InvokeInstruction(new InvokeVirtual(new MethodRef(printStream, "println", List.of(IntType.getInstance()), Optional.of(IntType.getInstance())), out, List.of(number)));
        Instruction insn4 = new ReturnVoidInstruction();

        Instruction insn5 = new ConstInstruction(new FloatConstant(0.0f), number);
        Instruction insn6 = new InvokeInstruction(new InvokeVirtual(new MethodRef(printStream, "println", List.of(FloatType.getInstance()), Optional.of(IntType.getInstance())), out, List.of(number)));
        Instruction insn7 = new ReturnVoidInstruction();

        insn2.setTarget(insn6);

        instructions.addAll(List.of(insn0, insn1, insn2, insn3, insn4, insn6, insn7));
        DexCfgGraph cfg = new DexCfgGraph(body);

        // BEFORE //
        // 0
        // |
        // 1
        // |
        // 2
        // | \
        // 3  6
        // |  |
        // 4  7
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4, insn6, insn7));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3, insn6));
        assertCfgNode(cfg, insn3, Set.of(insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());
        assertCfgNode(cfg, insn6, Set.of(insn2), Set.of(insn7));
        assertCfgNode(cfg, insn7, Set.of(insn6), Set.of());

        cfg.insertBefore(cfg.getNode(insn6), insn5);

        // AFTER //
        // 0
        // |
        // 1
        // |
        // 2
        // | \
        // 3  5
        // |  |
        // 4  6
        //    |
        //    7
        assertInstructions(cfg, List.of(insn0, insn1, insn2, insn3, insn4, insn5, insn6, insn7));
        assertCfgNode(cfg, insn0, Set.of(), Set.of(insn1));
        assertCfgNode(cfg, insn1, Set.of(insn0), Set.of(insn2));
        assertCfgNode(cfg, insn2, Set.of(insn1), Set.of(insn3, insn5));
        assertCfgNode(cfg, insn3, Set.of(insn2), Set.of(insn4));
        assertCfgNode(cfg, insn4, Set.of(insn3), Set.of());
        assertCfgNode(cfg, insn5, Set.of(insn2), Set.of(insn6));
        assertCfgNode(cfg, insn6, Set.of(insn5), Set.of(insn7));
        assertCfgNode(cfg, insn7, Set.of(insn6), Set.of());
        assertEquals(insn5, insn2.getTarget());
    }
}
