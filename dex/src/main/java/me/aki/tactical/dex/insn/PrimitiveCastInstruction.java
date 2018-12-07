package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.BooleanType;
import me.aki.tactical.core.type.ByteType;
import me.aki.tactical.core.type.CharType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Cast a value from one primitive type to another.
 */
public class PrimitiveCastInstruction implements Instruction {
    /**
     * Type of the value before the cast.
     */
    private PrimitiveType fromType;

    /**
     * Cast the value to this type.
     */
    private PrimitiveType toType;

    /**
     * Register containing the not yet casted value.
     */
    private Register fromRegister;

    /**
     * Store the casted value in that register.
     */
    private Register toRegister;

    public PrimitiveCastInstruction(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
        setFromType(fromType);
        setToType(toType);
        this.fromRegister = fromRegister;
        this.toRegister = toRegister;
    }

    public PrimitiveType getFromType() {
        return fromType;
    }

    public void setFromType(PrimitiveType fromType) {
        if (fromType instanceof BooleanType || fromType instanceof ByteType ||
                fromType instanceof CharType || fromType instanceof ShortType) {
            throw new IllegalArgumentException("Casts from " + fromType.getClass().getSimpleName() + " are not possible");
        }

        this.fromType = fromType;
    }

    public PrimitiveType getToType() {
        return toType;
    }

    public void setToType(PrimitiveType toType) {
        if (toType instanceof BooleanType) {
            throw new IllegalArgumentException("Casts to BooleanType are not possible");
        }

        this.toType = toType;
    }

    public Register getFromRegister() {
        return fromRegister;
    }

    public void setFromRegister(Register fromRegister) {
        this.fromRegister = fromRegister;
    }

    public Register getToRegister() {
        return toRegister;
    }

    public void setToRegister(Register toRegister) {
        this.toRegister = toRegister;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(fromRegister);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(toRegister);
    }
}
