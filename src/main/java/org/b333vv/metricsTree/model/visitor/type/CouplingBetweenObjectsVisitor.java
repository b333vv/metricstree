package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metricsTree.exec.ProjectMetricsRunner;
import org.b333vv.metricsTree.model.code.DependencyMap;
import org.b333vv.metricsTree.model.metric.util.ClassUtils;
import org.b333vv.metricsTree.model.metric.value.Value;
import org.b333vv.metricsTree.model.metric.Metric;

import java.util.HashSet;
import java.util.Set;

public class CouplingBetweenObjectsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        metric = Metric.of("CBO", "Coupling Between Objects",
                "/html/CouplingBetweenObjects.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(aClass)) {
            DependencyMap dependencyMap = ProjectMetricsRunner.getDependencyMap();
            Set<PsiClass> dependencies = dependencyMap.calculateDependencies(aClass);
            Set<PsiClass> dependents = dependencyMap.calculateDependents(aClass);
            Set<PsiClass> union = new HashSet<>(dependencies);
            union.addAll(dependents);
            metric = Metric.of("CBO", "Coupling Between Objects",
                    "/html/CouplingBetweenObjects.html", union.size());
        }
    }
}