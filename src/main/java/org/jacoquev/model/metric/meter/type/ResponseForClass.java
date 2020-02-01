package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResponseForClass implements Meter<JavaClass> {

    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long responseForClass = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = getResponseForClass(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("RFC", "Response For Class",
                        "/html/ResponseForClass.html", responseForClass)
        );
    }

    private long getResponseForClass(PsiClass psiClass) {
        Visitor visitor = new Visitor();
        psiClass.accept(visitor);
        return visitor.getResult();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        final Set<PsiMethod> methodsCalled = new HashSet<>();

        @Override
        public void visitClass(PsiClass aClass) {
            if (!TypeUtils.isConcrete(aClass)) {
                return;
            }
            super.visitClass(aClass);

            Collections.addAll(methodsCalled, aClass.getMethods());
            aClass.acceptChildren(new JavaRecursiveElementVisitor() {

                @Override
                public void visitClass(PsiClass aClass) {
                    // do not recurse into anonymous, inner and local classes
                }

                @Override
                public void visitCallExpression(PsiCallExpression callExpression) {
                    super.visitCallExpression(callExpression);
                    final PsiMethod target = callExpression.resolveMethod();
                    if (target != null) {
                        methodsCalled.add(target);
                    }
                }
            });
        }

        public long getResult() {
            return methodsCalled.size();
        }
    }
}
