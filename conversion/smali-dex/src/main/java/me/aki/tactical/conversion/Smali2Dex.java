package me.aki.tactical.conversion;

import me.aki.tactical.conversion.smali2dex.ClassFileConverter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.dex.DexFile;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade for conversions from <tt>*.dex</tt> files to their tactical representations backed by baksmali.
 */
public class Smali2Dex {
    /**
     * Convert a <tt>*.dex</tt> file into a tactical {@link DexFile}.
     *
     * @param file a <tt>*.dex</tt> file
     * @param opcodes api level of the provided <tt>*.dex</tt> file
     * @return the converted {@link DexFile}
     * @throws IOException the file could not be read
     */
    public static DexFile convertDex(File file, Opcodes opcodes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(file);
        byte[] cache = new byte[4096];

        int len;
        while ((len = in.read(cache)) > 0) {
            out.write(cache, 0, len);
        }

        return convertDex(out.toByteArray(), opcodes);
    }

    /**
     * Convert a <tt>*.dex</tt> file into a tactical {@link DexFile}.
     *
     * @param bytecode content of a <tt>*.dex</tt> file
     * @param opcodes api level of the provided <tt>*.dex</tt> file
     * @return the converted {@link DexFile}
     */
    public static DexFile convertDex(byte[] bytecode, Opcodes opcodes) {
        return convertDex(new DexBackedDexFile(opcodes, bytecode));
    }

    /**
     * Convert a smali {@link org.jf.dexlib2.iface.DexFile} into a tactical {@link DexFile}.
     *
     * @param dexFile smali {@link org.jf.dexlib2.iface.DexFile} to convert
     * @return a converted tactical {@link DexFile}
     */
    public static DexFile convertDex(org.jf.dexlib2.iface.DexFile dexFile) {
        List<Classfile> classes = dexFile.getClasses().stream()
                .map(Smali2Dex::convertClass)
                .collect(Collectors.toList());

        return new DexFile(classes);
    }

    /**
     * Convert a single smali {@link ClassDef} into a tactical {@link Classfile}.
     *
     * @param classDef smali classfile to be converted
     * @return converted tactical {@link Classfile}
     */
    public static Classfile convertClass(ClassDef classDef) {
        return new ClassFileConverter(classDef).getTacticalClass();
    }
}
