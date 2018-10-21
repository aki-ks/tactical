package me.aki.tactical.core.textify;

import me.aki.tactical.core.Attribute;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Module;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ClassTextifier implements Textifier<Classfile> {
    private final BodyTextifier bodyTextifier;

    public ClassTextifier(BodyTextifier bodyTextifier) {
        this.bodyTextifier = bodyTextifier;
    }

    @Override
    public void textify(Printer printer, Classfile classfile) {
        appendPackage(printer, classfile.getName());
        appendVersion(printer, classfile.getVersion());
        appendSignature(printer, classfile.getSignature());
        appendSource(printer, classfile.getSource());
        appendDebug(printer, classfile.getSourceDebug());

        appendAnnotations(printer, classfile.getAnnotations());
        appendTypeAnnotations(printer, classfile.getTypeAnnotations());
        appendAttributes(printer, classfile.getAttributes());

        appendDescriptor(printer, classfile);
        printer.newLine();
        printer.increaseIndent();

        appendModuleContent(printer, classfile.getModule());

        appendEnclosingMethod(printer, classfile.getEnclosingMethod());
        appendInnerClass(printer, classfile.getInnerClasses());

        appendNests(printer, classfile.getNestHost(), classfile.getNestMembers());

        appendFields(printer, classfile.getFields());
        appendMethods(printer, classfile);

        printer.decreaseIndent();
        printer.addText("}");
        printer.newLine();
    }

    private void appendSignature(Printer printer, Optional<String> signatureOpt) {
        signatureOpt.ifPresent(signature -> {
            printer.addText("signature ");
            printer.addEscaped(signature, '"');
            printer.addText(";");
            printer.newLine();
        });
    }

    private void appendSource(Printer printer, Optional<String> sourceOpt) {
        sourceOpt.ifPresent(source -> {
            printer.addText("source ");
            printer.addEscaped(source, '"');
            printer.addText(";");
            printer.newLine();
        });
    }

    private void appendDebug(Printer printer, Optional<String> debugOpt) {
        debugOpt.ifPresent(debug -> {
            printer.addText("debug ");
            printer.addEscaped(debug, '"');
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

    private void appendTypeAnnotations(Printer printer, List<ClassTypeAnnotation> typeAnnotations) {
        for (ClassTypeAnnotation typeAnnotation : typeAnnotations) {
            TypeAnnotationTextifier.CLASS.textify(printer, typeAnnotation);
            printer.newLine();
        }
    }

    private void appendAttributes(Printer printer, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            AttributeTextifier.getInstance().textify(printer, attribute);
            printer.newLine();
        }
    }

    private void appendPackage(Printer printer, Path path) {
        List<String> pkg = path.getPackage();
        if (!pkg.isEmpty()) {
            printer.addText("package ");
            TextUtil.joined(pkg,
                    printer::addLiteral, () -> printer.addText("."));
            printer.addText(";");
            printer.newLine();
        }
    }

    private void appendVersion(Printer printer, Classfile.Version version) {
        printer.addText("version ");
        printer.addText(Integer.toString(version.getMajor()));
        printer.addText(".");
        printer.addText(Integer.toString(version.getMinor()));
        printer.addText(";");
        printer.newLine();
    }

    private void appendDescriptor(Printer printer, Classfile classfile) {
        FlagTextifier.CLASS.textify(printer, classfile.getFlags());
        appendClassKeyword(printer, classfile.getFlags());
        printer.addText(" ");
        printer.addLiteral(classfile.getName().getName());

        Path supertype = classfile.getSupertype();
        if (supertype != null) {
            printer.addText(" extends ");
            printer.addPath(supertype);
        }

        List<Path> interfaces = classfile.getInterfaces();
        if (!interfaces.isEmpty()) {
            printer.addText(" implements");
            TextUtil.joined(interfaces, printer::addPath, () -> printer.addText(", "));
        }

        printer.addText(" {");
        printer.newLine();
    }

    private void appendClassKeyword(Printer printer, Set<Classfile.Flag> accessFlags) {
        printer.addText(
                accessFlags.contains(Classfile.Flag.ANNOTATION) ? "@interface" :
                accessFlags.contains(Classfile.Flag.INTERFACE) ? "interface" :
                accessFlags.contains(Classfile.Flag.ENUM) ? "enum" :
                accessFlags.contains(Classfile.Flag.MODULE) ? "module" : "class"
        );
    }

    private void appendModuleContent(Printer printer, Optional<Module> moduleOpt) {
        moduleOpt.ifPresent(module -> {
            FlagTextifier.MODULE.textify(printer, module.getAccessFlags());
            printer.addText("module ");
            printer.addPath(module.getModule());
            printer.addText(" {");
            printer.newLine();
            printer.increaseIndent();

            module.getVersion().ifPresent(version -> {
                printer.addText("version ");
                printer.addEscaped(version, '"');
                printer.addText(";");
                printer.newLine();
            });

            module.getMainClass().ifPresent(main -> {
                printer.addText("main ");
                printer.addPath(main);
                printer.addText(";");
                printer.newLine();
            });

            for (Path pkg : module.getPackages()) {
                printer.addText("package ");
                printer.addPath(pkg);
                printer.addText(";");
                printer.newLine();
            }

            for (Module.Require requires : module.getRequires()) {
                FlagTextifier.MODULE_REQUIRES.textify(printer, requires.getFlags());
                printer.addText("requires ");
                printer.addPath(requires.getName());
                requires.getVersion().ifPresent(version -> {
                    printer.addText(" : ");
                    printer.addEscaped(version, '"');
                });
                printer.addText(";");
                printer.newLine();
            }

            for (Module.Export exports : module.getExports()) {
                FlagTextifier.MODULE_EXPORTS.textify(printer, exports.getFlags());
                printer.addText("exports ");
                printer.addPath(exports.getName());

                List<Path> modules = exports.getModules();
                if (!modules.isEmpty()) {
                    printer.addText(" to ");
                    TextUtil.joined(modules, printer::addPath, () -> printer.addText(", "));
                }

                printer.addText(";");
                printer.newLine();
            }

            for (Module.Open opens : module.getOpens()) {
                FlagTextifier.MODULE_OPENS.textify(printer, opens.getFlags());
                printer.addText("opens ");
                printer.addPath(opens.getName());

                List<Path> modules = opens.getModules();
                if (!modules.isEmpty()) {
                    printer.addText(" to ");
                    TextUtil.joined(modules, printer::addPath, () -> printer.addText(", "));
                }

                printer.addText(";");
                printer.newLine();
            }

            for (Path use : module.getUses()) {
                printer.addText("uses ");
                printer.addPath(use);
                printer.addText(";");
                printer.newLine();
            }

            for (Module.Provide provide : module.getProvides()) {
                printer.addText("provides ");
                printer.addPath(provide.getService());

                List<Path> providers = provide.getProviders();
                if (!providers.isEmpty()) {
                    printer.addText(" with ");
                    providers.forEach(printer::addPath);
                }

                printer.addText(";");
                printer.newLine();
            }

            printer.decreaseIndent();
            printer.addText("}");
            printer.newLine();
        });
    }

    private void appendEnclosingMethod(Printer printer, Optional<Classfile.EnclosingMethod> enclosingMethodOpt) {
        enclosingMethodOpt.ifPresent(enclosingMethod -> {
            printer.addText("enclosing ");
            printer.addPath(enclosingMethod.getOwner());

            if (!enclosingMethod.getDescriptor().isPresent() && !enclosingMethod.getName().isPresent()) {
                printer.addText(" {}");
                printer.newLine();
            } else {
                printer.addText(" {");
                printer.newLine();
                printer.increaseIndent();

                enclosingMethod.getName().ifPresent(name -> {
                    printer.addText("name = ");
                    printer.addEscaped(name, '"');
                    printer.addText(";");
                    printer.newLine();
                });

                enclosingMethod.getDescriptor().ifPresent(descriptor -> {
                    printer.addText("descriptor = ");
                    MethodDescriptorTextifier.getInstance().textify(printer, descriptor);
                    printer.addText(";");
                    printer.newLine();
                });

                printer.decreaseIndent();
                printer.newLine();
            }
        });
    }

    private void appendInnerClass(Printer printer, List<Classfile.InnerClass> innerClasses) {
        for (Classfile.InnerClass innerClass : innerClasses) {
            FlagTextifier.INNER_CLASS.textify(printer, innerClass.getFlags());
            printer.addText("inner ");
            printer.addPath(innerClass.getName());

            if (!innerClass.getInnerName().isPresent() && !innerClass.getOuterName().isPresent()) {
                printer.addText(" {}");
                printer.newLine();
            } else {
                printer.addText(" {");
                printer.newLine();
                printer.increaseIndent();

                innerClass.getInnerName().ifPresent(innerName -> {
                    printer.addText("inner ");
                    printer.addEscaped(innerName, '"');
                    printer.addText(";");
                });

                innerClass.getOuterName().ifPresent(outerName -> {
                    printer.addText("outer ");
                    printer.addPath(outerName);
                    printer.addText(";");
                });

                printer.decreaseIndent();
                printer.addText("}");
                printer.newLine();
            }
        }
    }

    private void appendNests(Printer printer, Optional<Path> nestHost, List<Path> nestMembers) {
        nestHost.ifPresent(host -> {
            printer.addText("nest host ");
            printer.addPath(host);
            printer.addText(";");
            printer.newLine();
        });

        if (!nestMembers.isEmpty()) {
            printer.addText("nest member ");
            TextUtil.joined(nestMembers,
                    printer::addPath,
                    () -> printer.addText(", "));
            printer.addText(";");
            printer.newLine();
        }
    }

    private void appendFields(Printer printer, List<Field> fields) {
        for (Field field : fields) {
            FieldTextifier.getInstance().textify(printer, field);
            printer.newLine();
            printer.newLine();
        }
    }

    private void appendMethods(Printer printer, Classfile classfile) {
        MethodTextifier methodTextifier = new MethodTextifier(bodyTextifier, classfile);
        for (Method method : classfile.getMethods()) {
            methodTextifier.textify(printer, method);
            printer.newLine();
            printer.newLine();
        }
    }
}
