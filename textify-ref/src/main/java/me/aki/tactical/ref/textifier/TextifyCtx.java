package me.aki.tactical.ref.textifier;

import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;

import java.util.Map;
import java.util.Optional;

public class TextifyCtx {
    /**
     * Map locals to their name
     */
    private final Map<RefLocal, String> locals;

    /**
     * Map instructions to the name of labels pointing at them.
     */
    private final Map<Statement, String> labels;

    public TextifyCtx(Map<RefLocal, String> locals, Map<Statement, String> labels) {
        this.locals = locals;
        this.labels = labels;
    }

    /**
     * Get the name that was assigned to a local.
     *
     * @param local the local
     * @return the name used to textify it
     */
    public String getLocalName(RefLocal local) {
        String name = locals.get(local);
        if (name == null) {
            // The local was not part of the local list of the body.
            throw new IllegalArgumentException("Unknown local");
        }
        return name;
    }

    /**
     * Get the name of a label that points to a certain instruction.
     *
     * @param target statement to be referenced
     * @return name of the label
     */
    public String getLabel(Statement target) {
        String label = labels.get(target);
        if (label == null) {
            throw new IllegalArgumentException("Unlabeled instruction");
        }
        return label;
    }

    /**
     * Get the name of a label if present.
     *
     * @param target statement to be referenced
     * @return name of the label
     * @see #getLabel(Statement)
     */
    public Optional<String> getLabelOpt(Statement target) {
        return Optional.ofNullable(labels.get(target));
    }
}
