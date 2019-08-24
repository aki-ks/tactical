package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.CommonOperations;
import me.aki.tactical.dex.utils.DexCfgGraph;

import java.util.List;
import java.util.Set;

/**
 * If a register is not used at a certain location in code, it may get reused there.
 * The register may contain values of different types at these locations which makes them impossible to type.
 *
 * This utility detects such reuses and partitions them.
 * Each of these independent usage groups gets its own local which can then get typed.
 *
 * Here's pseudocode for one example local that would get split:
 * <pre><code>
 *     // Group 1
 *     if (...) {
 *         x = 10;
 *     } else {
 *         x = 30;
 *     }
 *     System.out.println(x);
 *
 *     // Group 2
 *     if (...) {
 *         x = "a";
 *     } else {
 *         x = "b";
 *     }
 *     System.out.println(x);
 * </code></pre>
 *
 * First the local <tt>x</tt> is an <tt>int</tt>, then it is a <tt>String</tt>.
 * That makes assigning a type to <tt>x</tt> impossible.
 *
 * Since it is not possible to access values assigned to <tt>x</tt> by the first group from the second group,
 * all uses of <tt>x</tt> in the second group can be replaced against a new variable.
 */
public class RegisterPartitioner {
    private final DexCfgGraph dexCfgGraph;

    public RegisterPartitioner(DexCfgGraph dexCfgGraph) {
        this.dexCfgGraph = dexCfgGraph;
    }

    public void process(DexBody body) {
        RegisterStateAnalysis registerStateAnalysis = new RegisterStateAnalysis(dexCfgGraph);

        // the register list may get modified during the blow iteration
        List<Register> registerListCopy = List.copyOf(body.getRegisters());

        for (Register register : registerListCopy) {
            RegisterStateAnalysis.RegisterStates registerState = registerStateAnalysis.getRegisterState(register);
            for (Set<RegisterStateAnalysis.RegisterState> group : registerState.getGroups()) {
                processGroup(body, registerState, register, group);
            }
        }
    }

    private void processGroup(DexBody body, RegisterStateAnalysis.RegisterStates registerState, Register register, Set<RegisterStateAnalysis.RegisterState> group) {
        Register newRegister = getRegisterForGroup(body, group);

        // Update the assign instructions
        for (RegisterStateAnalysis.RegisterState state : group) {
            if (state instanceof RegisterStateAnalysis.RegisterState.Assignment) {
                Instruction instruction = state.getNode().getInstruction();
                instruction.getWrittenRegisterCell().get().set(newRegister);
            }
        }

        // Update all instructions in the group that read from the register
        for (RegisterStateAnalysis.RegisterState state : group) {
            for (Instruction instruction : registerState.getInstructions(state)) {
                for (RWCell<Register> cell : instruction.getReadRegisterCells()) {
                    if (cell.get() == register) {
                        cell.set(newRegister);
                    }
                }
            }
        }

        CommonOperations.removeLocalIfUnused(body, register);
    }

    /**
     * Create a new local for a group.
     *
     * If the group contains the this or a parameter value, the corresponding register must be used instead
     *
     * @param body the body containing the registers and instructions
     * @param group the group for which we want a new register
     * @return either a new or a this or parameter register
     */
    private Register getRegisterForGroup(DexBody body, Set<RegisterStateAnalysis.RegisterState> group) {
        for (RegisterStateAnalysis.RegisterState registerState : group) {
            if (registerState instanceof RegisterStateAnalysis.RegisterState.This) {
                return body.getThisRegister().get();
            } else if (registerState instanceof RegisterStateAnalysis.RegisterState.Parameter) {
                int index = ((RegisterStateAnalysis.RegisterState.Parameter) registerState).getIndex();
                return body.getParameterRegisters().get(index);
            }
        }

        Register register = new Register(null);
        body.getRegisters().add(register);
        return register;
    }
}
