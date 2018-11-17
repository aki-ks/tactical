package me.aki.tactical.conversion.ref2stack;

import me.aki.tactical.conversion.refutils.CfgUnitGraph;
import me.aki.tactical.conversion.stackasm.analysis.Stack;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.TryCatchBlock;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BodyConverter {
    private final RefBody refBody;
    private final StackBody stackBody;

    private final ConversionContext ctx = new ConversionContext();

    private final Map<Statement, List<Instruction>> convertedStatements = new HashMap<>();

    private Stack.Mutable<Type> stackState = new Stack.Mutable<>();

    public BodyConverter(RefBody refBody) {
        this(refBody, new StackBody());
    }

    public BodyConverter(RefBody refBody, StackBody stackBody) {
        this.refBody = refBody;
        this.stackBody = stackBody;
    }

    public StackBody getStackBody() {
        return stackBody;
    }

    public RefBody getRefBody() {
        return refBody;
    }

    public void convert() {
        convertLocals();

        convertInstructions();
        resolveInsnReferences();
        insertConvertedInstructions();
    }

    private void convertLocals() {
        Map<RefLocal, StackLocal> localMap = ctx.getLocalMap();
        List<StackLocal> locals = this.stackBody.getLocals();
        for (RefLocal refLocal : refBody.getLocals()) {
            StackLocal stackLocal = new StackLocal();
            localMap.put(refLocal, stackLocal);
            locals.add(stackLocal);
        }

        Optional<StackLocal> thisLocalOpt = refBody.getThisLocal().map(ctx::getStackLocal);
        List<StackLocal> parameterLocals = this.refBody.getArgumentLocals().stream()
                .map(ctx::getStackLocal)
                .collect(Collectors.toList());

        this.stackBody.setThisLocal(thisLocalOpt);
        this.stackBody.setParameterLocals(parameterLocals);
    }

    private void convertInstructions() {
        CfgUnitGraph graph = new CfgUnitGraph(refBody);
        Deque<CfgUnitGraph.Node> worklist = new ArrayDeque<>();
        Set<CfgUnitGraph.Node> visited = new HashSet<>();

        worklist.add(graph.getNode(refBody.getStatements().getFirst()));
        for (TryCatchBlock tryCatchBlock : refBody.getTryCatchBlocks()) {
            worklist.add(graph.getNode(tryCatchBlock.getHandler()));
        }

        StackInsnWriter writer = new StackInsnWriter(ctx);
        RefInsnReader reader = new RefInsnReader(ctx, writer);

        CfgUnitGraph.Node node;
        while ((node = worklist.poll()) != null) {
            Statement statement = node.getStatement();

            if (!visited.add(node)) {
                continue;
            }

            reader.accept(statement);
            List<Instruction> instructions = writer.getInstructions();
            convertedStatements.put(statement, new ArrayList<>(instructions));
            instructions.clear();

            worklist.addAll(node.getSucceeding());
        }
    }

    private void resolveInsnReferences() {
        ctx.getInstructionsRefs().forEach((stmt, cells) -> {
            Instruction instruction = convertedStatements.get(stmt).get(0);
            cells.forEach(cell -> cell.set(instruction));
        });
    }

    private void insertConvertedInstructions() {
        for (Statement statement : refBody.getStatements()) {
            List<Instruction> instructions = convertedStatements.getOrDefault(statement, Collections.emptyList());
            stackBody.getInstructions().addAll(instructions);
        }
    }
}
