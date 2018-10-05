package me.aki.tactical.core.textify;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.typeannotation.TypePath;

import java.util.List;

public class TypePathTextifier implements Textifier<TypePath> {
    private final static TypePathTextifier INSTANCE = new TypePathTextifier();

    public static TypePathTextifier getInstance() {
        return INSTANCE;
    }

    private TypePathTextifier() {}

    public final static Textifier<TypePath.Kind> KIND = (printer, kind) -> {
        if (kind instanceof TypePath.Kind.Array) {
            printer.addText("array");
        } else if (kind instanceof Classfile.InnerClass) {
            printer.addText("inner");
        } else if (kind instanceof TypePath.Kind.WildcardBound) {
            printer.addText("wildcard");
        } else if (kind instanceof TypePath.Kind.TypeArgument) {
            int argument = ((TypePath.Kind.TypeArgument) kind).getTypeArgumentIndex();
            printer.addText("<" + argument + ">");
        } else {
            throw new AssertionError();
        }
    };

    @Override
    public void textify(Printer printer, TypePath value) {
        List<TypePath.Kind> paths = value.getPaths();

        if (paths.isEmpty()) {
            printer.addText("{}");
        } else {
            printer.addText("{ ");
            TextUtil.joined(paths,
                    kind -> KIND.textify(printer, kind),
                    () -> printer.addText(", "));
            printer.addText(" }");
        }
    }
}
