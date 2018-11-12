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

    /**
     * Assign a type to local or merge it with the current type of the local.
     *
     * @param local the local whose type gets modified
     * @param mergeType the type that the local should be able to store
     */
    private void mergeType(RefLocal local, Type mergeType) {
        Type currentType = local.getType();
        if (currentType == null) {
            local.setType(mergeType);
            return;
        }

        if (mergeType.equals(currentType)) {
            return;
        }

        if (mergeType instanceof PrimitiveType || currentType instanceof PrimitiveType) {
            if (mergeType instanceof IntLikeType && currentType instanceof IntLikeType) {
                // If we merge e.g. 'byte' and 'short', we will just set the type to int.
                local.setType(IntType.getInstance());
                return;
            }

            throw new AssertionError("Cannot merge types " + mergeType + " and " + currentType);
        }

        // We have two different RefTypes, so we just set the type to 'java.lang.Object'.
        // Computing the 'greatest' common supertype would require that we have all their superclasses e.g. on the classpath.
        local.setType(ObjectType.OBJECT);
    }
}
