package me.aki.tactical.stack.insn;

import me.aki.tactical.core.Path;

/**
 * Create a new instance of a class.
 * The constructor must be called before the instance can be used.
 */
public class NewInsn implements Instruction {
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
}
