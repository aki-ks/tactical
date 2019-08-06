package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnReader;

import java.util.*;
import java.util.stream.Collectors;

public class InsnTyper {
    private final Method method;
    private final DexBody body;
    private final DexCfgGraph cfgGraph;
    private final List<UntypedInfo> untyped;

    public InsnTyper(Method method, DexBody body, DexCfgGraph cfgGraph, List<UntypedInfo> untyped) {
        this.method = method;
        this.body = body;
        this.cfgGraph = cfgGraph;
        this.untyped = untyped;
    }

    public void doTyping() {
        // Try to type all instructions.
        // If instructions were successfully typed, typing other not yet typeable instructions might be possible now.
        while (!this.untyped.isEmpty()) {
            boolean wasAnyInsnTyped = false;

            Iterator<UntypedInfo> iterator = this.untyped.iterator();
            while (iterator.hasNext()) {
                UntypedInfo next = iterator.next();
                if (tryDoTyping(next)) {
                    iterator.remove();
                    wasAnyInsnTyped = true;
                }
            }

            if (!wasAnyInsnTyped) {
                removeUnusedUntypableInsns();
                return;
            }
        }
    }

    /**
     * Remove any instructions that could not have been typed, have no side effects and whose result value is never used.
     */
    private void removeUnusedUntypableInsns() {
        untyped.removeIf(untypedInfo -> {
            if (!untypedInfo.hasSideEffects()) {
                Instruction insn = untypedInfo.getInstruction();
                if (insn.getWrittenRegister().isPresent()) {
                    Register register = insn.getWrittenRegister().get();
                    return findSucceedingReads(insn, register).isEmpty();
                }
            }
            return false;
        });

        if (!this.untyped.isEmpty()) {
            throw new RuntimeException("Could not type all instructions");
        }
    }

    private boolean tryDoTyping(UntypedInfo task) {
        Map<Register, Set<Type>> typesBefore = task.getBefore().stream().collect(Collectors.toMap(
                RCell::get,
                register -> findPrecedingStates(task.getInstruction(), register.get())
        ));

        Optional<Set<Type>> typesAfter = task.getAfter()
                .map(register -> findSucceedingReads(task.getInstruction(), register.get()));

        return task.setType(typesBefore, typesAfter);
    }

    private Set<Type> findPrecedingStates(Instruction start, Register register) {
        class Inner {
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();
            private final Set<Type> types = new HashSet<>();

            public Inner() {
                cfgGraph.getNode(start).getPreceeding().forEach(this::walkBackward);
            }

            public void walkBackward(DexCfgGraph.Node node) {
                if (!visited.add(node)) {
                    return;
                }

                Instruction insn = node.getInstruction();

                Optional<Register> write = insn.getWrittenRegister();
                boolean writesToRegister = write.isPresent() && write.get() == register;
                if (writesToRegister) {
                    insertWriteTypes(node, insn);

                    // The register is overwritten, so the value cannot be
                    // read by this or any succeeding instructions
                    return;
                }

                boolean readsFromRegister = insn.getReadRegisters().contains(register);
                if (readsFromRegister) {
                    insertReadTypes(node, insn);
                }

                node.getPreceeding().forEach(this::walkBackward);
            }

            private void insertWriteTypes(DexCfgGraph.Node node, Instruction insn) {
                TypeHintInsnVisitor iv = new TypeHintInsnVisitor(method.getReturnType()) {
                    @Override
                    protected void visit(RegisterAccess action) {
                        action.getWrites().get(register).ifPresent(types::add);
                    }
                };
                iv.setInstruction(node);
                new DexInsnReader(iv).accept(insn);
            }

            private void insertReadTypes(DexCfgGraph.Node node, Instruction insn) {
                TypeHintInsnVisitor iv = new TypeHintInsnVisitor(method.getReturnType()) {
                    @Override
                    protected void visit(RegisterAccess action) {
                        action.getReads().get(register).ifPresent(types::add);
                    }
                };
                iv.setInstruction(node);
                new DexInsnReader(iv).accept(insn);
            }
        }

        return new Inner().types;
    }

    private Set<Type> findSucceedingReads(Instruction start, Register register) {
        class Inner {
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();
            private final Set<Type> reads = new HashSet<>();

            public Inner() {
                cfgGraph.getNode(start).getSucceeding().forEach(this::walkForward);
            }

            public void walkForward(DexCfgGraph.Node node) {
                if (!visited.add(node)) {
                    return;
                }

                Instruction insn = node.getInstruction();
                if (insn.getReadRegisters().contains(register)) {
                    TypeHintInsnVisitor iv = new TypeHintInsnVisitor(method.getReturnType()) {
                        @Override
                        protected void visit(RegisterAccess action) {
                            action.getReads().get(register).ifPresent(reads::add);
                        }
                    };
                    iv.setInstruction(node);
                    new DexInsnReader(iv).accept(insn);
                }

                Optional<Register> write = insn.getWrittenRegister();
                if (write.isPresent() && write.get() == register) {
                    // Another value is written to the register, so there cannot
                    // be any further reads along this way through the cfg.
                    return;
                }

                node.getSucceeding().forEach(this::walkForward);
            }
        }

        return new Inner().reads;
    }
}
