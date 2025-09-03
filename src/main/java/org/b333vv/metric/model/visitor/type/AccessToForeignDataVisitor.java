package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

public class AccessToForeignDataVisitor extends JavaClassVisitor {
    private final Set<PsiClass> usedClasses = new HashSet<>();

    @Override
    public void visitClass(PsiClass psiClass) {
        usedClasses.clear();
        super.visitClass(psiClass);

        // исключаем сам класс и его родителей
        usedClasses.remove(psiClass);
        for (PsiClass parent : psiClass.getSupers()) {
            usedClasses.remove(parent);
        }

        metric = Metric.of(MetricType.ATFD, Value.of(usedClasses.size()));
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expr) {
        if (expr == null) return;

        super.visitReferenceExpression(expr);

        PsiElement resolved = expr.resolve();
        if (resolved instanceof PsiField) {
            PsiField field = (PsiField) resolved;
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                PsiClass owner = field.getContainingClass();
                if (owner != null) {
                    usedClasses.add(owner);
                }
            }
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression call) {
        super.visitMethodCallExpression(call);

        PsiMethod method = call.resolveMethod();
        if (method == null || method.hasModifierProperty(PsiModifier.STATIC)) {
            return;
        }

        if (isAccessor(method)) {
            PsiClass owner = method.getContainingClass();
            if (owner != null) {
                usedClasses.add(owner);
            }
        }
    }

    private boolean isAccessor(PsiMethod method) {
        String name = method.getName();
        if (name.startsWith("get") && method.getParameterList().isEmpty() && method.getReturnType() != PsiType.VOID) {
            return true;
        }
        if (name.startsWith("is") && method.getParameterList().isEmpty() &&
                PsiType.BOOLEAN.equals(method.getReturnType())) {
            return true;
        }
        if (name.startsWith("set") && method.getParameterList().getParametersCount() == 1 &&
                PsiType.VOID.equals(method.getReturnType())) {
            return true;
        }
        return false;
    }
}