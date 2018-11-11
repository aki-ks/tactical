package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.AssignStmt;

/**
 * Compute and assign types to all locals.
 *
 * Since one JVM local may have multiple types at different locations in code,
 * the {@link LocalPartitioningPostProcessor} should be run first.
 * It will split such locals into multiple ones that may each have different types.
 */
public class LocalTypingPostProcessor implements PostProcessor {
    @Override
    public void process(RefBody body) {
        for (Statement statement : body.getStatements()) {
            if (statement instanceof AssignStmt) {
                AssignStmt assignment = (AssignStmt) statement;

                if (assignment.getVariable() instanceof RefLocal) {
                    RefLocal local = (RefLocal) assignment.getVariable();
                    Type requiredType = assignment.getValue().getType();
                    mergeType(local, requiredType);
                }
            }
        }
    }

    private void mergeType(RefLocal local, Type mergeType) {
        Type currentType = getSimpleType(local.getType());
        mergeType = getSimpleType(mergeType);

        if (currentType == null) {
            local.setType(mergeType);
        } else {
            if (mergeType instanceof PrimitiveType && currentType instanceof PrimitiveType) {
                if (mergeType.equals(currentType)) {
                    return;
                }
            }

            if (mergeType instanceof RefType && currentType instanceof RefType) {
                return;
            }

            throw new AssertionError("Cannot merge types " + mergeType + " and " + currentType);
        }
    }

    private Type getSimpleType(Type type) {
        return type instanceof RefType ? ObjectType.OBJECT :
                type instanceof IntLikeType ? IntType.getInstance() : type;
    }
}
