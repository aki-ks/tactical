package me.aki.tactical.core.textify;

import me.aki.tactical.core.typeannotation.TargetType;

public class TargetTypeTextifier {
    public final static Textifier<TargetType.Extends> EXTENDS = (printer, target) ->
            printer.addText("extends");

    public final static Textifier<TargetType.Implements> IMPLEMENTS = (printer, target) ->
            printer.addText("implements " + target.getIndex());

    private static final Textifier<TargetType.TypeParameter> TYPE_PARAMETER = (printer, target) ->
            printer.addText("type parameter " + target.getParameterIndex());

    private static final Textifier<TargetType.TypeParameterBound> TYPE_PARAMETER_BOUND = (printer, target) ->
            printer.addText("type parameter bound " + target.getParameterIndex() + " " + target.getBoundIndex());

    private static final Textifier<TargetType.CheckedException> CHECKED_EXCEPTION = (printer, target) ->
            printer.addText("exception " + target.getException());

    private static final Textifier<TargetType.ReturnType> RETURN = (printer, target) ->
            printer.addText("return");

    private static final Textifier<TargetType.MethodReceiver> METHOD_RECEIVER = (printer, target) ->
            printer.addText("receiver");

    private static final Textifier<TargetType.MethodParameter> METHOD_PARAMETER = (printer, target) ->
            printer.addText("parameter " + target.getParameter());

    private static final Textifier<TargetType.LocalVariable> LOCAL_VARIABLE = (printer, target) ->
            printer.addText("local");

    private static final Textifier<TargetType.ResourceVariable> RESOURCE_VARIABLE = (printer, target) ->
            printer.addText("resource");

    private static final Textifier<TargetType.New> NEW = (printer, target) ->
            printer.addText("new");

    private static final Textifier<TargetType.Cast> CAST = (printer, target) ->
            printer.addText("cast " + target.getIntersection());

    private static final Textifier<TargetType.InstanceOf> INSTANCE_OF = (printer, target) ->
            printer.addText("instanceof");

    private static final Textifier<TargetType.ConstructorReference> CONSTRUCTOR_REFERENCE = (printer, target) ->
            printer.addText("constructor reference");

    private static final Textifier<TargetType.ConstructorInvokeTypeParameter> CONSTRUCTOR_INVOKE_TYPE_PARAMETER = (printer, target) ->
            printer.addText("constructor invoke type parameter");

    private static final Textifier<TargetType.ConstructorReferenceTypeParameter> CONSTRUCTOR_REFERENCE_TYPE_PARAMETER = (printer, target) ->
            printer.addText("constructor reference type parameter");

    private static final Textifier<TargetType.MethodReference> METHOD_REFERENCE = (printer, target) ->
            printer.addText("method reference");

    private static final Textifier<TargetType.MethodInvokeTypeParameter> METHOD_INVOKE_TYPE_PARAMETER = (printer, target) ->
            printer.addText("method invoke type parameter");

    private static final Textifier<TargetType.MethodReferenceTypeParameter> METHOD_REFERENCE_TYPE_PARAMETER = (printer, target) ->
            printer.addText("method reference type parameter");

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

    public static final Textifier<TargetType.MethodTargetType> METHOD_TARGET_TYPE = (printer, target) -> {
        if (target instanceof TargetType.CheckedException) {
            CHECKED_EXCEPTION.textify(printer, (TargetType.CheckedException) target);
        } else if (target instanceof TargetType.ReturnType) {
            RETURN.textify(printer, (TargetType.ReturnType) target);
        } else if (target instanceof TargetType.MethodReceiver) {
            METHOD_RECEIVER.textify(printer, (TargetType.MethodReceiver) target);
        } else if (target instanceof TargetType.MethodParameter) {
            METHOD_PARAMETER.textify(printer, (TargetType.MethodParameter) target);
        } else if (target instanceof TargetType.TypeParameter) {
            TYPE_PARAMETER.textify(printer, (TargetType.TypeParameter) target);
        } else if (target instanceof TargetType.TypeParameterBound) {
            TYPE_PARAMETER_BOUND.textify(printer, (TargetType.TypeParameterBound) target);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<TargetType.LocalTargetType> LOCAL_TARGET_TYPE = (printer, target) -> {
        if (target instanceof TargetType.LocalVariable) {
            LOCAL_VARIABLE.textify(printer, (TargetType.LocalVariable) target);
        } else if (target instanceof TargetType.ResourceVariable) {
            RESOURCE_VARIABLE.textify(printer, (TargetType.ResourceVariable) target);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<TargetType.InsnTargetType> INSN_TARGET_TYPE = (printer, target) -> {
        if (target instanceof TargetType.New) {
            NEW.textify(printer, (TargetType.New) target);
        } else if (target instanceof TargetType.Cast) {
            CAST.textify(printer, (TargetType.Cast) target);
        } else if (target instanceof TargetType.InstanceOf) {
            INSTANCE_OF.textify(printer, (TargetType.InstanceOf) target);
        } else if (target instanceof TargetType.ConstructorReference) {
            CONSTRUCTOR_REFERENCE.textify(printer, (TargetType.ConstructorReference) target);
        } else if (target instanceof TargetType.ConstructorInvokeTypeParameter) {
            CONSTRUCTOR_INVOKE_TYPE_PARAMETER.textify(printer, (TargetType.ConstructorInvokeTypeParameter) target);
        } else if (target instanceof TargetType.ConstructorReferenceTypeParameter) {
            CONSTRUCTOR_REFERENCE_TYPE_PARAMETER.textify(printer, (TargetType.ConstructorReferenceTypeParameter) target);
        } else if (target instanceof TargetType.MethodReference) {
            METHOD_REFERENCE.textify(printer, (TargetType.MethodReference) target);
        } else if (target instanceof TargetType.MethodInvokeTypeParameter) {
            METHOD_INVOKE_TYPE_PARAMETER.textify(printer, (TargetType.MethodInvokeTypeParameter) target);
        } else if (target instanceof TargetType.MethodReferenceTypeParameter) {
            METHOD_REFERENCE_TYPE_PARAMETER.textify(printer, (TargetType.MethodReferenceTypeParameter) target);
        } else {
            throw new AssertionError();
        }
    };
}
