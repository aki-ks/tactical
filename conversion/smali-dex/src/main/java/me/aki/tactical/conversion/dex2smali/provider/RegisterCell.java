package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

/**
 * A RWCell that should contain the register index of a tactical {@link Register}.
 */
public class RegisterCell extends RWCell.Heap<Integer> {
    private Register register;

    public RegisterCell(Register register) {
        super(Integer.class, 0);
        this.register = register;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }
}
