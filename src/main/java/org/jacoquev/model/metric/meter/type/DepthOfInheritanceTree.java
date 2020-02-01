package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiTypeParameter;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class DepthOfInheritanceTree implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long depthOfInheritanceTree = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = getInheritanceDepth(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("DIT", "Depth Of Inheritance Tree",
                        "/html/DepthOfInheritanceTree.html", depthOfInheritanceTree)
        );
    }

    private long getInheritanceDepth(PsiClass psiClass) {
        final PsiClass superClass = psiClass.getSuperClass();
        return superClass == null ? 0 : getInheritanceDepth(superClass) + 1;
    }
}
