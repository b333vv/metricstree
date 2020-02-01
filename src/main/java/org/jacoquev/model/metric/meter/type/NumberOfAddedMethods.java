package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.MethodUtils;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfAddedMethods implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long numberOfAddedMethods = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = calculateNumberOfAddedMethods(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("NOAM", "Number Of Added Methods",
                        "/html/NumberOfAddedMethods.html", numberOfAddedMethods)
        );
    }

    public long calculateNumberOfAddedMethods(PsiClass psiClass) {
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
            int numAddedMethods = 0;
            for (final PsiMethod method : methods) {
                if (method.isConstructor() || method.hasModifierProperty(PsiModifier.ABSTRACT)) {
                    continue;
                }
                if (method.hasModifierProperty(PsiModifier.PRIVATE) || method.hasModifierProperty(PsiModifier.STATIC)) {
                    numAddedMethods++;
                    continue;
                }
                if (!MethodUtils.hasConcreteSuperMethod(method)) {
                    numAddedMethods++;
                }
            }
            result = numAddedMethods;
        }

        public long getResult() {
            return result;
        }
    }
}
