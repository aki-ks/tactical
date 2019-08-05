package me.aki.tactical.dex.textifier;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;

import java.util.Map;
import java.util.Optional;

public class TextifyCtx {
    /**
     * Map locals to their name
     */
    private final Map<Register, String> registers;

    /**
     * Map instructions to the name of labels pointing at them.
     */
    private final Map<Instruction, String> labels;

    public TextifyCtx(Map<Register, String> registers, Map<Instruction, String> labels) {
        this.registers = registers;
        this.labels = labels;
    }

    /**
     * Get the name that was assigned to a register.
     *
     * @param register the register
     * @return the name used to textify it
     */
    public String getRegisterName(Register register) {
        String name = registers.get(register);
        if (name == null) {
            // The register was not part of the register list of the body.
            throw new IllegalArgumentException("Unknown register");
        }
        return name;
    }

    /**
     * Get the name of a label that points to a certain instruction.
     *
     * @param target instruction to be referenced
     * @return name of the label
     */
    public String getLabel(Instruction target) {
        String label = labels.get(target);
        if (label == null) {
            throw new IllegalArgumentException("Unlabeled instruction");
        }
        return label;
    }

    /**
     * Get the name of a label of an instruction if it is present.
     *
     * @param target instruction to be referenced
     * @return name of the label
     */
    public Optional<String> getLabelOpt(Instruction target) {
        return Optional.ofNullable(labels.get(target));
    }
}
