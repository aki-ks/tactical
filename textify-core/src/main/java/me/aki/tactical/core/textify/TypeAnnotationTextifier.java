package me.aki.tactical.core.textify;

import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;
import me.aki.tactical.core.typeannotation.FieldTypeAnnotation;
import me.aki.tactical.core.typeannotation.InsnTypeAnnotation;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;

public class TypeAnnotationTextifier {
    public static final Textifier<ClassTypeAnnotation> CLASS = (printer, typeAnnotation) -> {
        printer.addText("#[path = ");
        TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
        printer.addText(", target = ");
        TargetTypeTextifier.CLASS_TARGET_TYPE.textify(printer, typeAnnotation.getTargetType());
        printer.addText(", annotation = ");
        AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
        printer.addText("]");
    };

    public static final Textifier<FieldTypeAnnotation> FIELD = (printer, typeAnnotation) -> {
        printer.addText("#[path = ");
        TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
        printer.addText(", annotation = ");
        AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
        printer.addText("]");
    };

    public static final Textifier<MethodTypeAnnotation> METHOD = (printer, typeAnnotation) -> {
        printer.addText("#[path = ");
        TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
        printer.addText(", target = ");
        TargetTypeTextifier.METHOD_TARGET_TYPE.textify(printer, typeAnnotation.getTargetType());
        printer.addText(", annotation = ");
        AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
        printer.addText("]");
    };

    public static final Textifier<InsnTypeAnnotation> INSN = (printer, typeAnnotation) -> {
        printer.addText(" #[path = ");
        TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
        printer.addText(", target = ");
        TargetTypeTextifier.INSN_TARGET_TYPE.textify(printer, typeAnnotation.getTargetType());
        printer.addText(", annotation = ");
        AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
        printer.addText("]");
    };

    public static final Textifier<LocalVariableTypeAnnotation> LOCAL = (printer, typeAnnotation) -> {
        printer.addText("#[path = ");
        TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
        printer.addText(", target = ");
        TargetTypeTextifier.LOCAL_TARGET_TYPE.textify(printer, typeAnnotation.getTargetType());
        printer.addText(", annotation = ");
        AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
        printer.addText("]");
    };
}
