/*
 * Kotlin Coupling Between Objects (CBO) - initial PSI-based implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CBO;

/**
 * Counts unique external types referenced by a Kotlin class using PSI-only heuristics.
 * Sources scanned: supertypes, constructor/property types, function param/return/receiver types,
 * generic type arguments (via user types), and local variable type references.
 *
 * Limitations: Does not resolve type aliases or imports; treats built-in Kotlin types as non-coupling;
 * may include some false positives/negatives without resolve. Good enough for Phase 2 scaffolding.
 */
public class KotlinCouplingBetweenObjectsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getName();

        // Supertypes
        List<KtSuperTypeListEntry> superTypes = klass.getSuperTypeListEntries();
        for (KtSuperTypeListEntry st : superTypes) {
            addTypesFromTypeRef(types, st.getTypeReference(), selfName);
        }

        // Primary constructor property params
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    addTypesFromTypeRef(types, p.getTypeReference(), selfName);
                }
            }
        }

        // Class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    addTypesFromTypeRef(types, ((KtProperty) decl).getTypeReference(), selfName);
                } else if (decl instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) decl;
                    // receiver, params, return
                    addTypesFromTypeRef(types, f.getReceiverTypeReference(), selfName);
                    for (KtParameter p : f.getValueParameters()) {
                        addTypesFromTypeRef(types, p.getTypeReference(), selfName);
                    }
                    addTypesFromTypeRef(types, f.getTypeReference(), selfName);
                    // local variables and nested type refs inside body
                    KtBlockExpression bodyExpr = (f.getBodyBlockExpression());
                    if (bodyExpr != null) {
                        bodyExpr.accept(new KtTreeVisitorVoid() {
                            @Override
                            public void visitTypeReference(@NotNull KtTypeReference typeReference) {
                                addTypesFromTypeRef(types, typeReference, selfName);
                                super.visitTypeReference(typeReference);
                            }
                        });
                    }
                } else if (decl instanceof KtObjectDeclaration) {
                    // Nested objects: skip their internals for outer class coupling
                }
            }
        }

        metric = Metric.of(CBO, types.size());
    }

    private void addTypesFromTypeRef(Set<String> sink, KtTypeReference typeRef, String selfName) {
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
        // Basic Kotlin built-ins and common containers (approximate)
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
