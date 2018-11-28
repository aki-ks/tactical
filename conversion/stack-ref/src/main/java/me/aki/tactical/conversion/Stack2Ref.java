package me.aki.tactical.conversion;

import me.aki.tactical.conversion.stack2ref.BodyConverter;
import me.aki.tactical.conversion.stack2ref.postprocessor.DirectReuseInliningPostProcessor;
import me.aki.tactical.conversion.stack2ref.postprocessor.LocalPartitioningPostProcessor;
import me.aki.tactical.conversion.stack2ref.postprocessor.LocalTypingPostProcessor;
import me.aki.tactical.conversion.stack2ref.postprocessor.PostProcessor;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.stack.StackBody;

import java.util.Optional;

/**
 * Main entry point to the stack to ref conversion.
 */
public class Stack2Ref {
    private static PostProcessor[] POST_PROCESSORS = {
            new DirectReuseInliningPostProcessor(),
            new LocalPartitioningPostProcessor(),
            new LocalTypingPostProcessor()
    };

    /**
     * Take a class containing only methods with StackBodies and replace them against RefBodies.
     *
     * @param classfile classfile whose method bodies should get converted.
     */
    public static void convert(Classfile classfile) {
        for (Method method : classfile.getMethods()) {
            method.getBody().ifPresent(body -> {
                if (body instanceof StackBody) {
                    method.setBody(Optional.of(convert(classfile, method, (StackBody) body)));
                } else {
                    throw new IllegalArgumentException("Class contains a " + body.getClass().getSimpleName() + " expected only StackBodies");
                }
            });
        }
    }

    /**
     * Build a {@link RefBody} from a {@link StackBody}.
     *
     * @param classfile the classfile that contains the method and body
     * @param method the method contains the body
     * @param stackBody the stackbody to be converted
     * @return the converted ref body
     */
    public static RefBody convert(Classfile classfile, Method method, StackBody stackBody) {
        BodyConverter bodyConverter = new BodyConverter(classfile.getName(), method, stackBody);
        bodyConverter.convert();
        RefBody refBody = bodyConverter.getRefBody();

        for (PostProcessor postProcessor : POST_PROCESSORS) {
            postProcessor.process(refBody);
        }

        return refBody;
    }
}
