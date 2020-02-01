package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.MethodUtils;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfOverriddenMethods implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long numberOfOverriddenMethods = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = calculateNumberOfOverriddenMethods(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("NOOM", "Number Of Overridden Methods",
                        "/html/NumberOfOverriddenMethods.html", numberOfOverriddenMethods)
        );
    }

    public long calculateNumberOfOverriddenMethods(PsiClass psiClass) {
        Visitor visitor = new Visitor();
        psiClass.accept(visitor);
        return visitor.getResult();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        private long result;

        @Override
        public void visitClass(final PsiClass aClass) {
            super.visitClass(aClass);
            final PsiMethod[] methods = aClass.getMethods();
            int numOverriddenMethods = 0;
            for (final PsiMethod method : methods) {
                if (!MethodUtils.isConcrete(method)) {
                    continue;
                }
                if (MethodUtils.hasConcreteSuperMethod(method)) {
                    numOverriddenMethods++;
                }
            }
            result = numOverriddenMethods;
        }

        public long getResult() {
            return result;
        }
    }
}
