package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.core.Field;
import org.objectweb.asm.FieldVisitor;

public class TacticalFieldReader {
    private final Field field;

    public TacticalFieldReader(Field field) {
        this.field = field;
    }

    public void accept(FieldVisitor fv) {
        fv.visitEnd();
    }
}
