package me.aki.tactical.stack.textify;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.textify.AnnotationTextifier;
import me.aki.tactical.core.textify.BodyTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TargetTypeTextifier;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeAnnotationTextifier;
import me.aki.tactical.core.textify.TypePathTextifier;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.TryCatchBlock;
import me.aki.tactical.stack.insn.BranchInsn;
import me.aki.tactical.stack.insn.Instruction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StackBodyTextifier implements BodyTextifier {
    @Override
    public void textifyParameters(Printer printer, Method method) {
        int paramIndex = 0;
        Iterator<Type> paramIter = method.getParameterTypes().iterator();
        while (paramIter.hasNext()) {
            Type type = paramIter.next();

            TypeTextifier.getInstance().textify(printer, type);
            printer.addText(" ");
            printer.addLiteral(getParamLocalName(paramIndex++));

            if (paramIter.hasNext()) {
                printer.addText(", ");
            }
        }
    }

    private String getParamLocalName(int param) {
        return "param" + param;
    }

    @Override
    public void textify(Printer printer, Method method) {
        Optional<Body> bodyOpt = method.getBody();
        if (bodyOpt.isPresent()) {
            Body body = bodyOpt.get();
            if (body instanceof StackBody) {
                textifyBody(printer, (StackBody) body);
            } else {
                throw new IllegalArgumentException("Cannot parse body of type " + body.getClass().getSimpleName());
            }
        } else {
            throw new IllegalArgumentException("Method lacks a body");
        }
    }

    private void textifyBody(Printer printer, StackBody body) {
        TextifyContext ctx = new TextifyContext(body);
        prepareLabels(body, ctx);
        prepareLocals(body, ctx);

        textifyInstructions(printer, body, ctx);

        textifyTryCatchBlocks(printer, body.getTryCatchBlocks(), ctx);
        textifyLines(printer, body.getLineNumbers(), ctx);
        textifyLocalVariables(printer, body.getLocalVariables(), ctx);
        textifyLocalVariableAnnotations(printer, body.getLocalVariableAnnotations(), ctx);
        textifyInsnAnnotation(printer, body.getInstructions(), ctx);
    }

    private void prepareLabels(StackBody body, TextifyContext ctx) {
        Set<Instruction> referencedInstructions = getAllReferencedInstructions(body);
        int labelCount = referencedInstructions.size();

        int index = 0;
        for (Instruction instruction : body.getInstructions()) {
            if (referencedInstructions.contains(instruction)) {
                ctx.setLabel(instruction, "label" + TextUtil.paddedNumber(index++, labelCount));
            }
        }
    }

    /**
     * Assign names to all locals.
     *
     * @param body body that contains the locals
     * @param ctx register the locals in that context
     */
    private void prepareLocals(StackBody body, TextifyContext ctx) {
        body.getThisLocal().ifPresent(thisLocal ->
                ctx.setLocalName(thisLocal, "this"));

        int paramIndex = 0;
        for (StackLocal param : body.getParameterLocals()) {
            ctx.setLocalName(param, getParamLocalName(paramIndex++));
        }

        List<StackLocal> remainingLocals = body.getLocals().stream()
                .filter(local -> !ctx.isLocalNamed(local))
                .collect(Collectors.toList());

        int localIndex = 0;
        for (StackLocal local : remainingLocals) {
            ctx.setLocalName(local, "local" + TextUtil.paddedNumber(localIndex++, remainingLocals.size()));
        }
    }

    /**
     * Get all instructions that are referenced somewhere.
     *
     * @param body whose referenced instructions are requested.
     * @return all referenced instructions
     */
    private Set<Instruction> getAllReferencedInstructions(StackBody body) {
        Set<Instruction> insns = new HashSet<>();

        for (Instruction instruction : body.getInstructions()) {
            if (!instruction.getTypeAnnotations().isEmpty()) {
                insns.add(instruction);
            }

            if (instruction instanceof BranchInsn) {
                insns.addAll(((BranchInsn) instruction).getBranchTargets());
            }
        }

        for (TryCatchBlock catchBlock : body.getTryCatchBlocks()) {
            insns.add(catchBlock.getFirst());
            insns.add(catchBlock.getLast());
            insns.add(catchBlock.getHandler());
        }

        for (StackBody.LineNumber lineNumber : body.getLineNumbers()) {
            insns.add(lineNumber.getInstruction());
        }

        for (StackBody.LocalVariable localVariable : body.getLocalVariables()) {
            insns.add(localVariable.getStart());
            insns.add(localVariable.getEnd());
        }

        for (StackBody.LocalVariableAnnotation localVariableAnnotation : body.getLocalVariableAnnotations()) {
            for (StackBody.LocalVariableAnnotation.Location location : localVariableAnnotation.getLocations()) {
                insns.add(location.getStart());
                insns.add(location.getEnd());
            }
        }

        return insns;
    }

    private void textifyInstructions(Printer printer, StackBody body, TextifyContext ctx) {
        InsnTextifier insnTextifier = new InsnTextifier(ctx);

        for (Instruction instruction : body.getInstructions()) {
            ctx.getLabelOpt(instruction).ifPresent(label -> {
                printer.decreaseIndent();
                printer.addLiteral(label);
                printer.addText(":");
                printer.newLine();
                printer.increaseIndent();
            });

            insnTextifier.textify(printer, instruction);
            printer.newLine();
        }
    }

    private void textifyTryCatchBlocks(Printer printer, List<TryCatchBlock> tryCatchBlocks, TextifyContext ctx) {
        for (TryCatchBlock tryCatchBlock : tryCatchBlocks) {
            printer.addText("try ");
            printer.addLiteral(ctx.getLabel(tryCatchBlock.getFirst()));
            printer.addText(" -> ");
            printer.addLiteral(ctx.getLabel(tryCatchBlock.getLast()));
            printer.addText(" catch ");
            printer.addLiteral(ctx.getLabel(tryCatchBlock.getHandler()));
            tryCatchBlock.getExceptionType().ifPresent(exception -> {
                printer.addText(" : ");
                printer.addPath(exception);
            });
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyLines(Printer printer, List<StackBody.LineNumber> lineNumbers, TextifyContext ctx) {
        for (StackBody.LineNumber lineNumber : lineNumbers) {
            printer.addText("line " + lineNumber.getLine() + " ");
            printer.addLiteral(ctx.getLabel(lineNumber.getInstruction()));
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyLocalVariables(Printer printer, List<StackBody.LocalVariable> localVariables, TextifyContext ctx) {
        for (StackBody.LocalVariable localVariable : localVariables) {
            printer.addText("local info ");
            printer.addLiteral(ctx.getLabel(localVariable.getStart()));
            printer.addText(" -> ");
            printer.addLiteral(ctx.getLabel(localVariable.getEnd()));
            printer.addText(", ");
            printer.addText(ctx.getLocalName(localVariable.getLocal()));
            printer.addText(", ");
            printer.addEscaped(localVariable.getName(), '"');
            printer.addText(", ");
            TypeTextifier.getInstance().textify(printer, localVariable.getType());
            localVariable.getSignature().ifPresent(signature -> {
                printer.addText(", ");
                printer.addEscaped(signature, '"');
            });
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyLocalVariableAnnotations(Printer printer, List<StackBody.LocalVariableAnnotation> annotations, TextifyContext ctx) {
        for (StackBody.LocalVariableAnnotation localAnnotation : annotations) {
            printer.addText("local annotation [");
            TextUtil.joined(localAnnotation.getLocations(),
                    location -> {
                        printer.addLiteral(ctx.getLabel(location.getStart()));
                        printer.addText(" -> ");
                        printer.addLiteral(ctx.getLabel(location.getEnd()));
                        printer.addText(" ");
                        printer.addLiteral(ctx.getLocalName(location.getLocal()));
                    },
                    () -> printer.addText(", "));
            printer.addText("], ");

            TypeAnnotationTextifier.LOCAL.textify(printer, localAnnotation.getAnnotation());
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyInsnAnnotation(Printer printer, InsertList<Instruction> instructions, TextifyContext ctx) {
        for (Instruction instruction : instructions) {
            for (InsnTypeAnnotation typeAnnotation : instruction.getTypeAnnotations()) {
                printer.addText("insn annotation ");
                printer.addLiteral(ctx.getLabel(instruction));
                printer.addText(" ");
                TypeAnnotationTextifier.INSN.textify(printer, typeAnnotation);
                printer.addText(";");
            }
        }
    }
}
