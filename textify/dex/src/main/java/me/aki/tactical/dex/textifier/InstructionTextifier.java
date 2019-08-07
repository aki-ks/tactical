package me.aki.tactical.dex.textifier;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.textify.ConstantTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.insn.litmath.*;
import me.aki.tactical.dex.insn.math.*;
import me.aki.tactical.dex.invoke.*;

import java.util.*;

public class InstructionTextifier implements CtxTextifier<Instruction> {
    private static final InstructionTextifier INSTANCE = new InstructionTextifier();

    public static InstructionTextifier getInstance() {
        return INSTANCE;
    }

    private static final CtxTextifier<GotoInstruction> GOTO = (printer, ctx, insn) -> {
        printer.addText("goto ");
        printer.addLiteral(ctx.getLabel(insn.getTarget()));
        printer.addText(";");
    };

    private static final CtxTextifier<IfInstruction> IF = new CtxTextifier<>() {
        @Override
        public void textify(Printer printer, TextifyCtx ctx, IfInstruction value) {
            printer.addText("if (");
            printer.addLiteral(ctx.getRegisterName(value.getOp1()));
            printer.addText(" " + getCompareSymbol(value.getComparison()) + " ");
            value.getOp2().ifPresentOrElse(
                    op2 -> printer.addLiteral(ctx.getRegisterName(op2)),
                    () -> printer.addText("0"));
            printer.addText(") goto ");
            printer.addLiteral(ctx.getLabel(value.getTarget()));
            printer.addText(";");
        }

        private String getCompareSymbol(IfInstruction.Comparison comparison) {
            switch (comparison) {
                case EQUAL: return "==";
                case NON_EQUAL: return "!=";
                case LESS_THAN: return "<";
                case LESS_EQUAL: return "<=";
                case GREATER_THAN: return ">";
                case GREATER_EQUAL: return ">=";
                default: throw new AssertionError();
            }
        }
    };

    private static final CtxTextifier<SwitchInstruction> SWITCH = (printer, ctx, insn) -> {
        printer.addText("switch (");
        printer.addLiteral(ctx.getRegisterName(insn.getValue()));
        printer.addText(") {");
        printer.newLine();
        printer.increaseIndent();

        insn.getBranchTable().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    printer.addText("case " + entry.getKey() + ": goto ");
                    printer.addLiteral(ctx.getLabel(entry.getValue()));
                    printer.addText(";");
                });

        printer.decreaseIndent();
        printer.addText("};");
    };

    private static final CtxTextifier<BranchInstruction> BRANCH = (printer, ctx, insn) -> {
        if (insn instanceof GotoInstruction) {
            GOTO.textify(printer, ctx, (GotoInstruction) insn);
        } else if (insn instanceof IfInstruction) {
            IF.textify(printer, ctx, (IfInstruction) insn);
        } else if (insn instanceof SwitchInstruction) {
            SWITCH.textify(printer, ctx, (SwitchInstruction) insn);
        } else {
            TextUtil.assertionError();
        }
    };

    private static final CtxTextifier<AbstractBinaryMathInstruction> BINARY_MATH_INSN = new CtxTextifier<>() {
        @Override
        public void textify(Printer printer, TextifyCtx ctx, AbstractBinaryMathInstruction insn) {
            Type type = insn instanceof AbstractLogicMathInstruction ? ((AbstractLogicMathInstruction) insn).getType() :
                    insn instanceof AbstractArithmeticMathInstruction ? ((AbstractArithmeticMathInstruction) insn).getType() :
                    TextUtil.assertionError();

            printer.addLiteral(ctx.getRegisterName(insn.getResult()));
            printer.addLiteral(" = ");
            printer.addLiteral(ctx.getRegisterName(insn.getOp1()));
            printer.addText(" ");
            printer.addText(getInsnKeyword(insn));
            printer.addText(" ");
            printer.addLiteral(ctx.getRegisterName(insn.getOp2()));
            printer.addText(" : ");
            TypeTextifier.getInstance().textify(printer, type);
            printer.addText(";");
        }

        private String getInsnKeyword(AbstractBinaryMathInstruction insn) {
            return insn instanceof AddInstruction ? "+" :
                    insn instanceof SubInstruction ? "-" :
                    insn instanceof MulInstruction ? "*" :
                    insn instanceof DivInstruction ? "/" :
                    insn instanceof ModInstruction ? "%" :
                    insn instanceof ShlInstruction ? "<<" :
                    insn instanceof ShrInstruction ? ">>" :
                    insn instanceof UShrInstruction ? ">>>" :
                    insn instanceof AndInstruction ? "&" :
                    insn instanceof OrInstruction ? "|" :
                    insn instanceof XorInstruction ? "^" :
                    TextUtil.assertionError();
        }
    };

    private static final CtxTextifier<AbstractBinaryLitMathInstruction> LIT_MATH = new CtxTextifier<>() {
        @Override
        public void textify(Printer printer, TextifyCtx ctx, AbstractBinaryLitMathInstruction insn) {
            boolean isReverse = insn instanceof RSubLitInstruction;

            printer.addLiteral(ctx.getRegisterName(insn.getResult()));
            printer.addText(" = ");

            if (isReverse) {
                printer.addText(Short.toString(insn.getOp2()));
            } else {
                printer.addLiteral(ctx.getRegisterName(insn.getOp1()));
            }

            printer.addText(" ");
            printer.addText(getInsnKeyword(insn));
            printer.addText(" ");

            if (isReverse) {
                printer.addLiteral(ctx.getRegisterName(insn.getOp1()));
            } else {
                printer.addText(Short.toString(insn.getOp2()));
            }

            printer.addText(";");
        }

        private String getInsnKeyword(AbstractBinaryLitMathInstruction insn) {
            return insn instanceof AddLitInstruction ? "+" :
                    insn instanceof RSubLitInstruction ? "-" :
                    insn instanceof MulLitInstruction ? "*" :
                    insn instanceof DivLitInstruction ? "/" :
                    insn instanceof ModLitInstruction ? "%" :
                    insn instanceof ShlLitInstruction ? "<<" :
                    insn instanceof ShrLitInstruction ? ">>" :
                    insn instanceof UShrLitInstruction ? ">>>" :
                    insn instanceof AndLitInstruction ? "&" :
                    insn instanceof OrLitInstruction ? "|" :
                    insn instanceof XorLitInstruction ? "^" :
                    TextUtil.assertionError();
        }
    };

    private static final CtxTextifier<AbstractCompareInstruction> COMPARE = (printer, ctx, insn) -> {
        String keyword;
        Optional<Type> typeOpt;
        if (insn instanceof CmpInstruction) {
            keyword = "cmp";
            typeOpt = Optional.empty();
        } else if (insn instanceof CmpgInstruction) {
            keyword = "cmpg";
            typeOpt = Optional.ofNullable(((CmpgInstruction) insn).getType());
        } else if (insn instanceof CmplInstruction) {
            keyword = "cmpl";
            typeOpt = Optional.ofNullable(((CmplInstruction) insn).getType());
        } else {
            throw new AssertionError();
        }

        printer.addLiteral(ctx.getRegisterName(insn.getResult()));
        printer.addLiteral(" = ");
        printer.addLiteral(ctx.getRegisterName(insn.getOp1()));
        printer.addText(" " + keyword + " ");
        printer.addLiteral(ctx.getRegisterName(insn.getOp2()));
        typeOpt.ifPresent(type -> {
            printer.addText(" : ");
            TypeTextifier.getInstance().textify(printer, type);
        });
        printer.addText(";");
    };

    private static final CtxTextifier<NegInstruction> NEG = (printer, ctx, insn) -> {
        printer.addLiteral(ctx.getRegisterName(insn.getResult()));
        printer.addText(" = !");
        printer.addLiteral(ctx.getRegisterName(insn.getValue()));
        printer.addText(" : ");
        TypeTextifier.PRIMITIVE_TYPE.textify(printer, insn.getType());
        printer.addText(";");
    };

    private static final CtxTextifier<NotInstruction> NOT = (printer, ctx, insn) -> {
        printer.addLiteral(ctx.getRegisterName(insn.getResult()));
        printer.addText(" = ~");
        printer.addLiteral(ctx.getRegisterName(insn.getValue()));
        printer.addText(" : ");
        TypeTextifier.PRIMITIVE_TYPE.textify(printer, insn.getType());
        printer.addText(";");
    };

    private static final CtxTextifier<ArrayLengthInstruction> ARRAY_LENGTH = (printer, ctx, insn) -> {
        printer.addLiteral(ctx.getRegisterName(insn.getResult()));
        printer.addText(" = ");
        printer.addLiteral(ctx.getRegisterName(insn.getArray()));
        printer.addText(".length;");
    };

    private static final CtxTextifier<ArrayLoadInstruction> ARRAY_LOAD = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getResult()));
        printer.addText(" = ");
        printer.addLiteral(ctx.getRegisterName(value.getArray()));
        printer.addText("[");
        printer.addLiteral(ctx.getRegisterName(value.getIndex()));
        printer.addText("];");
    };

    private static final CtxTextifier<ArrayStoreInstruction> ARRAY_STORE = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getArray()));
        printer.addText("[");
        printer.addLiteral(ctx.getRegisterName(value.getIndex()));
        printer.addText("] = ");
        printer.addLiteral(ctx.getRegisterName(value.getValue()));
        printer.addText(";");
    };

    private static final CtxTextifier<ConstInstruction> CONST = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(" = ");
        ConstantTextifier.DEX.textify(printer, value.getConstant());
        printer.addText(";");
    };

    private static final CtxTextifier<ReturnInstruction> RETURN = (printer, ctx, value) -> {
        printer.addText("return ");
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(";");
    };

    private static final CtxTextifier<ThrowInstruction> THROW = (printer, ctx, value) -> {
        printer.addText("throw ");
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(";");
    };

    private static final CtxTextifier<MonitorEnterInstruction> MONITOR_ENTER = (printer, ctx, value) -> {
        printer.addText("monitor enter ");
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(";");
    };

    private static final CtxTextifier<MonitorExitInstruction> MONITOR_EXIT = (printer, ctx, value) -> {
        printer.addText("monitor exit ");
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(";");
    };

    private static final CtxTextifier<FieldGetInstruction> FIELD_GET = (printer, ctx, value) -> {
        FieldRef field = value.getField();
        printer.addLiteral(ctx.getRegisterName(value.getResult()));
        printer.addText(" = ");
        printFieldRef(printer, ctx, field, value.getInstance());
        printer.addText(";");
    };

    private static final CtxTextifier<FieldSetInstruction> FIELD_SET = (printer, ctx, value) -> {
        FieldRef field = value.getField();
        printFieldRef(printer, ctx, field, value.getInstance());
        printer.addText(" = ");
        printer.addLiteral(ctx.getRegisterName(value.getValue()));
        printer.addText(";");
    };

    private static void printFieldRef(Printer printer, TextifyCtx ctx, FieldRef field, Optional<Register> instanceOpt) {
        printer.addPath(field.getOwner());
        printer.addText(".");
        instanceOpt.ifPresent(instance -> {
            printer.addText("<");
            printer.addLiteral(ctx.getRegisterName(instance));
            printer.addText(">.");
        });
        printer.addLiteral(field.getName());
        printer.addText(" : ");
        TypeTextifier.getInstance().textify(printer, field.getType());
    }

    private static final CtxTextifier<InstanceOfInstruction> INSTANCE_OF = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getResult()));
        printer.addText(" = ");
        printer.addLiteral(ctx.getRegisterName(value.getValue()));
        printer.addText(" instanceof ");
        TypeTextifier.REF_TYPE.textify(printer, value.getType());
        printer.addText(";");
    };

    private static final CtxTextifier<MoveExceptionInstruction> MOVE_EXCEPTION = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(" = catch;");
    };

    private static final CtxTextifier<MoveInstruction> MOVE = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getFrom()));
        printer.addText(" = ");
        printer.addLiteral(ctx.getRegisterName(value.getTo()));
        printer.addText(" : ");
        TypeTextifier.getInstance().textify(printer, value.getType());
        printer.addText(";");
    };

    private static final CtxTextifier<MoveResultInstruction> MOVE_RESULT = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(" = result;");
    };

    private static final CtxTextifier<PrimitiveCastInstruction> PRIMITIVE_CAST = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getToRegister()));
        printer.addText(" = (");
        TypeTextifier.PRIMITIVE_TYPE.textify(printer, value.getFromType());
        printer.addText(" -> ");
        TypeTextifier.PRIMITIVE_TYPE.textify(printer, value.getToType());
        printer.addText(") ");
        printer.addLiteral(ctx.getRegisterName(value.getFromRegister()));
        printer.addText(";");
    };

    private static final CtxTextifier<RefCastInstruction> REF_CAST = (printer, ctx, value) -> {
        printer.addText("cast ");
        printer.addLiteral(ctx.getRegisterName(value.getRegister()));
        printer.addText(" : ");
        TypeTextifier.REF_TYPE.textify(printer, value.getType());
        printer.addText(";");
    };

    private static final CtxTextifier<FillArrayInstruction> FILL_ARRAY = (printer, ctx, value) -> {
        int size = value.getElementSize().getByteSize();
        printer.addText("fill ");
        printer.addLiteral(ctx.getRegisterName(value.getArray()));

        Iterator<FillArrayInstruction.NumericConstant> iterator = value.getValues().iterator();
        if (iterator.hasNext()) {
            printer.addText(" { ");

            while (iterator.hasNext()) {
                long constant = iterator.next().longValue();
                printer.addText(Long.toString(constant));

                if (iterator.hasNext()) {
                    printer.addText(", ");
                }
            }

            printer.addText(" }");
        } else {
            printer.addText(" {}");
        }

        printer.addText(" : " + size + ";");
    };

    private static final CtxTextifier<NewArrayInstruction> NEW_ARRAY = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getResult()));
        printer.addText(" = new ");
        TypeTextifier.getInstance().textify(printer, value.getArrayType().getBaseType());

        printer.addText("[");
        printer.addLiteral(ctx.getRegisterName(value.getSize()));
        printer.addText("]");

        int dimensions = value.getArrayType().getDimensions();
        for (int i = 1; i < dimensions; i++) {
            printer.addText("[]");
        }

        printer.addText(";");
    };

    private static final CtxTextifier<NewFilledArrayInstruction> NEW_FILLED_ARRAY = (printer, ctx, value) -> {
        printer.addText("new ");
        TypeTextifier.ARRAY.textify(printer, value.getType());

        Iterator<Register> iterator = value.getRegisters().iterator();
        if (iterator.hasNext()) {
            printer.addText(" { ");
            while (iterator.hasNext()) {
                printer.addLiteral(ctx.getRegisterName(iterator.next()));
                if (iterator.hasNext()) {
                    printer.addText(", ");
                }
            }
            printer.addText(" };");
        } else {
            printer.addText(" {};");
        }
    };

    private static final CtxTextifier<NewInstanceInstruction> NEW_INSTANCE = (printer, ctx, value) -> {
        printer.addLiteral(ctx.getRegisterName(value.getResult()));
        printer.addText(" = new ");
        printer.addPath(value.getType());
        printer.addText(";");
    };

    @Override
    public void textify(Printer printer, TextifyCtx ctx, Instruction insn) {
        if (insn instanceof BranchInstruction) {
            BRANCH.textify(printer, ctx, (BranchInstruction) insn);
        } else if (insn instanceof AbstractBinaryMathInstruction) {
            BINARY_MATH_INSN.textify(printer, ctx, (AbstractBinaryMathInstruction) insn);
        } else if (insn instanceof AbstractBinaryLitMathInstruction) {
            LIT_MATH.textify(printer, ctx, (AbstractBinaryLitMathInstruction) insn);
        } else if (insn instanceof AbstractCompareInstruction) {
            COMPARE.textify(printer, ctx, (AbstractCompareInstruction) insn);
        } else if (insn instanceof NegInstruction) {
            NEG.textify(printer, ctx, (NegInstruction) insn);
        } else if (insn instanceof NotInstruction) {
            NOT.textify(printer, ctx, (NotInstruction) insn);
        } else if (insn instanceof ArrayLengthInstruction) {
            ARRAY_LENGTH.textify(printer, ctx, (ArrayLengthInstruction) insn);
        } else if (insn instanceof ArrayLoadInstruction) {
            ARRAY_LOAD.textify(printer, ctx, (ArrayLoadInstruction) insn);
        } else if (insn instanceof ArrayStoreInstruction) {
            ARRAY_STORE.textify(printer, ctx, (ArrayStoreInstruction) insn);
        } else if (insn instanceof ConstInstruction) {
            CONST.textify(printer, ctx, (ConstInstruction) insn);
        } else if (insn instanceof ReturnInstruction) {
            RETURN.textify(printer, ctx, (ReturnInstruction) insn);
        } else if (insn instanceof ReturnVoidInstruction) {
            printer.addText("return;");
        } else if (insn instanceof ThrowInstruction) {
            THROW.textify(printer, ctx, (ThrowInstruction) insn);
        } else if (insn instanceof MonitorEnterInstruction) {
            MONITOR_ENTER.textify(printer, ctx, (MonitorEnterInstruction) insn);
        } else if (insn instanceof MonitorExitInstruction) {
            MONITOR_EXIT.textify(printer, ctx, (MonitorExitInstruction) insn);
        } else if (insn instanceof FieldGetInstruction) {
            FIELD_GET.textify(printer, ctx, (FieldGetInstruction) insn);
        } else if (insn instanceof FieldSetInstruction) {
            FIELD_SET.textify(printer, ctx, (FieldSetInstruction) insn);
        } else if (insn instanceof InstanceOfInstruction) {
            INSTANCE_OF.textify(printer, ctx, (InstanceOfInstruction) insn);
        } else if (insn instanceof InvokeInstruction) {
            Invoke invoke = ((InvokeInstruction) insn).getInvoke();
            InvokeTextifier.getInstance().textify(printer, ctx, invoke);
        } else if (insn instanceof MoveExceptionInstruction) {
            MOVE_EXCEPTION.textify(printer, ctx, (MoveExceptionInstruction) insn);
        } else if (insn instanceof MoveInstruction) {
            MOVE.textify(printer, ctx, (MoveInstruction) insn);
        } else if (insn instanceof MoveResultInstruction) {
            MOVE_RESULT.textify(printer, ctx, (MoveResultInstruction) insn);
        } else if (insn instanceof RefCastInstruction) {
            REF_CAST.textify(printer, ctx, (RefCastInstruction) insn);
        } else if (insn instanceof PrimitiveCastInstruction) {
            PRIMITIVE_CAST.textify(printer, ctx, (PrimitiveCastInstruction) insn);
        } else if (insn instanceof FillArrayInstruction) {
            FILL_ARRAY.textify(printer, ctx, (FillArrayInstruction) insn);
        } else if (insn instanceof NewArrayInstruction) {
            NEW_ARRAY.textify(printer, ctx, (NewArrayInstruction) insn);
        } else if (insn instanceof NewFilledArrayInstruction) {
            NEW_FILLED_ARRAY.textify(printer, ctx, (NewFilledArrayInstruction) insn);
        } else if (insn instanceof NewInstanceInstruction) {
            NEW_INSTANCE.textify(printer, ctx, (NewInstanceInstruction) insn);
        }
    }
}
