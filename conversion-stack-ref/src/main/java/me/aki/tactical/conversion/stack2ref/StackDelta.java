package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.Expression;
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
            // Two stack frames should be merged that do not have equal stack heights.
            // This would not be able to pass the classfile verifier.
            throw new IllegalArgumentException("Illegal bytecode");
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
        Expression exprA = valueA.getValue();
        Expression exprB = valueB.getValue();

        if (exprA != exprB) {
            RefLocal local =
                    exprA instanceof RefLocal ? (RefLocal) exprA :
                    exprB instanceof RefLocal ? (RefLocal) exprB :
                        converter.newLocal();

            if (local != exprA) {
                valueA.storeInLocal(converter, local);
            }
            if (local != exprB) {
                valueB.storeInLocal(converter, local);
            }
        }
    }
}
