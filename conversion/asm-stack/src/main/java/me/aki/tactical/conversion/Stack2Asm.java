package me.aki.tactical.conversion;

import me.aki.tactical.conversion.stack2asm.TacticalClassReader;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * A Facade for the conversions from {@link Classfile Classfiles} with {@link StackBody StackBodies}
 * to objectweb asm structures.
 */
public class Stack2Asm {
    /**
     * Make a {@link ClassVisitor} visit a {@link Classfile}.
     *
     * Note: Neither the {@link MethodVisitor#visitMaxs(int, int) stack size and local count} nor
     * {@link MethodVisitor#visitFrame(int, int, Object[], int, Object[]) the frames} are visited.
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
    public static ClassNode toClassNode(Classfile classfile) {
        ClassNode cn = new ClassNode();
        accept(classfile, cn);
        return cn;
    }

    /**
     * Convert a {@link Classfile} into a byte array of jvm bytecode.
     *
     * @param classfile the {@link Classfile} to convert
     * @return the converted jvm bytecode
     */
    public static byte[] toBytecode(Classfile classfile) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        accept(classfile, cw);
        return cw.toByteArray();
    }
}
