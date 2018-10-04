package me.aki.tactical.core.textify;

import me.aki.tactical.core.annotation.AbstractAnnotation;
import me.aki.tactical.core.annotation.Annotation;

public class AnnotationTextifier implements Textifier<Annotation> {
    @Override
    public void textify(Printer printer, Annotation annotation) {
        printer.addText("@");
        printer.addPath(annotation.getType());
        printer.addText("[visible = ");
        printer.addText(annotation.isRuntimeVisible() ? "true" : "false");
        printer.addText("](");
        appendValues(printer, annotation);
        printer.addText(")");
    }

    public static void appendValues(Printer printer, AbstractAnnotation annotation) {
        TextUtil.joined(annotation.getValues().entrySet(),
                entry -> {
                    printer.addLiteral(entry.getKey());
                    printer.addText(" = ");
                    AnnotationValueTextifier.getInstance().textify(printer, entry.getValue());
                }, () -> printer.addText(", "));
    }
}
