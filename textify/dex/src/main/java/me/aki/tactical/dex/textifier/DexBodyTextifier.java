package me.aki.tactical.dex.textifier;

import me.aki.tactical.core.Method;
import me.aki.tactical.core.textify.BodyTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.TryCatchBlock;
import me.aki.tactical.dex.insn.BranchInstruction;
import me.aki.tactical.dex.insn.Instruction;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DexBodyTextifier implements BodyTextifier {
    @Override
    public void textifyParameters(Printer printer, Method method) {
        int parameterIndex = 0;
        int parameterCount = method.getParameterTypes().size();

        Iterator<Type> paramTypeIterator = method.getParameterTypes().iterator();
        while (paramTypeIterator.hasNext()) {
            TypeTextifier.getInstance().textify(printer, paramTypeIterator.next());
            printer.addText(" ");
            printer.addLiteral(getParameterRegisterName(parameterIndex++, parameterCount));

            if (paramTypeIterator.hasNext()) {
                printer.addText(", ");
            }
        }
    }

    private String getParameterRegisterName(int parameter, int parameterCount) {
        return "param" + TextUtil.paddedNumber(parameter, parameterCount);
    }

    private Map<Register, String> buildRegisterMap(DexBody dexBody) {
        Map<Register, String> nameMap = new HashMap<>();

        nameThisRegister(dexBody, nameMap);
        nameParameterRegisters(dexBody, nameMap);
        nameRemainingRegisters(dexBody, nameMap);

        return nameMap;
    }

    private void nameThisRegister(DexBody dexBody, Map<Register, String> nameMap) {
        dexBody.getThisRegister().ifPresent(thisRegister ->
                nameMap.put(thisRegister, "this"));
    }

    private void nameParameterRegisters(DexBody dexBody, Map<Register, String> nameMap) {
        int parameterIndex = 0;
        int parameterCount = dexBody.getParameterRegisters().size();
        for (Register paramRegister : dexBody.getParameterRegisters()) {
            String registerName = getParameterRegisterName(parameterIndex++, parameterCount);
            nameMap.put(paramRegister, registerName);
        }
    }

    private void nameRemainingRegisters(DexBody dexBody, Map<Register, String> nameMap) {
        Set<Register> unnamedRegisters = new HashSet<>(dexBody.getRegisters());
        unnamedRegisters.removeAll(nameMap.keySet());

        int registerIndex = 0;
        for (Register unnamedRegister : unnamedRegisters) {
            String registerName = "register" + TextUtil.paddedNumber(registerIndex++, unnamedRegisters.size());
            nameMap.put(unnamedRegister, registerName);
        }
    }

    private Map<Instruction, String> buildLabelMap(DexBody body) {
        Set<Instruction> referencedInsns = getReferencedInstructions(body);

        Map<Instruction, String> labels = new HashMap<>();
        int labelIndex = 0;
        for (Instruction instruction : body.getInstructions()) {
            if (referencedInsns.contains(instruction)) {
                String labelName = "label" + TextUtil.paddedNumber(labelIndex++, referencedInsns.size());
                labels.put(instruction, labelName);
            }
        }
        return labels;
    }

    private Set<Instruction> getReferencedInstructions(DexBody body) {
        Stream<Instruction> insnsReferencedByBranchInsn = body.getInstructions().stream()
                .flatMap(insn -> insn instanceof BranchInstruction ? Stream.of((BranchInstruction) insn) : Stream.of())
                .flatMap(branchInsn -> branchInsn.getBranchTargets().stream());

        Stream<Instruction> insnsReferencedByTryCatchBlocks = body.getTryCatchBlocks().stream()
                .flatMap(block -> Stream.concat(
                        Stream.of(block.getStart(), block.getEnd()),
                        block.getHandlers().stream().map(TryCatchBlock.Handler::getHandler)
                ));

        return Stream.concat(insnsReferencedByBranchInsn, insnsReferencedByTryCatchBlocks).collect(Collectors.toSet());
    }

    @Override
    public void textify(Printer printer, Method value) {
        DexBody body = (DexBody) value.getBody().get();
        TextifyCtx ctx = new TextifyCtx(buildRegisterMap(body), buildLabelMap(body));

        textifyRegisters(printer, ctx, body);

        textifyInstructions(printer, ctx, body);

        textifyTryCatchBlocks(printer, ctx, body);
    }

    private void textifyRegisters(Printer printer, TextifyCtx ctx, DexBody body) {
        Set<Register> syntheticRegisters = new HashSet<>();
        body.getThisRegister().ifPresent(syntheticRegisters::add);
        syntheticRegisters.addAll(body.getParameterRegisters());

        List<Register> unnamedRegisters = body.getRegisters().stream()
                .filter(register -> !syntheticRegisters.contains(register))
                .collect(Collectors.toList());

        for (Register register : unnamedRegisters) {
            printer.addText("register ");

            if (register.getType() == null) {
                // Registers should always have a type. It's only absent during conversions from other intermediations.
                printer.addText("<null>");
            } else {
                TypeTextifier.getInstance().textify(printer, register.getType());
            }

            printer.addText(" ");
            printer.addLiteral(ctx.getRegisterName(register));
            printer.addText(";");
            printer.newLine();
        }

        if (!unnamedRegisters.isEmpty()) {
            printer.newLine();
        }
    }

    private void textifyInstructions(Printer printer, TextifyCtx ctx, DexBody body) {
        for (Instruction instruction : body.getInstructions()) {
            ctx.getLabelOpt(instruction).ifPresent(label -> {
                printer.decreaseIndent();
                printer.addLiteral(label);
                printer.addText(":");
                printer.newLine();
                printer.increaseIndent();
            });

            InstructionTextifier.getInstance().textify(printer, ctx, instruction);
            printer.newLine();
        }
    }

    private void textifyTryCatchBlocks(Printer printer, TextifyCtx ctx, DexBody body) {
        for (TryCatchBlock tryCatchBlock : body.getTryCatchBlocks()) {
            printer.addText("try ");
            printer.addLiteral(ctx.getLabel(tryCatchBlock.getStart()));
            printer.addText(" -> ");
            printer.addLiteral(ctx.getLabel(tryCatchBlock.getEnd()));
            printer.addText(" catch {");
            if (tryCatchBlock.getHandlers().size() == 1) {
                printer.addText(" ");
                printHandler(printer, ctx, tryCatchBlock.getHandlers().get(0));
                printer.addText(" ");
            } else {
                printer.newLine();
                for (TryCatchBlock.Handler handler : tryCatchBlock.getHandlers()) {
                    printHandler(printer, ctx, handler);
                    printer.newLine();
                }
            }
            printer.addText("}");
            printer.newLine();
        }
    }

    private void printHandler(Printer printer, TextifyCtx ctx, TryCatchBlock.Handler handler) {
        handler.getException().ifPresentOrElse(exception -> {
            printer.addText("case ");
            printer.addPath(exception);
            printer.addText(": ");
        }, () -> {
            printer.addText("default: ");
        });

        printer.addLiteral(ctx.getLabel(handler.getHandler()));
        printer.addText(";");
    }
}
