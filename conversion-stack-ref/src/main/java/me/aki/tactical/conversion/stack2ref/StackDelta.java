package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.RefLocal;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Stores the changes that an instruction applies to the stack.
 */
public class StackDelta {
    /**
     * Values popped by the instruction.
     */
    private List<StackValue> pops;

    /**
     * Value pushed by the instruction.
     */
    private Optional<StackValue> push;

    public StackDelta(List<StackValue> pops, Optional<StackValue> push) {
        this.pops = pops;
        this.push = push;
    }

    public Optional<StackValue> getPush() {
        return push;
    }

    public void merge(BodyConverter converter, List<StackValue> pops) {
        if (pops.size() != this.pops.size()) {
            throw new IllegalArgumentException();
        }

        Iterator<StackValue> thisPopIter = this.pops.iterator();
        Iterator<StackValue> thatPopIter = pops.iterator();
        while (thisPopIter.hasNext()) {
            StackValue thisPop = thisPopIter.next();
            StackValue thatPop = thatPopIter.next();

            merge(converter, thisPop, thatPop);
        }
    }

    private void merge(BodyConverter converter, StackValue valueA, StackValue valueB) {
        RefLocal local;
        if (valueA.getValue() instanceof RefLocal) {
            local = (RefLocal) valueA.getValue();
        } else if (valueB.getValue() instanceof RefLocal) {
            local = (RefLocal) valueB.getValue();
        } else {
            local = converter.newLocal();
        }

        valueA.storeInLocal(converter, local);
        valueB.storeInLocal(converter, local);
    }
}
