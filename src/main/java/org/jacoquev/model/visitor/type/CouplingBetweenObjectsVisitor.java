package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

import static org.jacoquev.exec.ProjectMetricsRunner.getDependencyMap;

public class CouplingBetweenObjectsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        metric.setName("CBO");
        metric.setDescription("Coupling Between Objects");
        metric.setDescriptionUrl("/html/CouplingBetweenObjects.html");
        if (ClassUtils.isConcrete(aClass)) {
            DependencyMap dependencyMap = getDependencyMap();
            Set<PsiClass> dependencies = dependencyMap.calculateDependencies(aClass);
            Set<PsiClass> dependents = dependencyMap.calculateDependents(aClass);
            Set<PsiClass> union = new HashSet<>(dependencies);
            union.addAll(dependents);
            metric.setValue(Value.of(union.size()));
        }
    }
}