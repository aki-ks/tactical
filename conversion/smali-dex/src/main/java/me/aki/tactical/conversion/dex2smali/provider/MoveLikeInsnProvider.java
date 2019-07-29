package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction12x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction32x;

import java.util.List;
import java.util.Set;

public class MoveLikeInsnProvider implements InstructionProvider<TwoRegisterInstruction> {
    private final Opcode opcode;
    private final Opcode opcodeFrom16;
    private final Opcode opcode16;

    private final RegisterCell toRegister;
    private final RegisterCell fromRegister;

    public MoveLikeInsnProvider(Opcode opcode, Opcode opcodeFrom16, Opcode opcode16, Register toRegister, Register fromRegister) {
        this.opcode = opcode;
        this.opcodeFrom16 = opcodeFrom16;
        this.opcode16 = opcode16;

        this.toRegister = new RegisterCell(toRegister);
        this.fromRegister = new RegisterCell(fromRegister);
    }

    public RegisterCell getToRegisterCell() {
        return toRegister;
    }

    public RegisterCell getFromRegisterCell() {
        return fromRegister;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(toRegister, fromRegister);
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        int to = toRegister.get();
        int from = fromRegister.get();
        return is4BitRegister(to) && is4BitRegister(from) ? Format.Format12x :
                is8BitRegister(to) && is16BitRegister(from) ? Format.Format22x :
                is16BitRegister(to) && is16BitRegister(from) ? Format.Format32x :
                DexUtils.unreachable();
    }

    private boolean is4BitRegister(int to) {
        return (to & 0xFFFFFFF0) == 0;
    }

    private boolean is8BitRegister(int to) {
        return (to & 0xFFFFFF00) == 0;
    }

    private boolean is16BitRegister(int from) {
        return (from & 0xFFFF0000) == 0;
    }

    @Override
    public Set<Format> getPossibleFormats() {
        return Set.of(Format.Format12x, Format.Format22x, Format.Format32x);
    }

    @Override
    public TwoRegisterInstruction newInstance() {
        int to = toRegister.get();
        int from = fromRegister.get();
        switch (getFormat()) {
            case Format12x: return new ImmutableInstruction12x(opcode, to, from);
            case Format22x: return new ImmutableInstruction22x(opcodeFrom16, to, from);
            case Format32x: return new ImmutableInstruction32x(opcode16, to, from);
            default: return DexUtils.unreachable();
        }
    }

}
