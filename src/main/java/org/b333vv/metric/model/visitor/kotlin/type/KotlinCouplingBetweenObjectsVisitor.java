package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CBO;

/**
 * Counts unique external types referenced by a Kotlin class using PSI
 * resolution.
 * Sources scanned: supertypes, constructor/property types, function
 * param/return/receiver types,
 * generic type arguments (via user types), and local variable type references.
 *
 * Improvements: Resolves types to their fully qualified names to correctly
 * identify classes
 * and exclude packages or standard library types.
 */
public class KotlinCouplingBetweenObjectsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getFqName() != null ? klass.getFqName().asString() : klass.getName();

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
        if (typeRef == null)
            return;
        typeRef.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitUserType(@NotNull KtUserType type) {
                KtSimpleNameExpression refExpr = type.getReferenceExpression();
                if (refExpr != null) {
                    try {
                        PsiElement target = refExpr.getReference() != null ? refExpr.getReference().resolve() : null;
                        String qName = null;
                        if (target instanceof PsiClass) {
                            qName = ((PsiClass) target).getQualifiedName();
                        } else if (target instanceof KtClassOrObject) {
                            qName = ((KtClassOrObject) target).getFqName() != null
                                    ? ((KtClassOrObject) target).getFqName().asString()
                                    : null;
                        }

                        if (qName != null && !isStandardClass(qName) && (selfName == null || !qName.equals(selfName))) {
                            sink.add(qName);
                        }
                    } catch (Exception e) {
                        // Ignore resolution errors
                    }
                }
                super.visitUserType(type);
            }
        });
    }

    private boolean isStandardClass(String qName) {
        return qName.startsWith("java.") || qName.startsWith("kotlin.") || qName.startsWith("javax.");
    }
}
