package me.aki.tactical.dex.utils;

import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.insn.litmath.MulLitInstruction;
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

}
