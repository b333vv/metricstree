package org.jacoquev.model.visitor.pack;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPackage;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.BucketedCount;
import org.jacoquev.model.metric.util.ClassUtils;

import java.util.Set;

import static org.jacoquev.exec.ProjectMetricsRunner.getDependencyMap;

public class PackageAbstractnessCalculator {
    private BucketedCount<PsiPackage> numAbstractClassesPerPackage = new BucketedCount<>();
    private BucketedCount<PsiPackage> numClassesPerPackage = new BucketedCount<>();

    public void calculate(JavaProject javaProject) {

        javaProject.getAllClasses()
                .forEach(c -> c.getPsiClass().accept(new Visitor()));

        javaProject.getAllPackages()
                .forEach(p -> {
                    int numClasses = numClassesPerPackage.getBucketValue(p.getPsiPackage());
                    int numAbstractClasses = numAbstractClassesPerPackage.getBucketValue(p.getPsiPackage());
                    double abstractness = numClasses == 0 ? 0.0 :
                            (double) numAbstractClasses / (double) numClasses;
                    p.addMetric(Metric.of(
                            "A",
                            "Abstractness",
                            "/html/Abstractness.html",
                            abstractness));
                });
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            if (ClassUtils.isAnonymous(aClass)) {
                return;
            }
            final PsiPackage aPackage = ClassUtils.findPackage(aClass);
            if (aPackage == null) {
                return;
            }
            if (aClass.isInterface() || aClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                numAbstractClassesPerPackage.incrementBucketValue(aPackage);
            }
            numClassesPerPackage.incrementBucketValue(aPackage);
        }
    }
}
