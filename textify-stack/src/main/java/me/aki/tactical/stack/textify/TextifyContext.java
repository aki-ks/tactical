package me.aki.tactical.stack.textify;

import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.Instruction;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class TextifyContext {
    private final StackBody body;

    /**
     * Map locals to their name used to identify them in the textified code.
     */
    private final Map<StackLocal, String> localNames = new IdentityHashMap<>();

    /**
     * Map instructions to labels names uniquely identifying it.
     */
    private final Map<Instruction, String> labels = new IdentityHashMap<>();

    public TextifyContext(StackBody body) {
        this.body = body;
    }

    public String getLocalName(StackLocal local) {
        return localNames.get(local);
    }

    public void setLocalName(StackLocal local, String name) {
        localNames.put(local, name);
    }

    public boolean isLocalNamed(StackLocal local) {
        return localNames.containsKey(local);
    }

    public Map<StackLocal, String> getLocalNames() {
        return localNames;
    }

    public Optional<String> getLabelOpt(Instruction instruction) {
        return Optional.ofNullable(labels.get(instruction));
    }

    public String getLabel(Instruction instruction) {
        String label = labels.get(instruction);
        if (label == null) {
            throw new IllegalArgumentException("There's no label name assigned to the instruction");
        }
        return label;
    }

    public void setLabel(Instruction instruction, String name) {
        labels.put(instruction, name);
    }
}
