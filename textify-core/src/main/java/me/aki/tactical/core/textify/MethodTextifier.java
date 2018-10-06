package me.aki.tactical.core.textify;

import me.aki.tactical.core.Attribute;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;

import java.util.List;
import java.util.Optional;

public class MethodTextifier implements Textifier<Method> {
    private final AbstractBodyTextifier bodyTextifier;

    public MethodTextifier(AbstractBodyTextifier bodyTextifier) {
        this.bodyTextifier = bodyTextifier;
    }

    @Override
    public void textify(Printer printer, Method method) {
        appendSignature(printer, method.getSignature());
        appendAnnotations(printer, method.getAnnotations());
        appendTypeAnnotations(printer, method.getTypeAnnotations());
        appendAttributes(printer, method.getAttributes());
        appendParameterInfos(printer, method.getParameterInfo());
        appendParameterAnnotations(printer, method.getParameterAnnotations());

        FlagTextifier.METHOD.textify(printer, method.getAccessFlags());
        method.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
        printer.addText(" ");
        printer.addLiteral(method.getName());
        printer.addText("(");
        bodyTextifier.textifyParameterList(printer, method);
        printer.addText(")");

        List<Path> exceptions = method.getExceptions();
        if (!exceptions.isEmpty()) {
            printer.addText(" throws ");
            exceptions.forEach(printer::addPath);
        }

        method.getDefaultValue().ifPresent(defaultValue -> {
            printer.addText(" = ");
            AnnotationValueTextifier.getInstance().textify(printer, defaultValue);
        });

        method.getBody().ifPresentOrElse(body -> {
            printer.addText(" {");
            printer.newLine();
            printer.increaseIndent();

            bodyTextifier.textify(printer, body);

            printer.decreaseIndent();
            printer.addText("}");
            printer.newLine();
        }, () -> {
            printer.addText(";");
        });
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

    private void appendTypeAnnotations(Printer printer, List<MethodTypeAnnotation> typeAnnotations) {
        for (MethodTypeAnnotation typeAnnotation : typeAnnotations) {
            printer.addText("#[path = ");
            TypePathTextifier.getInstance().textify(printer, typeAnnotation.getTypePath());
            printer.addText(", target = ");
            TargetTypeTextifier.METHOD_TARGET_TYPE.textify(printer, typeAnnotation.getTargetType());
            printer.addText(", annotation = ");
            AnnotationTextifier.getInstance().textify(printer, typeAnnotation.getAnnotation());
            printer.addText("]");
            printer.newLine();
        }
    }

    private void appendAttributes(Printer printer, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            AttributeTextifier.getInstance().textify(printer, attribute);
            printer.newLine();
        }
    }

    private void appendParameterInfos(Printer printer, List<Method.Parameter> parameterInfos) {
        if (!parameterInfos.isEmpty()) {
            printer.addText("parameters { ");

            TextUtil.joined(parameterInfos,
                parameter -> {
                    FlagTextifier.METHOD_PARAMETER.textify(printer, parameter.getFlags());
                    parameter.getName().ifPresent(name -> printer.addEscaped(name, '"'));
                },
                () -> printer.addText(", "));

            printer.addText(" }");
        }
    }

    private void appendParameterAnnotations(Printer printer, List<List<Annotation>> allParameterAnnotations) {
        if (!allParameterAnnotations.isEmpty()) {
            printer.addText("parameter annotations { ");
            TextUtil.joined(allParameterAnnotations,
                    paramList -> appendAnnotationList(printer, paramList),
                    () -> printer.addText(", "));
            printer.addText(" }");
        }
    }

    private void appendAnnotationList(Printer printer, List<Annotation> parameterList) {
        if (parameterList.isEmpty()) {
            printer.addText("{}");
        } else {
            printer.addText("{ ");
            TextUtil.joined(parameterList,
                    annotation -> AnnotationTextifier.getInstance().textify(printer, annotation),
                    () -> printer.addText(", "));
            printer.addText(" }");
        }
    }
}
