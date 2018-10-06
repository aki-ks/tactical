package me.aki.tactical.core.textify;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.Method;

public abstract class AbstractBodyTextifier implements Textifier<Body> {
    public void textifyParameterList(Printer printer, Method method) {
        TextUtil.joined(method.getParameterTypes(),
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText(", "));
    }
}
