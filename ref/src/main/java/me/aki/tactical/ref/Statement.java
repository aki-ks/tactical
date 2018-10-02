package me.aki.tactical.ref;

public interface Statement extends Referencing {
    /**
     * May the instruction followed by this one be ever executed?
     *
     * @return false if the succeeding instruction is unreachable
     */
    default boolean continuesExecution() {
        return true;
    }
}
