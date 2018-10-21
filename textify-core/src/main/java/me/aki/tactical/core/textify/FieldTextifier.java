package me.aki.tactical.core.textify;

import me.aki.tactical.core.Attribute;
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
        appendSignature(printer, field.getSignature());
        appendAnnotations(printer, field.getAnnotations());
        appendTypeAnnotations(printer, field.getTypeAnnotations());
        appendAttributes(printer, field.getAttributes());

        FlagTextifier.FIELD.textify(printer, field.getFlags());
        TypeTextifier.getInstance().textify(printer, field.getType());
        printer.addText(" ");
        printer.addLiteral(field.getName());

        field.getValue().ifPresent(constant -> {
            printer.addText(" = ");
            ConstantTextifier.FIELD.textify(printer, constant);
        });

        printer.addText(";");
    }

    private void appendSignature(Printer printer, Optional<String> signatureOpt) {
        signatureOpt.ifPresent(signature -> {
            printer.addText("signature ");
            printer.addEscaped(signature, '"');
            printer.addText(";");
            printer.newLine();
        });
    }

    private void appendAnnotations(Printer printer, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            AnnotationTextifier.getInstance().textify(printer, annotation);
            printer.newLine();
        }
    }

    private void appendTypeAnnotations(Printer printer, List<FieldTypeAnnotation> typeAnnotations) {
        for (FieldTypeAnnotation typeAnnotation : typeAnnotations) {
            TypeAnnotationTextifier.FIELD.textify(printer, typeAnnotation);
            printer.newLine();
        }
    }

    private void appendAttributes(Printer printer, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            AttributeTextifier.getInstance().textify(printer, attribute);
        }
    }
}
