package me.aki.tactical.dex.utils;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.TryCatchBlock;
import me.aki.tactical.dex.insn.Instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CommonOperations {
    /**
     * Get a map from all registers to the instructions that read from them.
     *
     * @param body build the read map for this body
     * @return map from registers to writing instruction
     */
    public static Map<Register, Set<Instruction>> getReadMap(DexBody body) {
        Map<Register, Set<Instruction>> readMap = new HashMap<>();
        for (Instruction instruction : body.getInstructions()) {
            for (Register readRegister : instruction.getReadRegisters()) {
                readMap.computeIfAbsent(readRegister, x -> new HashSet<>()).add(instruction);
            }
        }
        return readMap;
    }

    /**
     * Get a map from all registers to the instructions that write from them.
     *
     * @param body build the write map for this body
     * @return map from registers to writing instruction
     */
    public static Map<Register, Set<Instruction>> getWriteMap(DexBody body) {
        Map<Register, Set<Instruction>> writeMap = new HashMap<>();
        for (Instruction instruction : body.getInstructions()) {
            instruction.getWrittenRegister().ifPresent(writtenRegister ->
                    writeMap.computeIfAbsent(writtenRegister, x -> new HashSet<>()).add(instruction));
        }
        return writeMap;
    }

    /**
     * Remove a {@link Register} from a body's {@link DexBody#getRegisters() register list} if it is no longer used anywhere.
     *
     * @param body body containing the register
     * @param register register to be removed
     * @return was the register removed
     */
    public static boolean removeLocalIfUnused(DexBody body, Register register) {
        if (body.getThisRegister().isPresent() && body.getThisRegister().get() ==register) {
            return false;
        }

        if (body.getParameterRegisters().contains(register)) {
            return false;
        }

        for (Instruction instruction : body.getInstructions()) {
            for (Register readRegister : instruction.getReadRegisters()) {
                if (readRegister == register) {
                    return false;
                }
            }
        }

        return body.getRegisters().remove(register);
    }
}
