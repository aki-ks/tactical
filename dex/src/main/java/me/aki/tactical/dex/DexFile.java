package me.aki.tactical.dex;

import me.aki.tactical.core.Classfile;

import java.util.List;

/**
 * A <tt>.dex</tt> file which is actually a list of classes with {@link DexBody}.
 */
public class DexFile {
    /**
     * The classes contained within the dex file
     */
    private List<Classfile> classes;

    public DexFile(List<Classfile> classes) {
        this.classes = classes;
    }

    public List<Classfile> getClasses() {
        return classes;
    }

    public void setClasses(List<Classfile> classes) {
        this.classes = classes;
    }
}
