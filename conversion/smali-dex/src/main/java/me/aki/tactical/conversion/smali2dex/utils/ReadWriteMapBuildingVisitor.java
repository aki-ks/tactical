package me.aki.tactical.conversion.smali2dex.utils;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnReader;

import java.util.*;

public class ReadWriteMapBuildingVisitor extends TypeHintInsnVisitor {
    /**
     * Build a read and write map from a {@link DexCfgGraph}.
     *
     * @param cfgGraph a control flow graph of a method
     * @return a completely build read and write map
     */
    public static ReadWriteMapBuildingVisitor build(DexCfgGraph cfgGraph) {
        ReadWriteMapBuildingVisitor iv = new ReadWriteMapBuildingVisitor();
        DexInsnReader reader = new DexInsnReader(iv);
        cfgGraph.forEachNode(node -> {
            iv.setInstruction(node);
            reader.accept(node.getInstruction());
        });
        return iv;
    }

    /**
     * Store for a register which instructions read from it and which type they expect.
     */
    private final Map<Register, Set<WithTypeOption<Instruction>>> readMap = new HashMap<>();

    /**
     * Store for a register which instructions write to it and which type they expect.
     */
    private final Map<Register, Set<WithTypeOption<Instruction>>> writeMap = new HashMap<>();

    public Map<Register, Set<WithTypeOption<Instruction>>> getReadMap() {
        return readMap;
    }

    public Map<Register, Set<WithTypeOption<Instruction>>> getWriteMap() {
        return writeMap;
    }

    @Override
    protected void visitRegisterRead(Optional<Type> type, Register register) {
        this.readMap.computeIfAbsent(register, x -> new HashSet<>())
                .add(new WithTypeOption<>(type, this.instruction.getInstruction()));
    }

    @Override
    protected void visitRegisterWrite(Optional<Type> type, Register register) {
        this.writeMap.computeIfAbsent(register, x -> new HashSet<>())
                .add(new WithTypeOption<>(type, this.instruction.getInstruction()));
    }

    /**
     * Zip a value with a type.
     *
     * The type is just additional meta information.
     * It is ignored by {@link WithTypeOption#equals(Object)} and
     * {@link WithTypeOption#hashCode()} methods.
     */
    public class WithTypeOption<T> {
        private final Optional<Type> type;
        private final T value;

        public WithTypeOption(Optional<Type> type, T value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WithTypeOption<?> that = (WithTypeOption<?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
