package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.asm2stack.visitor.AsmInsnReader;
import me.aki.tactical.conversion.asm2stack.visitor.InsnWriter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import me.aki.tactical.core.typeannotation.TypePath;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BodyConverter {
    private final Classfile classfile;
    private final Method method;
    private final StackBody body;
    private final MethodNode mn;

    private final ConversionContext ctx = new ConversionContext();
    private Frame<BasicValue>[] frames;

    private final Map<AbstractInsnNode, List<Instruction>> convertedInsns = new HashMap<>();

    public BodyConverter(Classfile classfile, Method method, StackBody body, MethodNode mn) {
        this.classfile = classfile;
        this.method = method;
        this.body = body;
        this.mn = mn;
    }

    private void initLocals() {
        for (int i = 0; i < mn.maxLocals; i++) {
            ctx.getLocals().add(new Local());
        }

        int localIndex = 0;
        if (!this.method.getFlag(Method.Flag.STATIC)) {
            this.body.setThisLocal(Optional.of(ctx.getLocal(localIndex++)));
        }

        for (Type paramType : this.method.getParameterTypes()) {
            this.body.getParameterLocals().add(ctx.getLocal(localIndex++));
        }
    }

    public void convert() {
        initLocals();

        runAsmClassAnalysis();
        convertInsns();

        updateInsnCells();
    }

    private void runAsmClassAnalysis() {
        String owner = classfile.getName().join('/');
        try {
            Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
            this.frames = analyzer.analyze(owner, mn);
        } catch (AnalyzerException e) {
            throw new RuntimeException("Asm cannot analyze method " + owner + "#" + mn.name + mn.desc);
        }
    }

    private void convertInsns() {
        InsnWriter iw = new InsnWriter(ctx);
        AsmInsnReader reader = new AsmInsnReader(iw, ctx);

        InsnList instructions = mn.instructions;
        for (int index = 0; index < instructions.size(); index++) {
            AbstractInsnNode insn = instructions.get(index);
            Frame<BasicValue> frame = this.frames[index];
            if (frame == null) {
                // this instruction is dead code
                continue;
            }

            reader.accept(insn, frame);

            List<Instruction> convertedInsns = iw.getInstructions();

            convertInsnTypeAnnotations(insn, convertedInsns.get(0));

            this.convertedInsns.put(insn, new ArrayList<>(convertedInsns));
            this.body.getInstructions().addAll(convertedInsns);
            convertedInsns.clear();
        }
    }

    private void convertInsnTypeAnnotations(AbstractInsnNode insn, Instruction instruction) {
        if (insn.visibleTypeAnnotations != null) {
            for (TypeAnnotationNode anno : insn.visibleTypeAnnotations) {
                instruction.getTypeAnnotations().add(convertInsnTypeAnnotation(anno, true));
            }
        }

        if (insn.invisibleTypeAnnotations != null) {
            for (TypeAnnotationNode anno : insn.invisibleTypeAnnotations) {
                instruction.getTypeAnnotations().add(convertInsnTypeAnnotation(anno, false));
            }
        }
    }

    private InsnTypeAnnotation convertInsnTypeAnnotation(TypeAnnotationNode typeAnnotation, boolean visible) {
        TypePath typePath = AsmUtil.fromAsmTypePath(typeAnnotation.typePath);
        TargetType.InsnTargetType targetType = convertInsnTargetType(new TypeReference(typeAnnotation.typeRef));

        Annotation annotation = new Annotation(AsmUtil.pathFromInternalName(typeAnnotation.desc), visible);
        typeAnnotation.accept(new AnnotationConvertVisitor(null, annotation));

        return new InsnTypeAnnotation(typePath, annotation, targetType);
    }

    private TargetType.InsnTargetType convertInsnTargetType(TypeReference tref) {
        switch (tref.getSort()) {
            case TypeReference.INSTANCEOF:
                return new TargetType.InstanceOf();

            case TypeReference.NEW:
                return new TargetType.New();

            case TypeReference.CONSTRUCTOR_REFERENCE:
                return new TargetType.ConstructorReference();

            case TypeReference.METHOD_REFERENCE:
                return new TargetType.MethodReference();

            case TypeReference.CAST:
                return new TargetType.Cast(tref.getTypeArgumentIndex());

            case TypeReference.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
                return new TargetType.ConstructorInvokeTypeParameter(tref.getTypeArgumentIndex());

            case TypeReference.METHOD_INVOCATION_TYPE_ARGUMENT:
                return new TargetType.MethodInvokeTypeParameter(tref.getTypeArgumentIndex());

            case TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
                return new TargetType.ConstructorReferenceTypeParameter(tref.getTypeArgumentIndex());

            case TypeReference.METHOD_REFERENCE_TYPE_ARGUMENT:
                return new TargetType.MethodReferenceTypeParameter(tref.getTypeArgumentIndex());

            default:
                throw new AssertionError();
        }
    }

    private void updateInsnCells() {
        ctx.getLabelCells().forEach((label, insnCells) -> {
            List<Instruction> insns;

            AbstractInsnNode node = label;
            while (true) {
                insns = convertedInsns.get(node);
                if (insns != null && !insns.isEmpty()) {
                    break;
                }

                if ((node = node.getNext()) == null) {
                    throw new RuntimeException("Label could not be resolved");
                }
            }

            for (Cell<Instruction> cell : insnCells) {
                cell.set(insns.get(0));
            }
        });
    }
}
