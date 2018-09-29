package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.ref.Expression;

import java.util.List;

public class InvokeVirtual extends AbstractInstanceInvoke {
    public InvokeVirtual(MethodRef method, Expression instance, List<Expression> arguments) {
        super(method, instance, arguments);
    }
}
