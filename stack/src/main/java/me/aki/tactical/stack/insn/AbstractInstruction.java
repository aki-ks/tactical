package me.aki.tactical.stack.insn;

import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractInstruction implements Instruction {
    private List<InsnTypeAnnotation> typeAnnotations = new ArrayList<>();

    public List<InsnTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<InsnTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractInstruction that = (AbstractInstruction) o;
        return Objects.equals(typeAnnotations, that.typeAnnotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeAnnotations);
    }
}
