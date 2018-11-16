package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.asmutils.AsmInsnReader;
import me.aki.tactical.conversion.asmutils.AsmStackInsnWriter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.ExceptionTypeAnnotation;
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import me.aki.tactical.core.typeannotation.TypePath;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.TryCatchBlock;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
            StackLocal local = new StackLocal();
            ctx.getLocals().add(local);
            this.body.getLocals().add(local);
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

        convertTryCatchBlocks();
        convertLocalVariables();
        convertLocalVariableAnnotations();

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
        AsmStackInsnWriter iw = new AsmStackInsnWriter(ctx);
        AsmInsnReader reader = new AsmInsnReader(iw, ctx);

        InsnList instructions = mn.instructions;
        for (int index = 0; index < instructions.size(); index++) {
            AbstractInsnNode insn = instructions.get(index);
            Frame<BasicValue> frame = this.frames[index];
            if (frame == null) {
                // this instruction is dead code
                continue;
            }

            if (insn instanceof LineNumberNode) {
                convertLineNumber((LineNumberNode) insn);
                continue;
            }

            if (insn instanceof LabelNode || insn instanceof FrameNode) {
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

    private void convertLineNumber(LineNumberNode insn) {
        StackBody.LineNumber line = new StackBody.LineNumber(insn.line, null);
        this.body.getLineNumbers().add(line);
        this.ctx.registerForwardInsnCell(insn.start, line.getInstructionCell());
    }

    private void convertTryCatchBlocks() {
        for (TryCatchBlockNode block : mn.tryCatchBlocks) {
            if (isRangeEmpty(block.start, block.end)) {
                // This handler handles only dead code
                continue;
            }

            Optional<Path> exceptionType = Optional.ofNullable(block.type).map(AsmUtil::pathFromInternalName);
            TryCatchBlock tryCatchBlock = new TryCatchBlock(null, null, null, exceptionType);
            this.body.getTryCatchBlocks().add(tryCatchBlock);

            this.ctx.registerForwardInsnCell(block.start, tryCatchBlock.getFirstCell());
            this.ctx.registerBackwardInsnCell(block.end, tryCatchBlock.getLastCell());
            this.ctx.registerForwardInsnCell(block.handler, tryCatchBlock.getHandlerCell());

            if (block.visibleTypeAnnotations != null) {
                for (TypeAnnotationNode anno : block.visibleTypeAnnotations) {
                    tryCatchBlock.getTypeAnnotations().add(convertExceptionTypeAnnotation(anno, true));
                }
            }

            if (block.invisibleTypeAnnotations != null) {
                for (TypeAnnotationNode anno : block.invisibleTypeAnnotations) {
                    tryCatchBlock.getTypeAnnotations().add(convertExceptionTypeAnnotation(anno, false));
                }
            }
        }
    }

    private ExceptionTypeAnnotation convertExceptionTypeAnnotation(TypeAnnotationNode typeAnnotation, boolean visible) {
        TypePath typePath = AsmUtil.fromAsmTypePath(typeAnnotation.typePath);

        Annotation annotation = new Annotation(AsmUtil.pathFromInternalName(typeAnnotation.desc), visible);
        typeAnnotation.accept(new AnnotationConvertVisitor(null, annotation));

        return new ExceptionTypeAnnotation(typePath, annotation);
    }

    private void convertLocalVariables() {
        for (LocalVariableNode asmVar : mn.localVariables) {
            if (isRangeEmpty(asmVar.start, asmVar.end)) {
                continue;
            }

            StackBody.LocalVariable localVar = new StackBody.LocalVariable(asmVar.name,
                    AsmUtil.fromDescriptor(asmVar.desc), Optional.ofNullable(asmVar.signature),
                    null, null, ctx.getLocal(asmVar.index));
            this.body.getLocalVariables().add(localVar);

            this.ctx.registerForwardInsnCell(asmVar.start, localVar.getStartCell());
            this.ctx.registerBackwardInsnCell(asmVar.end, localVar.getEndCell());
        }
    }

    private void convertLocalVariableAnnotations() {
        if (mn.visibleLocalVariableAnnotations != null) {
            for (LocalVariableAnnotationNode varAnno : mn.visibleLocalVariableAnnotations) {
                convertLocalVariableAnnotation(varAnno, true);
            }
        }

        if (mn.invisibleLocalVariableAnnotations != null) {
            for (LocalVariableAnnotationNode varAnno : mn.invisibleLocalVariableAnnotations) {
                convertLocalVariableAnnotation(varAnno, false);
            }
        }
    }

    private void convertLocalVariableAnnotation(LocalVariableAnnotationNode varAnno, boolean visible) {
        List<StackBody.LocalVariableAnnotation.Location> locations = convertLocalVariableAnnotationLocations(varAnno);
        if (locations.isEmpty()) {
            // The annotated local variable exists only within dead code
            return;
        }

        TargetType.LocalTargetType targetType = convertLocalTargetType(new TypeReference(varAnno.typeRef));
        TypePath typePath = AsmUtil.fromAsmTypePath(varAnno.typePath);

        Annotation annotation = new Annotation(AsmUtil.pathFromInternalName(varAnno.desc), visible);
        varAnno.accept(new AnnotationConvertVisitor(null, annotation));

        LocalVariableTypeAnnotation typeAnno = new LocalVariableTypeAnnotation(typePath, annotation, targetType);
        body.getLocalVariableAnnotations().add(new StackBody.LocalVariableAnnotation(typeAnno, locations));
    }

    private TargetType.LocalTargetType convertLocalTargetType(TypeReference tref) {
        switch (tref.getSort()) {
            case TypeReference.LOCAL_VARIABLE:
                return new TargetType.LocalVariable();

            case TypeReference.RESOURCE_VARIABLE:
                return new TargetType.ResourceVariable();

            default:
                throw new AssertionError();
        }
    }

    private List<StackBody.LocalVariableAnnotation.Location> convertLocalVariableAnnotationLocations(LocalVariableAnnotationNode varAnno) {
        List<StackBody.LocalVariableAnnotation.Location> locations = new ArrayList<>();

        Iterator<LabelNode> startIter = varAnno.start.iterator();
        Iterator<LabelNode> endIter = varAnno.end.iterator();
        Iterator<Integer> indexIter = varAnno.index.iterator();
        while (startIter.hasNext()) {
            LabelNode start = startIter.next();
            LabelNode end = endIter.next();
            Integer index = indexIter.next();

            if (!isRangeEmpty(start, end)) {
                StackBody.LocalVariableAnnotation.Location location = new StackBody.LocalVariableAnnotation.Location(null, null, ctx.getLocal(index));
                locations.add(location);

                this.ctx.registerForwardInsnCell(start, location.getStartCell());
                this.ctx.registerBackwardInsnCell(end, location.getEndCell());
            }
        }

        return locations;
    }

    /**
     * Check whether any reachable (= no dead code) instructions are within a range
     * of instructions defined by two labels.
     *
     * @param start of the range
     * @param end of the range
     * @return whether the range contains no reachable statements
     */
    private boolean isRangeEmpty(LabelNode start, LabelNode end) {
        AbstractInsnNode node = start;
        do {
            int insnIndex = mn.instructions.indexOf(node);
            if (frames[insnIndex] != null) {
                return false;
            }
        } while((node = node.getNext()) != end);
        return true;
    }

    private void updateInsnCells() {
        ctx.getForwardLabelCells().forEach((label, insnCells) -> {
            List<Instruction> insns = findConvertedInsn(label, true);
            Instruction firstInsn = insns.get(0);

            insnCells.forEach(cell -> cell.set(firstInsn));
        });

        ctx.getBackwardLabelCells().forEach((label, insnCells) -> {
            List<Instruction> insns = findConvertedInsn(label, false);
            Instruction lastInsn = insns.get(insns.size() - 1);

            insnCells.forEach(cell -> cell.set(lastInsn));
        });
    }

    private List<Instruction> findConvertedInsn(LabelNode label, boolean walkForward) {
        AbstractInsnNode node = label;
        while ((node = (walkForward ? node.getNext() : node.getPrevious())) != null) {
            List<Instruction> insns = convertedInsns.get(node);
            if (insns != null && !insns.isEmpty()) {
                return insns;
            }
        }

        throw new RuntimeException("Label could not be resolved");
    }
}
