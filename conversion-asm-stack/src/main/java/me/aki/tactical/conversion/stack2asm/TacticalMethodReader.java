package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stack2asm.analysis.Analysis;
import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TacticalMethodReader {
    private final Method method;

    public TacticalMethodReader(Method method) {
        this.method = method;
    }

    public void accept(MethodVisitor mv) {
        visitParameters(mv);
        visitAnnotationDefault(mv);
        visitAnnotations(mv);
        visitTypeAnnotations(mv);
        visitParameterAnnotations(mv);
        //TODO: visitAttribute
        visitBody(mv);
        mv.visitEnd();
    }

    private void visitParameters(MethodVisitor mv) {
        for (Method.Parameter parameter : method.getParameterInfo()) {
            String name = parameter.getName().orElse(null);
            int access = AccessConverter.parameter.toBitMap(parameter.getFlags());

            mv.visitParameter(name, access);
        }
    }

    private void visitAnnotationDefault(MethodVisitor mv) {
        method.getDefaultValue().ifPresent(annoValue -> {
            AnnotationVisitor av = mv.visitAnnotationDefault();
            if (av != null) {
                LinkedHashMap<String, AnnotationValue> map = new LinkedHashMap<>();
                map.put(null, annoValue);
                new TacticalAnnotationReader(map);
            }
        });
    }

    private void visitAnnotations(MethodVisitor mv) {
        for (Annotation annotation : method.getAnnotations()) {
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = mv.visitAnnotation(descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation.getValues()).accept(av);
            }
        }
    }

    private void visitTypeAnnotations(MethodVisitor cv) {
        for (MethodTypeAnnotation typeAnnotation : method.getTypeAnnotations()) {
            Annotation annotation = typeAnnotation.getAnnotation();

            int typeRef = convertTargetType(typeAnnotation.getTargetType()).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = cv.visitTypeAnnotation(typeRef, typePath, descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation.getValues()).accept(av);
            }
        }
    }

    private TypeReference convertTargetType(TargetType.MethodTargetType targetType) {
        if (targetType instanceof TargetType.CheckedException) {
            int exceptionIndex = ((TargetType.CheckedException) targetType).getException();
            return TypeReference.newExceptionReference(exceptionIndex);
        } else if (targetType instanceof TargetType.MethodParameter) {
            int parameterIndex = ((TargetType.MethodParameter) targetType).getParameter();
            return TypeReference.newFormalParameterReference(parameterIndex);
        } else if (targetType instanceof TargetType.MethodReceiver) {
            return TypeReference.newTypeReference(TypeReference.METHOD_RECEIVER);
        } else if (targetType instanceof TargetType.ReturnType) {
            return TypeReference.newTypeReference(TypeReference.METHOD_RETURN);
        } else if (targetType instanceof TargetType.TypeParameter) {
            int parameterIndex = ((TargetType.TypeParameter) targetType).getParameterIndex();
            return TypeReference.newTypeParameterReference(TypeReference.METHOD_TYPE_PARAMETER, parameterIndex);
        } else if (targetType instanceof TargetType.TypeParameterBound) {
            int parameterIndex = ((TargetType.TypeParameterBound) targetType).getParameterIndex();
            int boundIndex = ((TargetType.TypeParameterBound) targetType).getBoundIndex();
            return TypeReference.newTypeParameterBoundReference(TypeReference.METHOD_TYPE_PARAMETER_BOUND, parameterIndex, boundIndex);
        } else {
            throw new AssertionError();
        }
    }

    private void visitParameterAnnotations(MethodVisitor mv) {
        int parameterIndex = 0;
        for (List<Annotation> paramAnnos : method.getParameterAnnotations()) {
            for (Annotation annotation : paramAnnos) {
                String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
                boolean isVisible = annotation.isRuntimeVisible();

                AnnotationVisitor av = mv.visitParameterAnnotation(parameterIndex, descriptor, isVisible);
                if (av != null) {
                    new TacticalAnnotationReader(annotation.getValues()).accept(av);
                }
            }

            parameterIndex++;
        }
    }

    private void visitBody(MethodVisitor mv) {
        method.getBody().map(body -> (StackBody) body).ifPresent(body -> {
            MethodNode mn = new MethodNode();

            Analysis analysis = new Analysis(body);
            analysis.analyze();

            LabelResolver labelResolver = convertInstructions(body, analysis, mn.instructions);

            insertLineNumberNodes(body.getLineNumbers(), mn.instructions, labelResolver);

            convertLocalVariables(body, analysis, labelResolver, mn);

            mn.accept(mv);
        });
    }

    private LabelResolver convertInstructions(StackBody body, Analysis analysis, InsnList insnList) {
        Map<Instruction, List<AbstractInsnNode>> convertedInsns = new HashMap<>();
        ConversionContext ctx = new ConversionContext(body);

        AsmInsnWriter insnWriter = new AsmInsnWriter(ctx);
        TacticalInsnReader insnReader = new TacticalInsnReader(insnWriter);

        for (Instruction instruction : body.getInstructions()) {
            analysis.getStackState(instruction).ifPresent(stackFrame -> {
                insnWriter.setStackFrame(stackFrame);
                insnReader.accept(instruction);

                List<AbstractInsnNode> asmInsns = insnWriter.getConvertedInsns();
                asmInsns.forEach(insnList::add);
                convertedInsns.put(instruction, new ArrayList<>(asmInsns));
                asmInsns.clear();
            });
        }

        LabelResolver labelResolver = new LabelResolver(insnList, convertedInsns);

        // Resolve all labels used within instructions
        ctx.getConvertedLabels().forEach((insn, labelCells) -> {
            LabelNode labelNode = labelResolver.getForwardLabel(insn);
            labelCells.forEach(cell -> cell.set(labelNode));
        });

        return labelResolver;
    }

    private void insertLineNumberNodes(List<StackBody.LineNumber> lineNumbers, InsnList insnList, LabelResolver labelResolver) {
        for (StackBody.LineNumber lineNumber : lineNumbers) {
            LabelNode labelNode = labelResolver.getForwardLabel(lineNumber.getInstruction());
            LineNumberNode lineNumberNode = new LineNumberNode(lineNumber.getLine(), labelNode);
            insnList.insert(labelNode, lineNumberNode);
        }
    }

    private void convertLocalVariables(StackBody body, Analysis analysis, LabelResolver labelResolver, MethodNode mn) {
        for (StackBody.LocalVariable local : body.getLocalVariables()) {
            if (isRangeEmpty(body, analysis, local.getStart(), local.getEnd())) {
                continue;
            }

            String name = local.getName();
            String descriptor = AsmUtil.toDescriptor(local.getType());
            String signature = local.getSignature().orElse(null);
            LabelNode start = labelResolver.getForwardLabel(local.getStart());
            LabelNode end = labelResolver.getBackwardLabel(local.getEnd());
            int index = getLocalIndex(body, local.getLocal());

            mn.localVariables.add(new LocalVariableNode(name, descriptor, signature, start, end, index));
        }
    }

    /**
     * Get the index of a local.
     *
     * @param body that contains the local
     * @param local local whose index we want
     * @return index of the local
     */
    private int getLocalIndex(StackBody body, Local local) {
        int index = body.getLocals().indexOf(local);
        if (index < 0) {
            throw new IllegalStateException("Local is not contained within the body");
        }
        return index;
    }

    /**
     * Check whether a range of instructions is only dead code.
     *
     * @param stackBody body containing the instructions
     * @param analysis cfg analysis of all instructions within the method
     * @param start first instruction of the instruction range
     * @param end last instruction of the instruction range
     * @return contains the instruction range only dead code
     */
    private boolean isRangeEmpty(StackBody stackBody, Analysis analysis, Instruction start, Instruction end) {
        int startIndex = stackBody.getInstructions().indexOf(start);
        Iterator<Instruction> iter = stackBody.getInstructions().listIterator(startIndex);

        while (iter.hasNext()) {
            Instruction instruction = iter.next();

            if (analysis.getStackState(instruction).isPresent()) {
                return false;
            }

            if (instruction == end) {
                return false;
            }
        }

        // The "start" instruction is not succeeded by the "end" instruction
        throw new IllegalStateException("Illegal instruction range");
    }

    private static class LabelResolver {
        private final InsnList insnList;
        private final Map<Instruction, List<AbstractInsnNode>> convertedInsns;

        private final Map<Instruction, LabelNode> forwardLabels = new HashMap<>();
        private final Map<Instruction, LabelNode> backwardLabels = new HashMap<>();

        LabelResolver(InsnList insnList, Map<Instruction, List<AbstractInsnNode>> convertedInsns) {
            this.insnList = insnList;
            this.convertedInsns = convertedInsns;
        }

        /**
         * Get a {@link LabelNode} that targets the {@link AbstractInsnNode} corresponding
         * to a certain {@link Instruction}. If there is no such {@link LabelNode} it will be
         * created and inserted into the instruction list of this method.
         *
         * @param insn whose corresponding label we want
         * @return a {@link LabelNode} corresponding to the request instruction
         */
        public LabelNode getForwardLabel(Instruction insn) {
            return forwardLabels.computeIfAbsent(insn, x -> {
                LabelNode labelNode = new LabelNode(new Label());
                List<AbstractInsnNode> asmInsns = getAsmInsns(insn);
                insnList.insertBefore(asmInsns.get(0), labelNode);
                return labelNode;
            });
        }

        /**
         * Similar to {@link LabelResolver#getForwardLabel(Instruction)} but the returned
         * {@link LabelNode} succeeds the request instruction instead of preceding it.
         *
         * @param insn whose label do we want
         * @return {@link LabelNode} inserted after the corresponding instruction.
         */
        public LabelNode getBackwardLabel(Instruction insn) {
            return backwardLabels.computeIfAbsent(insn, x -> {
                LabelNode labelNode = new LabelNode(new Label());
                List<AbstractInsnNode> asmInsns = getAsmInsns(insn);
                AbstractInsnNode lastAsmInsn = asmInsns.get(asmInsns.size() - 1);
                insnList.insert(lastAsmInsn, labelNode);
                return labelNode;
            });
        }

        private List<AbstractInsnNode> getAsmInsns(Instruction insn) {
            List<AbstractInsnNode> asmInsns = convertedInsns.get(insn);
            if (asmInsns == null || asmInsns.isEmpty()) {
                throw new IllegalStateException();
            }
            return asmInsns;
        }
    }
}
