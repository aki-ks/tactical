package me.aki.tactical.conversion;

import me.aki.tactical.conversion.stack2asm.TacticalClassReader;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * A Facade for the conversions from {@link Classfile Classfiles} with {@link StackBody StackBodies}
 * to objectweb asm structures.
 */
public class Stack2Asm {
    /**
     * Make a {@link ClassVisitor} visit a {@link Classfile}.
     *
     * @param file the file that the {@link ClassVisitor} will visit.
     * @param cv the ClassVisitor that will visit the class
     */
    public static void accept(Classfile file, ClassVisitor cv) {
        new TacticalClassReader(file).accept(cv);
    }

    /**
     * Convert a {@link Classfile} into an asm {@link ClassNode}.
     *
     * @param classfile the {@link Classfile} to convert
     * @return the converted ASM {@link ClassNode}.
     */
    public static ClassNode convert(Classfile classfile) {
        ClassNode cn = new ClassNode();
        accept(classfile, cn);
        return cn;
    }
}
