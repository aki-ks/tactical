package me.aki.tactical.stack.textify;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.textify.BodyTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TypeTextifier;
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

public class StackBodyTextifier implements BodyTextifier {
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

        textifyLocals(printer, body, ctx);

        textifyInstructions(printer, body, ctx);

        textifyTryCatchBlocks(printer, body.getTryCatchBlocks(), ctx);
        textifyLines(printer, body.getLineNumbers(), ctx);
        textifyLocalVariables(printer, body.getLocalVariables(), ctx);
    }

    private void prepareLabels(StackBody body, TextifyContext ctx) {
        Set<Instruction> referencedInstructions = getAllReferencedInstructions(body);
        int labelCount = referencedInstructions.size();

        int index = 0;
        for (Instruction instruction : body.getInstructions()) {
            if (referencedInstructions.contains(instruction)) {
                ctx.setLabel(instruction, "label" + paddedNumber(index++, labelCount));
            }
        }
    }

    /**
     * Get a number as string and prepend zeros until it is as long as another string.
     *
     * @param number to be turned into a string
     * @param max reference for amount of prepended zeros.
     * @return number prepended with zeros
     */
    private String paddedNumber(int number, int max) {
        StringBuilder builder = new StringBuilder();

        int zeroCount = ((int) Math.log10(max)) - ((int) Math.log10(number));
        for (int i = 0; i < zeroCount; i++) {
            builder.append('0');
        }

        builder.append(Integer.toString(number));
        return builder.toString();
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

    private void textifyLocals(Printer printer, StackBody body, TextifyContext ctx) {
        body.getThisLocal().ifPresent(thisLocal -> {
            String localName = "this";
            ctx.setLocalName(thisLocal, localName);

            printer.addText("Local " + localName + " = this;");
            printer.newLine();
        });

        int parameterIndex = 0;
        for (StackLocal local : body.getParameterLocals()) {
            String localName = "param" + parameterIndex;
            ctx.setLocalName(local, localName);

            printer.addText("Local " + localName + " = parameter " + parameterIndex++ + ";");
            printer.newLine();
            parameterIndex++;
        }

        Iterator<StackLocal> remainingLocalIter = body.getLocals().stream()
                .filter(local -> !ctx.isLocalNamed(local))
                .iterator();

        int localIndex = 0;
        if (remainingLocalIter.hasNext()) {
            printer.addText("Local ");

            while (remainingLocalIter.hasNext()) {
                String localName = "local" + localIndex++;
                ctx.setLocalName(remainingLocalIter.next(), localName);
                printer.addText(localName);

                if (remainingLocalIter.hasNext()) {
                    printer.addText(", ");
                }
            }

            printer.addText(";");
            printer.newLine();
        }

        if (!ctx.getLocalNames().isEmpty()) {
            // if any locals were printer, insert a blank line
            printer.newLine();
        }
    }

    private void textifyInstructions(Printer printer, StackBody body, TextifyContext ctx) {
        InsnTextifier insnTextifier = new InsnTextifier(ctx);

        for (Instruction instruction : body.getInstructions()) {
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
                printer.addText(" ");
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
            printer.addText("localvariable ");
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
        }
    }
}
