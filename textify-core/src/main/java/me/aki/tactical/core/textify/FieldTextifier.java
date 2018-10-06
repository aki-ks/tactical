package me.aki.tactical.core.textify;

import me.aki.tactical.core.Field;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.FieldTypeAnnotation;

import java.util.List;
import java.util.Optional;

public class FieldTextifier implements Textifier<Field> {
    private static final FieldTextifier INSTANCE = new FieldTextifier();

    public static FieldTextifier getInstance() {
        return INSTANCE;
    }

    private FieldTextifier() {}

    @Override
    public void textify(Printer printer, Field field) {
        appendAnnotations(printer, field.getAnnotations());
        appendTypeAnnotations(printer, field.getTypeAnnotations());
        appendSignature(printer, field.getSignature());

        FlagTextifier.FIELD.textify(printer, field.getAccessFlags());
        TypeTextifier.getInstance().textify(printer, field.getType());
        printer.addText(" ");
        printer.addLiteral(field.getName());

        field.getValue().ifPresent(constant -> {
            printer.addText(" = ");
            ConstantTextifier.FIELD.textify(printer, constant);
        });

        printer.addText(";");
    }

    private void appendAnnotations(Printer printer, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            AnnotationTextifier.getInstance().textify(printer, annotation);
            printer.newLine();
        }
    }

    private void appendTypeAnnotations(Printer printer, List<FieldTypeAnnotation> typeAnnotations) {
        for (FieldTypeAnnotation typeAnnotation : typeAnnotations) {
            printer.addText("#[path = ");
            TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
            printer.addText(", annotation = ");
            AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
            printer.addText("]");
            printer.newLine();
        }
    }

    private void appendSignature(Printer printer, Optional<String> signatureOpt) {
        signatureOpt.ifPresent(signature -> {
            printer.addText("signature ");
            printer.addEscaped(signature, '"');
            printer.addText(";");
            printer.newLine();
        });
    }
}
