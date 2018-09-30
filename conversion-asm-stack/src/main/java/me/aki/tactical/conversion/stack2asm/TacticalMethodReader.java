package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.analysis.Analysis;
import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.conversion.stackasm.StackInsnReader;
import me.aki.tactical.core.Attribute;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.typeannotation.ExceptionTypeAnnotation;
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.TryCatchBlock;
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
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        visitAttributes(mv);
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
                new TacticalAnnotationReader(annotation).accept(av);
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
                new TacticalAnnotationReader(annotation).accept(av);
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
                    new TacticalAnnotationReader(annotation).accept(av);
                }
            }

            parameterIndex++;
        }
    }

    private void visitAttributes(MethodVisitor mv) {
        for (Attribute attribute : method.getAttributes()) {
            mv.visitAttribute(new CustomAttribute(attribute.getName(), attribute.getData()));
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
            convertLocalVariableAnnotations(body, analysis, labelResolver, mn);

            convertTryCatchBlocks(body, analysis, labelResolver, mn);

            mn.accept(mv);
        });
    }

    private LabelResolver convertInstructions(StackBody body, Analysis analysis, InsnList insnList) {
        Map<Instruction, List<AbstractInsnNode>> convertedInsns = new HashMap<>();
        ConversionContext ctx = new ConversionContext(body);

        AsmInsnWriter insnWriter = new AsmInsnWriter(ctx);
        StackInsnReader insnReader = new StackInsnReader(insnWriter);

        for (Instruction instruction : body.getInstructions()) {
            analysis.getStackState(instruction).ifPresent(stackFrame -> {
                insnWriter.setStackFrame(stackFrame);
                insnReader.accept(instruction);

                List<AbstractInsnNode> asmInsns = insnWriter.getConvertedInsns();
                convertInsnAnnotation(instruction.getTypeAnnotations(), asmInsns.get(0));
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

    private void convertInsnAnnotation(List<InsnTypeAnnotation> typeAnnotations, AbstractInsnNode insn) {
        for (InsnTypeAnnotation typeAnnotation : typeAnnotations) {
            Annotation annotation = typeAnnotation.getAnnotation();
            int typeRef = convertInsnTargetType(typeAnnotation.getTargetType()).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());

            TypeAnnotationNode annotationNode = new TypeAnnotationNode(typeRef, typePath, descriptor);
            new TacticalAnnotationReader(annotation).accept(annotationNode);
            getInsnTypeAnnotationList(insn, annotation.isRuntimeVisible()).add(annotationNode);
        }
    }

    private TypeReference convertInsnTargetType(TargetType.InsnTargetType targetType) {
        if (targetType instanceof TargetType.InstanceOf) {
            return TypeReference.newTypeReference(TypeReference.INSTANCEOF);
        } else if (targetType instanceof TargetType.New) {
            return TypeReference.newTypeReference(TypeReference.NEW);
        } else if (targetType instanceof TargetType.ConstructorReference) {
            return TypeReference.newTypeReference(TypeReference.CONSTRUCTOR_REFERENCE);
        } else if (targetType instanceof TargetType.MethodReference) {
            return TypeReference.newTypeReference(TypeReference.METHOD_REFERENCE);
        } else if (targetType instanceof TargetType.Cast) {
            int intersection = ((TargetType.Cast) targetType).getIntersection();
            return TypeReference.newTypeArgumentReference(TypeReference.CAST, intersection);
        } else if (targetType instanceof TargetType.AbstractTypeParameterInsnTargetType) {
            int sort;
            if (targetType instanceof TargetType.ConstructorInvokeTypeParameter) {
                sort = TypeReference.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT;
            } else if (targetType instanceof TargetType.MethodInvokeTypeParameter) {
                sort = TypeReference.METHOD_INVOCATION_TYPE_ARGUMENT;
            } else if (targetType instanceof TargetType.ConstructorReferenceTypeParameter) {
                sort = TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT;
            } else if (targetType instanceof TargetType.MethodReferenceTypeParameter) {
                sort = TypeReference.METHOD_REFERENCE_TYPE_ARGUMENT;
            } else {
                throw new AssertionError();
            }

            int typeParameterIndex = ((TargetType.AbstractTypeParameterInsnTargetType) targetType).getTypeParameter();
            return TypeReference.newTypeArgumentReference(sort, typeParameterIndex);
        } else {
            throw new AssertionError();
        }
    }

    /**
     * Get (and initialize if necessary) the list for visible or invisible instruction type annotations.
     *
     * @param insn instruction containing the annotation list
     * @param isVisible request the list of visible or invisible annotations
     * @return the requested list of type annotations
     */
    private List<TypeAnnotationNode> getInsnTypeAnnotationList(AbstractInsnNode insn, boolean isVisible) {
        if (isVisible) {
            if (insn.visibleTypeAnnotations == null) {
                insn.visibleTypeAnnotations = new ArrayList<>();
            }
            return insn.visibleTypeAnnotations;
        } else {
            if (insn.invisibleTypeAnnotations == null) {
                insn.invisibleTypeAnnotations = new ArrayList<>();
            }
            return insn.invisibleTypeAnnotations;
        }
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

            if (mn.localVariables == null) {
                mn.localVariables = new ArrayList<>();
            }
            mn.localVariables.add(new LocalVariableNode(name, descriptor, signature, start, end, index));
        }
    }

    private void convertLocalVariableAnnotations(StackBody body, Analysis analysis, LabelResolver labelResolver, MethodNode mn) {
        for (StackBody.LocalVariableAnnotation localAnnotation : body.getLocalVariableAnnotations()) {
            List<StackBody.LocalVariableAnnotation.Location> locations = localAnnotation.getLocations().stream()
                    .filter(location -> !isRangeEmpty(body, analysis, location.getStart(), location.getEnd()))
                    .collect(Collectors.toList());

            if (locations.isEmpty()) {
                continue;
            }

            LocalVariableTypeAnnotation typeAnnotation = localAnnotation.getAnnotation();
            Annotation annotation = typeAnnotation.getAnnotation();
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());

            int typeRef = convertLocalTargetType(typeAnnotation.getTargetType()).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());

            LabelNode[] start = locations.stream()
                    .map(location -> labelResolver.getForwardLabel(location.getStart()))
                    .toArray(LabelNode[]::new);

            LabelNode[] end = locations.stream()
                    .map(location -> labelResolver.getBackwardLabel(location.getStart()))
                    .toArray(LabelNode[]::new);

            int[] index = locations.stream()
                    .mapToInt(location -> getLocalIndex(body, location.getLocal()))
                    .toArray();

            LocalVariableAnnotationNode annotationNode = new LocalVariableAnnotationNode(typeRef, typePath, start, end, index, descriptor);
            new TacticalAnnotationReader(annotation).accept(annotationNode);
            getLocalVariableAnnotationList(mn, annotation.isRuntimeVisible()).add(annotationNode);
        }
    }

    /**
     * Get (and initialize if necessary) the list for visible or invisible local type annotations.
     *
     * @param mn method node containing the annotation list
     * @param isVisible do we want the list for visible or invisible annotations
     * @return the list of visible or invisible local variable type annotations
     */
    private List<LocalVariableAnnotationNode> getLocalVariableAnnotationList(MethodNode mn, boolean isVisible) {
        List<LocalVariableAnnotationNode> annotationList;
        if (isVisible) {
            if (mn.visibleLocalVariableAnnotations == null) {
                mn.visibleLocalVariableAnnotations = new ArrayList<>();
            }
            annotationList = mn.visibleLocalVariableAnnotations;
        } else {
            if (mn.invisibleLocalVariableAnnotations == null) {
                mn.invisibleLocalVariableAnnotations = new ArrayList<>();
            }
            annotationList = mn.invisibleLocalVariableAnnotations;
        }
        return annotationList;
    }

    private TypeReference convertLocalTargetType(TargetType.LocalTargetType targetType) {
        if (targetType instanceof TargetType.LocalVariable) {
            return TypeReference.newTypeReference(TypeReference.LOCAL_VARIABLE);
        } else if (targetType instanceof TargetType.ResourceVariable) {
            return TypeReference.newTypeReference(TypeReference.RESOURCE_VARIABLE);
        } else {
            throw new AssertionError();
        }
    }

    private void convertTryCatchBlocks(StackBody body, Analysis analysis, LabelResolver labelResolver, MethodNode mn) {
        for (TryCatchBlock block : body.getTryCatchBlocks()) {
            if (isRangeEmpty(body, analysis, block.getFirst(), block.getLast())) {
                continue;
            }

            LabelNode start = labelResolver.getForwardLabel(block.getFirst());
            LabelNode end = labelResolver.getForwardLabel(block.getLast());
            LabelNode handler = labelResolver.getForwardLabel(block.getHandler());
            String type = block.getExceptionType().map(AsmUtil::toInternalName).orElse(null);

            TryCatchBlockNode node = new TryCatchBlockNode(start, end, handler, type);
            convertTryCatchBlockAnnotations(block, node);

            if (mn.tryCatchBlocks == null) {
                mn.tryCatchBlocks = new ArrayList<>();
            }
            mn.tryCatchBlocks.add(node);
        }
    }

    private void convertTryCatchBlockAnnotations(TryCatchBlock block, TryCatchBlockNode node) {
        for (ExceptionTypeAnnotation typeAnnotation : block.getTypeAnnotations()) {
            Annotation annotation = typeAnnotation.getAnnotation();
            int typeRef = TypeReference.newTypeReference(TypeReference.EXCEPTION_PARAMETER).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());

            TypeAnnotationNode annotationNode = new TypeAnnotationNode(typeRef, typePath, descriptor);
            new TacticalAnnotationReader(annotation).accept(annotationNode);
            getTryCatchBlockAnnotationList(node, annotation.isRuntimeVisible()).add(annotationNode);
        }
    }

    /**
     * Get (and initialize if necessary) the list of visible or invisible try/catch type annotations.
     *
     * @param node try/catch block containing the annotation list
     * @param isVisible request the list of visible or invisible annotations
     * @return the requested list of type annotations
     */
    private List<TypeAnnotationNode> getTryCatchBlockAnnotationList(TryCatchBlockNode node, boolean isVisible) {
        List<LocalVariableAnnotationNode> annotationList;
        if (isVisible) {
            if (node.visibleTypeAnnotations == null) {
                node.visibleTypeAnnotations = new ArrayList<>();
            }
            return node.visibleTypeAnnotations;
        } else {
            if (node.invisibleTypeAnnotations == null) {
                node.invisibleTypeAnnotations = new ArrayList<>();
            }
            return node.invisibleTypeAnnotations;
        }
    }

    /**
     * Get the index of a local.
     *
     * @param body that contains the local
     * @param local local whose index we want
     * @return index of the local
     */
    private int getLocalIndex(StackBody body, StackLocal local) {
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
                // This instruction is no dead code
                return false;
            }

            if (instruction == end) {
                return true;
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
