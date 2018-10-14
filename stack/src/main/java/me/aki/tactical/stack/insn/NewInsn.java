package me.aki.tactical.stack.insn;

import me.aki.tactical.core.Path;

import java.util.Objects;

/**
 * Create a new instance of a class.
 * The constructor must be called before the instance can be used.
 */
public class NewInsn extends AbstractInstruction {
    /**
     * Class whose instance gets created.
     */
    private Path path;

    public NewInsn(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NewInsn newInsn = (NewInsn) o;
        return Objects.equals(path, newInsn.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
