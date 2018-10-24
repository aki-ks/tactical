package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.Method;
import me.aki.tactical.core.textify.BodyTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeAnnotationTextifier;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.TryCatchBlock;
import me.aki.tactical.ref.stmt.BranchStmt;

import javax.naming.Context;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RefBodyTextifier implements BodyTextifier {
    @Override
    public void textifyParameters(Printer printer, Method method) {
        Iterator<Type> paramIter = method.getParameterTypes().iterator();
        int parameterCount = method.getParameterTypes().size();

        int parameterIndex = 0;
        while (paramIter.hasNext()) {
            TypeTextifier.getInstance().textify(printer, paramIter.next());
            printer.addText(" ");
            printer.addLiteral(getParameterLocalName(parameterIndex++, parameterCount));

            if (paramIter.hasNext()) {
                printer.addText(", ");
            }
        }
    }

    private String getParameterLocalName(int parameter, int parameterCount) {
        return "param" + TextUtil.paddedNumber(parameter, parameterCount);
    }

    @Override
    public void textify(Printer printer, Method method) {
        RefBody body = (RefBody) method.getBody().get();
        TextifyCtx ctx = new TextifyCtx(getNamedLocals(body), getLabels(body));

        textifyLocals(printer, ctx, body);

        textifyStatement(printer, ctx, body);

        textifyTryCatchBlocks(printer, ctx, body);
        textifyLines(printer, ctx, body);
        textifyLocalVariables(printer, ctx, body);
    }

    /**
     * Compute names for all locals.
     *
     * @return map from locals to their names
     */
    private Map<RefLocal, String> getNamedLocals(RefBody body) {
        Map<RefLocal, String> nameMap = new HashMap<>();

        body.getThisLocal().ifPresent(thisLocal ->
                nameMap.put(thisLocal, "this"));

        int parameterCount = body.getArgumentLocals().size();
        int paramIndex = 0;
        for (RefLocal argumentLocal : body.getArgumentLocals()) {
            nameMap.put(argumentLocal, getParameterLocalName(paramIndex++, parameterCount));
        }

        List<RefLocal> unnamedLocals = body.getLocals().stream()
                .filter(local -> !nameMap.containsKey(local))
                .collect(Collectors.toList());

        int localIndex = 0;
        for (RefLocal unnamedLocal : unnamedLocals) {
            nameMap.put(unnamedLocal, "local" + TextUtil.paddedNumber(localIndex, unnamedLocals.size()));
        }

        return nameMap;
    }

    /**
     * Assign a label name to all instructions that are referenced somewhere in the method.
     */
    private Map<Statement, String> getLabels(RefBody body) {
        Map<Statement, String> labels = new HashMap<>();
        Set<Statement> referencedStatements = getReferencedStatements(body);

        int labelIndex = 0;
        for (Statement statement : body.getStatements()) {
            if (referencedStatements.contains(statement)) {
                labels.put(statement, "label" + TextUtil.paddedNumber(labelIndex, referencedStatements.size()));
            }
        }

        return labels;
    }

    /**
     * Get all statements that are referenced somewhere within the method.
     *
     * @param body body that contains all statements
     * @return a set of all referenced statements
     */
    private Set<Statement> getReferencedStatements(RefBody body) {
        Set<Statement> statements = new HashSet<>();

        for (Statement statement : body.getStatements()) {
            if (statement instanceof BranchStmt) {
                statements.addAll(((BranchStmt) statement).getBranchTargets());
            }
        }

        for (TryCatchBlock tryCatchBlock : body.getTryCatchBlocks()) {
            statements.add(tryCatchBlock.getFirst());
            statements.add(tryCatchBlock.getLast());
            statements.add(tryCatchBlock.getHandler());
        }

        for (RefBody.LocalVariable localVariable : body.getLocalVariables()) {
            statements.add(localVariable.getStart());
            statements.add(localVariable.getEnd());
        }

        for (RefBody.LocalVariableAnnotation localVariableAnnotation : body.getLocalVariableAnnotations()) {
            for (RefBody.LocalVariableAnnotation.Location location : localVariableAnnotation.getLocations()) {
                statements.add(location.getStart());
                statements.add(location.getEnd());
            }
        }

        for (RefBody.LineNumber lineNumber : body.getLineNumbers()) {
            statements.add(lineNumber.getStatement());
        }

        return statements;
    }

    private void textifyLocals(Printer printer, TextifyCtx ctx, RefBody body) {
        Set<RefLocal> hiddenLocals = new HashSet<>();
        body.getThisLocal().ifPresent(hiddenLocals::add);
        hiddenLocals.addAll(body.getArgumentLocals());

        for (RefLocal local : body.getLocals()) {
            if (hiddenLocals.contains(local)) {
                continue;
            }

            if (local.getType() == null) {
                // Locals should always have a type. It's only absent during conversions from other intermediations.
                printer.addText("<null>");
            } else {
                TypeTextifier.getInstance().textify(printer, local.getType());
            }

            printer.addText(" ");
            printer.addLiteral(ctx.getLocalName(local));
            printer.addText(";");
        }

        if (!body.getLocals().isEmpty()) {
            printer.newLine();
        }
    }

    private void textifyStatement(Printer printer, TextifyCtx ctx, RefBody body) {
        for (Statement statement : body.getStatements()) {
            ctx.getLabelOpt(statement).ifPresent(label -> {
                printer.decreaseIndent();
                printer.addLiteral(label);
                printer.addText(":");
                printer.newLine();
                printer.increaseIndent();
            });

            StatementTextifier.getInstance().textify(printer, ctx, statement);
            printer.newLine();
        }
    }

    private void textifyTryCatchBlocks(Printer printer, TextifyCtx ctx, RefBody body) {
        for (TryCatchBlock block : body.getTryCatchBlocks()) {
            printer.addText("try ");
            printer.addLiteral(ctx.getLabel(block.getFirst()));
            printer.addText(" -> ");
            printer.addLiteral(ctx.getLabel(block.getLast()));
            printer.addText(" catch ");
            printer.addLiteral(ctx.getLabel(block.getHandler()));
            printer.addText(" ");
            printer.addLiteral(ctx.getLocalName(block.getExceptionLocal()));
            block.getException().ifPresent(exception -> {
                printer.addText(" : ");
                printer.addPath(exception);
            });
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyLines(Printer printer, TextifyCtx ctx, RefBody body) {
        for (RefBody.LineNumber node : body.getLineNumbers()) {
            printer.addText("line " + node.getLine() + " ");
            printer.addLiteral(ctx.getLabel(node.getStatement()));
            printer.addText(";");
            printer.newLine();
        }
    }

    private void textifyLocalVariables(Printer printer, TextifyCtx ctx, RefBody body) {
        for (RefBody.LocalVariable local : body.getLocalVariables()) {
            printer.addText("local info ");
            printer.addLiteral(ctx.getLabel(local.getStart()));
            printer.addText(" -> ");
            printer.addLiteral(ctx.getLabel(local.getEnd()));
            printer.addText(" ");
            printer.addLiteral(ctx.getLocalName(local.getLocal()));
            printer.addText(" ");
            printer.addEscaped(local.getName(), '"');
            printer.addText(" ");
            TypeTextifier.getInstance().textify(printer, local.getType());
            local.getSignature().ifPresent(signature -> {
                printer.addText(" ");
                printer.addEscaped(signature, '"');
            });
            printer.addText(";");
            printer.newLine();
        }
    }
}
