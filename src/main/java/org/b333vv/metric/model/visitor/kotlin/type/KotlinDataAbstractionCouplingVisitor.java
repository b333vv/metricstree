package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.DAC;

/**
 * Kotlin Data Abstraction Coupling (DAC) - counts the number of distinct classes that are used as
 * attribute/property types of a Kotlin class.
 *
 * Heuristics:
 * - Considers primary constructor parameters declared with val/var and properties in the class body.
 * - Extracts user type simple names from type references; skips Kotlin built-ins and the class itself.
 */
public class KotlinDataAbstractionCouplingVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getName();

        // Primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) addFromTypeRef(types, p.getTypeReference(), selfName);
            }
        }

        // Class body properties
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    addFromTypeRef(types, ((KtProperty) decl).getTypeReference(), selfName);
                }
            }
        }

        metric = Metric.of(DAC, types.size());
    }

    private void addFromTypeRef(Set<String> sink, KtTypeReference typeRef, String selfName) {
        if (typeRef == null) return;
        typeRef.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitUserType(@NotNull KtUserType type) {
                String name = type.getReferencedName();
                if (name != null && !isBuiltin(name) && (selfName == null || !name.equals(selfName))) {
                    sink.add(name);
                }
                super.visitUserType(type);
            }
        });
    }

    private boolean isBuiltin(String name) {
        switch (name) {
            case "Boolean":
            case "Byte":
            case "Short":
            case "Int":
            case "Long":
            case "Float":
            case "Double":
            case "Char":
            case "String":
            case "Unit":
            case "Any":
            case "Nothing":
            case "Array":
            case "List":
            case "MutableList":
            case "Set":
            case "MutableSet":
            case "Map":
            case "MutableMap":
            case "Pair":
            case "Triple":
                return true;
            default:
                return false;
        }
    }
}
