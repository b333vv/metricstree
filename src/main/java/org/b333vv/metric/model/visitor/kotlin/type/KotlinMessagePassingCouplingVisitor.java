package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.MPC;

/**
 * Kotlin Message Passing Coupling (MPC) - counts method call expressions inside the class body.
 * Includes calls in function bodies and initializers. Does not attempt resolution.
 */
public class KotlinMessagePassingCouplingVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        final int[] calls = {0};
        String selfName = klass.getFqName() != null ? klass.getFqName().asString() : klass.getName();
        KtClassBody body = klass.getBody();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    if (isExternalCall(expression, klass, selfName)) {
                        calls[0]++;
                    }
                    super.visitCallExpression(expression);
                }
            });
        }
        metric = Metric.of(MPC, calls[0]);
    }

    private boolean isExternalCall(KtCallExpression expression, KtClass sourceClass, String selfName) {
        try {
            PsiElement callee = expression.getCalleeExpression();
            if (callee == null) return false;

            PsiElement resolved = null;
            // Try to resolve the reference
            if (callee instanceof KtReferenceExpression) {
                resolved = ((KtReferenceExpression) callee).getReference().resolve();
            }

            if (resolved == null) {
                return false;
            }

            // Determine container of the resolved element
            String containerName = null;
            if (resolved instanceof PsiMember) {
                com.intellij.psi.PsiClass containingClass = ((PsiMember) resolved).getContainingClass();
                if (containingClass != null) {
                    containerName = containingClass.getQualifiedName();
                }
            } else if (resolved instanceof KtClassOrObject) {
                KtClassOrObject c = (KtClassOrObject) resolved;
                containerName = c.getFqName() != null ? c.getFqName().asString() : c.getName();
            } else if (resolved instanceof KtDeclaration) {
                KtClassOrObject containingClass = PsiTreeUtil.getParentOfType(resolved, KtClassOrObject.class);
                if (containingClass != null) {
                    containerName = containingClass.getFqName() != null ? containingClass.getFqName().asString() : containingClass.getName();
                }
            }

            if (containerName != null) {
                // Filter self calls
                if (selfName != null && selfName.equals(containerName)) {
                    return false;
                }
                // Filter standard library
                if (isStandardClass(containerName)) {
                    return false;
                }
                return true;
            }

        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    private boolean isStandardClass(String qName) {
        return qName.startsWith("java.") || qName.startsWith("kotlin.") || qName.startsWith("javax.");
    }
}