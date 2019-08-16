package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;

import java.util.*;

/**
 * Assign locals to indices based on the results of the {@link RegisterConstraintSolver}.
 *
 * All ranges will be encoded as sequence, then the remaining registers are inserted
 * and finally the this and parameter registers.
 */
public class RegisterIndexAssigner {
    private final DexBody body;
    private final RegisterConstraintSolver solver;

    private final Map<Register, Integer> registerMap = new HashMap<>();
    private int index = 0;

    public RegisterIndexAssigner(DexBody body, RegisterConstraintSolver solver) {
        this.body = body;
        this.solver = solver;

        assignAllRegisters();
    }

    private void assignAllRegisters() {
        Set<Register> unassignedRegisters = new HashSet<>(this.body.getRegisters());
        this.body.getThisRegister().ifPresent(unassignedRegisters::remove);
        this.body.getParameterRegisters().forEach(unassignedRegisters::remove);

        for (InsertList<Register> range : solver.getRanges()) {
            for (Register register : range) {
                appendRegister(register);
                unassignedRegisters.remove(register);
            }
        }

        for (Register register : unassignedRegisters) {
            appendRegister(register);
        }

        // Insert the this and parameter registers as the last registers of the method
        appendThisAndParameterRegisters();
    }

    private void appendThisAndParameterRegisters() {
        if (this.body.getThisRegister().isPresent()) {
            Register thisRegister = this.body.getThisRegister().get();
            appendRegister(thisRegister);
        }

        for (Register parameterRegister : this.body.getParameterRegisters()) {
            appendRegister(parameterRegister);
        }
    }

    private void appendRegister(Register register) {
        this.registerMap.put(register, this.index);
        this.index += isWideRegister(register) ? 2 : 1;
    }

    private boolean isWideRegister(Register register) {
        Type type = register.getType();
        return type instanceof LongType || type instanceof DoubleType;
    }

    public Map<Register, Integer> getRegisterMap() {
        return this.registerMap;
    }
}
