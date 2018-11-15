package me.aki.tactical.conversion;

import me.aki.tactical.conversion.asm2stack.ClassConvertVisitor;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * The entry point for the asm to stack conversion.
 */
public class Asm2Stack {
    /**
     * Build a Classfile with {@link StackBody Stackbodys} from an asm {@link ClassReader}.
     *
     * @param cr the class reader
     * @param parsingOption the parsingOption flag that is passed to the ClassReader.
     *                 {@link ClassReader#SKIP_CODE}, {@link ClassReader#SKIP_DEBUG},
     *                 {@link ClassReader#SKIP_FRAMES} or {@link ClassReader#EXPAND_FRAMES
     * @return the converted class
     */
    public static Classfile convert(ClassReader cr, int parsingOption) {
        ClassConvertVisitor converter = new ClassConvertVisitor();
        cr.accept(converter, parsingOption);
        return converter.getClassfile();
    }

    /**
     * Build a Classfile with {@link StackBody Stackbodys} from an asm {@link ClassReader}.
     *
     * @param cr the class reader
     * @return the converted class
     */
    public static Classfile convert(ClassReader cr) {
        ClassConvertVisitor converter = new ClassConvertVisitor();
        cr.accept(converter, 0);
        return converter.getClassfile();
    }

    /**
     * Build a {@link Classfile} with {@link StackBody Stackbodys} from an asm {@link ClassNode}.
     *
     * @param cn the class node
     * @return the converted class
     */
    public static Classfile convert(ClassNode cn) {
        ClassConvertVisitor converter = new ClassConvertVisitor();
        cn.accept(converter);
        return converter.getClassfile();
    }
}
