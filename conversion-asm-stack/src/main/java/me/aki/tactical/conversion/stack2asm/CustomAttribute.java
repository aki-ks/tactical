package me.aki.tactical.conversion.stack2asm;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

public class CustomAttribute extends Attribute {
    private final byte[] data;

    protected CustomAttribute(String name, byte[] data) {
        super(name);
        this.data = data;
    }

    @Override
    protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeAttributeOffset, Label[] labels) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
        ByteVector vector = new ByteVector(this.data.length);
        vector.putByteArray(this.data, 0, this.data.length);
        return vector;
    }
}
