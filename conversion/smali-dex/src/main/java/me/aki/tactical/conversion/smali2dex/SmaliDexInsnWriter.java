package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smali2dex.typing.UntypedInfo;
import me.aki.tactical.conversion.smali2dex.typing.UntypedNumberConstant;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.utils.AbstractDexInsnWriter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Collect events from visitors where instructions references and registers are in smali-dexlib2
 * representation into tactical dex instructions.
 */
public class SmaliDexInsnWriter extends AbstractDexInsnWriter<org.jf.dexlib2.iface.instruction.Instruction, Integer> {
    private final List<Register> registers;
    private final Map<org.jf.dexlib2.iface.instruction.Instruction, Collection<RWCell<Instruction>>> instructionRefs;
    private final List<UntypedInfo> untypedTypes = new ArrayList<>();

    public SmaliDexInsnWriter(List<Register> registers, Map<org.jf.dexlib2.iface.instruction.Instruction, Collection<RWCell<Instruction>>> instructionRefs) {
        this.registers = registers;
        this.instructionRefs = instructionRefs;
    }

    public List<UntypedInfo> getUntypedTypes() {
        return untypedTypes;
    }

    @Override
    protected void visitInstruction(Instruction instruction) {
        if (instruction instanceof ConstInstruction) {
            ConstInstruction constInstruction = (ConstInstruction) instruction;
            DexConstant constant = constInstruction.getConstant();
            if (constant instanceof UntypedNumberConstant) {
                this.untypedTypes.add(new UntypedConstant(constInstruction, (UntypedNumberConstant) constant));
            }
        } else if (instruction instanceof MoveInstruction) {
            this.untypedTypes.add(new UntypedMove((MoveInstruction) instruction));
        } else if (instruction instanceof ArrayLoadInstruction) {
            this.untypedTypes.add(new UntypedArrayLoad((ArrayLoadInstruction) instruction));
        } else if (instruction instanceof ArrayStoreInstruction) {
            this.untypedTypes.add(new UntypedArrayStore((ArrayStoreInstruction) instruction));
        }

        super.visitInstruction(instruction);
    }

    private static class UntypedConstant extends UntypedInfo {
        private final UntypedNumberConstant constant;
        private final ConstInstruction constInstruction;

        private UntypedConstant(ConstInstruction constInstruction, UntypedNumberConstant constant) {
            super(constInstruction, Set.of(), Optional.of(constInstruction.getRegisterCell()));
            this.constInstruction = constInstruction;
            this.constant = constant;
        }

        @Override
        public boolean setType(Map<Register, Set<Type>> typesBefore, Optional<Set<Type>> typeAfter) {
            Optional<Type> typeOpt = typeAfter.flatMap(types -> mergeTypes(types.stream()));
            typeOpt.ifPresent(type -> this.constInstruction.setConstant(toConstant(type)));
            return typeOpt.isPresent();
        }

        private PrimitiveValueConstant toConstant(Type type) {
            return type instanceof IntLikeType ? new IntConstant(this.constant.intValue()) :
            type instanceof LongType ? new LongConstant(this.constant.longValue()) :
            type instanceof FloatType ? new FloatConstant(this.constant.floatValue()) :
            type instanceof DoubleType ? new DoubleConstant(this.constant.doubleValue()) :
            DexUtils.unreachable();
        }

        @Override
        public boolean hasSideEffects() {
            return false;
        }
    }

    private static class UntypedMove extends UntypedInfo {
        private final MoveInstruction moveInsn;

        private UntypedMove(MoveInstruction moveInsn) {
            super(moveInsn, Set.of(moveInsn.getFromCell()), Optional.of(moveInsn.getToCell()));
            this.moveInsn = moveInsn;
        }

        @Override
        public boolean setType(Map<Register, Set<Type>> typesBefore, Optional<Set<Type>> typeAfter) {
            Optional<Type> typeOpt = mergeTypes(Stream.concat(
                    typesBefore.getOrDefault(moveInsn.getFrom(), Set.of()).stream(),
                    typeAfter.stream().flatMap(Set::stream)
            ));

            typeOpt.ifPresent(moveInsn::setType);
            return typeOpt.isPresent();
        }

        @Override
        public boolean hasSideEffects() {
            return false;
        }
    }

    private static class UntypedArrayLoad extends UntypedInfo {
        private final ArrayLoadInstruction loadInsn;

        private UntypedArrayLoad(ArrayLoadInstruction loadInsn) {
            super(loadInsn, Set.of(loadInsn.getArrayCell()), Optional.of(loadInsn.getResultCell()));
            this.loadInsn = loadInsn;
        }

        @Override
        public boolean setType(Map<Register, Set<Type>> typesBefore, Optional<Set<Type>> typeAfter) {
            Optional<Type> arrayLowerType = findArrayType(typesBefore.getOrDefault(loadInsn.getArray(), Set.of()).stream())
                    .map(ArrayType::getLowerType);

            Stream<Type> valueType = typeAfter.stream().flatMap(Set::stream);

            Optional<Type> typeOpt = mergeTypes(Stream.concat(valueType, arrayLowerType.stream()));
            typeOpt.ifPresent(loadInsn::setType);
            return typeOpt.isPresent();
        }

        @Override
        public boolean hasSideEffects() {
            return false;
        }
    }

    private static class UntypedArrayStore extends UntypedInfo {
        private final ArrayStoreInstruction storeInsn;

        private UntypedArrayStore(ArrayStoreInstruction storeInsn) {
            super(storeInsn, Set.of(storeInsn.getArrayCell(), storeInsn.getValueCell()), Optional.empty());
            this.storeInsn = storeInsn;
        }

        @Override
        public boolean setType(Map<Register, Set<Type>> typesBefore, Optional<Set<Type>> typeAfter) {
            Optional<Type> arrayLowerType = findArrayType(typesBefore.getOrDefault(storeInsn.getArray(), Set.of()).stream())
                    .map(ArrayType::getLowerType);

            Stream<Type> valueTypes = typesBefore.getOrDefault(storeInsn.getValue(), Set.of()).stream();

            Optional<Type> typeOpt = mergeTypes(Stream.concat(arrayLowerType.stream(), valueTypes));
            typeOpt.ifPresent(storeInsn::setType);
            return typeOpt.isPresent();
        }

        @Override
        public boolean hasSideEffects() {
            return true;
        }
    }

    @Override
    public Register convertRegister(Integer register) {
        return registers.get(register);
    }

    @Override
    public void registerReference(org.jf.dexlib2.iface.instruction.Instruction instruction, RWCell<Instruction> cell) {
        Collection<RWCell<Instruction>> cells = instructionRefs.computeIfAbsent(instruction, x -> new ArrayList<>());
        cells.add(cell);
    }
}
