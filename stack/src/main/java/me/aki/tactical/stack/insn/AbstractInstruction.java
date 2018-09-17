package me.aki.tactical.stack.insn;

import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstruction implements Instruction {
    private List<InsnTypeAnnotation> typeAnnotations = new ArrayList<>();

    public List<InsnTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<InsnTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }
}
