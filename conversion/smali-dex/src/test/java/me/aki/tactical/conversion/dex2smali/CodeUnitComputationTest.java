package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.*;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeUnitComputationTest {
    @Test
    public void testAffectionMapForwardBranch() {
        List<InstructionProvider<? extends Instruction>> insns = Arrays.asList(
        /* 0 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 1 */ new Insn21tProvider(Opcode.IF_NEZ, null, null /* 4 */),
        /* 2 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 3 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 4 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 5 */ new Insn10xProvider(Opcode.RETURN_VOID)
        );

        OffsetCell ifnezCell = ((Insn21tProvider) insns.get(1)).getOffsetCell();
        ifnezCell.resolveTarget(insns.get(4));

        // ---------------------------------

        Map<InstructionProvider<? extends Instruction>, Set<OffsetCell>> affectedCells = new CodeUnitComputation(insns).getAffectionMap();
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(0), Set.of()));
        assertEquals(Set.of(ifnezCell), affectedCells.getOrDefault(insns.get(1), Set.of()));
        assertEquals(Set.of(ifnezCell), affectedCells.getOrDefault(insns.get(2), Set.of()));
        assertEquals(Set.of(ifnezCell), affectedCells.getOrDefault(insns.get(3), Set.of()));
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(4), Set.of()));
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(5), Set.of()));
    }

    @Test
    public void testAffectionMapBackwardsBranch() {
        List<InstructionProvider<? extends Instruction>> insns = Arrays.asList(
        /* 0 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 1 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 2 */ new Insn10xProvider(Opcode.RETURN_VOID),
        /* 3 */ new Insn21tProvider(Opcode.IF_NEZ, null, null /* 1 */),
        /* 4 */ new Insn10xProvider(Opcode.RETURN_VOID)
        );

        OffsetCell ifnezCell = ((Insn21tProvider) insns.get(3)).getOffsetCell();
        ifnezCell.resolveTarget(insns.get(1));

        // ---------------------------------

        Map<InstructionProvider<? extends Instruction>, Set<OffsetCell>> affectedCells = new CodeUnitComputation(insns).getAffectionMap();
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(0), Set.of()));
        assertEquals(Set.of(ifnezCell), affectedCells.getOrDefault(insns.get(1), Set.of()));
        assertEquals(Set.of(ifnezCell), affectedCells.getOrDefault(insns.get(2), Set.of()));
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(3), Set.of()));
        assertEquals(Set.of(), affectedCells.getOrDefault(insns.get(4), Set.of()));
    }

    @Test
    public void testCalculateOffsetEqualSize() {
        int insnSize = 1;
        List<InstructionProvider<? extends Instruction>> insns = Arrays.asList(
                /* 0 */ new Insn10xProvider(null),
                /* 1 */ new Insn10xProvider(null),
                /* 2 */ new Insn10xProvider(null)
        );

        CodeUnitComputation computation = new CodeUnitComputation(insns);
        assertEquals(insnSize *  0, computation.calculateOffset(insns.get(0), insns.get(0)));
        assertEquals(insnSize *  1, computation.calculateOffset(insns.get(0), insns.get(1)));
        assertEquals(insnSize *  2, computation.calculateOffset(insns.get(0), insns.get(2)));

        assertEquals(insnSize * -1, computation.calculateOffset(insns.get(1), insns.get(0)));
        assertEquals(insnSize *  0, computation.calculateOffset(insns.get(1), insns.get(1)));
        assertEquals(insnSize *  1, computation.calculateOffset(insns.get(1), insns.get(2)));

        assertEquals(insnSize * -2, computation.calculateOffset(insns.get(2), insns.get(0)));
        assertEquals(insnSize * -1, computation.calculateOffset(insns.get(2), insns.get(1)));
        assertEquals(insnSize *  0, computation.calculateOffset(insns.get(2), insns.get(2)));
    };

    @Test
    public void testCalculateOffsetMixedSize() {
        List<InstructionProvider<? extends Instruction>> insns = Arrays.asList(
            /* 0 */ new Insn10xProvider(null), // Size 1
            /* 1 */ new Insn21cProvider(null, null, null), // Size 2
            /* 2 */ new Insn31iProvider(null, null, 0), // Size 3
            /* 3 */ new Insn10xProvider(null)
        );

        CodeUnitComputation computation = new CodeUnitComputation(insns);
        assertEquals(0, computation.calculateOffset(insns.get(0), insns.get(0)));
        assertEquals(1, computation.calculateOffset(insns.get(0), insns.get(1)));
        assertEquals(1 + 2, computation.calculateOffset(insns.get(0), insns.get(2)));
        assertEquals(1 + 2 + 3, computation.calculateOffset(insns.get(0), insns.get(3)));

        assertEquals(-1, computation.calculateOffset(insns.get(1), insns.get(0)));
        assertEquals(0, computation.calculateOffset(insns.get(1), insns.get(1)));
        assertEquals(2, computation.calculateOffset(insns.get(1), insns.get(2)));
        assertEquals(2 + 3, computation.calculateOffset(insns.get(1), insns.get(3)));

        assertEquals(-1 + -2, computation.calculateOffset(insns.get(2), insns.get(0)));
        assertEquals(-2, computation.calculateOffset(insns.get(2), insns.get(1)));
        assertEquals(0, computation.calculateOffset(insns.get(2), insns.get(2)));
        assertEquals(3, computation.calculateOffset(insns.get(2), insns.get(3)));
    }
}
