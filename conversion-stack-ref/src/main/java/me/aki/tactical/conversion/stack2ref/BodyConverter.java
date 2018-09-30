package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;

import java.util.stream.Collectors;

/**
 * Utility that builds a {@link RefBody} from a {@link StackBody}.
 */
public class BodyConverter {
    private final StackBody stackBody;
    private final RefBody refBody;
    private final ConversionContext ctx;

    public BodyConverter(StackBody stackBody) {
        this.stackBody = stackBody;
        this.refBody = new RefBody();
        this.ctx = new ConversionContext(stackBody);
    }

    public RefBody getRefBody() {
        return refBody;
    }

    public void convert() {
        convertLocals();
    }

    private void convertLocals() {
        for (StackLocal stackLocal : stackBody.getLocals()) {
            RefLocal refLocal = new RefLocal(null);
            refBody.getLocals().add(refLocal);
            ctx.getLocalMap().put(stackLocal, refLocal);
        }

        refBody.setThisLocal(stackBody.getThisLocal().map(ctx::getLocal));
        refBody.setArgumentLocals(stackBody.getParameterLocals().stream()
                .map(ctx::getLocal)
                .collect(Collectors.toList()));
    }
}
