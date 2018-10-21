package me.aki.tactical.core.textify;

import me.aki.tactical.core.Attribute;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MethodTextifier implements Textifier<Method> {
    private final Classfile classfile;
    private final BodyTextifier bodyTextifier;

    public MethodTextifier(BodyTextifier bodyTextifier, Classfile classfile) {
        this.bodyTextifier = bodyTextifier;
        this.classfile = classfile;
    }

    @Override
    public void textify(Printer printer, Method method) {
        appendSignature(printer, method.getSignature());
        appendAnnotations(printer, method.getAnnotations());
        appendTypeAnnotations(printer, method.getTypeAnnotations());
        appendAttributes(printer, method.getAttributes());
        appendParameterInfos(printer, method.getParameterInfo());
        appendParameterAnnotations(printer, method.getParameterAnnotations());

        String name = method.getName();
        if (name.equals("<clinit>")) {
            Set<Method.Flag> flags = new HashSet<>(method.getFlags());
            flags.remove(Method.Flag.STATIC);
            FlagTextifier.METHOD.textify(printer, flags);

            printer.addText ("static");
        } else {
            FlagTextifier.METHOD.textify(printer, method.getFlags());

            if (name.equals("<init>")) {
                printer.addLiteral(classfile.getName().getName());
            } else {
                method.getReturnType().ifPresentOrElse(
                        type -> TypeTextifier.getInstance().textify(printer, type),
                        () -> printer.addText("void"));
                printer.addText(" ");
                printer.addLiteral(name);
            }

            printer.addText("(");
            TextUtil.joined(method.getParameterTypes(),
                    type -> TypeTextifier.getInstance().textify(printer, type),
                    () -> printer.addText(", "));
            printer.addText(")");
        }

        List<Path> exceptions = method.getExceptions();
        if (!exceptions.isEmpty()) {
            printer.addText(" throws ");
            TextUtil.joined(exceptions,
                    printer::addPath,
                    () -> printer.addText(", "));
        }

        method.getDefaultValue().ifPresent(defaultValue -> {
            printer.addText(" = ");
            AnnotationValueTextifier.getInstance().textify(printer, defaultValue);
            printer.addText(";");
        });

        method.getBody().ifPresentOrElse(body -> {
            printer.addText(" {");
            printer.newLine();
            printer.increaseIndent();

            bodyTextifier.textify(printer, method);

            printer.decreaseIndent();
            printer.addText("}");
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
            TypeAnnotationTextifier.METHOD.textify(printer, typeAnnotation);
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
        for (Method.Parameter parameter : parameterInfos) {
            FlagTextifier.METHOD_PARAMETER.textify(printer, parameter.getFlags());
            printer.addText("parameter");
            parameter.getName().ifPresent(name -> {
                printer.addText(" ");
                printer.addEscaped(name, '"');
            });
            printer.addText(";");
            printer.newLine();
        }
    }

    private void appendParameterAnnotations(Printer printer, List<List<Annotation>> allParameterAnnotations) {
        for (List<Annotation> paramAnnotations : allParameterAnnotations) {
            printer.addText("parameter annotations ");

            if (paramAnnotations.isEmpty()) {
                printer.addText("{}");
            } else {
                printer.addText("{ ");
                TextUtil.joined(paramAnnotations,
                        annotation -> AnnotationTextifier.getInstance().textify(printer, annotation),
                        () -> printer.addText(", "));
                printer.addText(" }");
            }
            printer.addText(";");
            printer.newLine();
        }
    }
}
