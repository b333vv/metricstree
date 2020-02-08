package org.jacoquev.model.visitor.type;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResponseForClassVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        long responseForClass = 0;

        if (!ClassUtils.isConcrete(psiClass)) {
            return;
        }

        final Set<PsiMethod> methodsCalled = new HashSet<>();
        super.visitClass(psiClass);

        Collections.addAll(methodsCalled, psiClass.getMethods());
        psiClass.acceptChildren(new JavaRecursiveElementVisitor() {

            @Override
            public void visitClass(PsiClass psiClass) {}

            @Override
            public void visitCallExpression(PsiCallExpression callExpression) {
                super.visitCallExpression(callExpression);
                final PsiMethod target = callExpression.resolveMethod();
                if (target != null) {
                    methodsCalled.add(target);
                }
            }
        });

        metric = Metric.of("RFC", "Response For Class",
                "/html/ResponseForClass.html", responseForClass);
    }
}