package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.DAC;

/**
 * Kotlin Data Abstraction Coupling (DAC) - counts the number of distinct
 * classes that are used as
 * attribute/property types of a Kotlin class.
 */
public class KotlinDataAbstractionCouplingVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        Set<String> types = new HashSet<>();
        String selfName = klass.getFqName() != null ? klass.getFqName().asString() : klass.getName();

        // Primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    addTypesFromTypeRef(types, p.getTypeReference(), selfName);
                }
            }
        }

        // Class body properties
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    addTypesFromTypeRef(types, ((KtProperty) decl).getTypeReference(), selfName);
                }
            }
        }

        metric = Metric.of(DAC, types.size());
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
