package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;

import java.util.Set;

import static org.jacoquev.exec.ProjectMetricsRunner.getDependencyMap;

public class CouplingFactorCalculator {
    private AnalysisScope scope;
    private int totalCoupling = 0;
    private int numClasses = 0;

    public CouplingFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

//        javaProject.getAllClasses()
//                .forEach(c -> c.getPsiClass().accept(new Visitor()));
        final int denominator = (numClasses * (numClasses - 1)) / 2;
        final int numerator = totalCoupling;
        double couplingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                            "CF",
                            "Coupling Factor",
                            "/html/CouplingFactor.html",
                            couplingFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            numClasses++;
            final DependencyMap dependencyMap = getDependencyMap();
            final Set<PsiClass> dependencies = dependencyMap.calculateDependencies(aClass);
            //TODO: this isn't quite correct, as it includes superclasses
            totalCoupling += dependencies.size();
        }
    }
}
