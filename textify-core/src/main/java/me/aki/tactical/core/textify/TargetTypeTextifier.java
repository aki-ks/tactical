package me.aki.tactical.core.textify;

import me.aki.tactical.core.typeannotation.TargetType;

public class TargetTypeTextifier {
    public final static Textifier<TargetType.Extends> EXTENDS = (printer, target)
            -> printer.addText("extends");

    public final static Textifier<TargetType.Implements> IMPLEMENTS = (printer, target) ->
            printer.addText("implements " + target.getIndex());

    private static final Textifier<TargetType.TypeParameter> TYPE_PARAMETER = (printer, target) ->
            printer.addText("type parameter " + target.getParameterIndex());

    private static final Textifier<TargetType.TypeParameterBound> TYPE_PARAMETER_BOUND = (printer, target) ->
            printer.addText("type parameter bound " + target.getParameterIndex() + " " + target.getBoundIndex());

    public final static Textifier<TargetType.ClassTargetType> CLASS_TARGET_TYPE = (printer, target) -> {
        if (target instanceof TargetType.Extends) {
            EXTENDS.textify(printer, (TargetType.Extends) target);
        } else if (target instanceof TargetType.Implements) {
            IMPLEMENTS.textify(printer, (TargetType.Implements) target);
        } else if (target instanceof TargetType.TypeParameter) {
            TYPE_PARAMETER.textify(printer, (TargetType.TypeParameter) target);
        } else if (target instanceof TargetType.TypeParameterBound) {
            TYPE_PARAMETER_BOUND.textify(printer, (TargetType.TypeParameterBound) target);
        } else {
            throw new AssertionError();
        }
    };
}
