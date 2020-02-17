package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;

import java.util.HashMap;
import java.util.Map;

public class PolymorphismFactorCalculator {
    private AnalysisScope scope;
    private Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();
    private int numOverridingMethods = 0;
    private int numOverridePotentials = 0;

    public PolymorphismFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

//        javaProject.getAllClasses()
//                .forEach(c -> c.getPsiClass().accept(new Visitor()));

        double polymorphismFactor = numOverridePotentials == 0 ? 1.0 :
                    (double) numOverridingMethods / (double) numOverridePotentials;

        javaProject.addMetric(Metric.of(
                            "PF",
                            "Polymorphism Factor",
                            "/html/PolymorphismFactor.html",
                            polymorphismFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            final PsiMethod[] methods = aClass.getMethods();
            for (PsiMethod method : methods) {
                final PsiMethod[] superMethods = method.findSuperMethods();
                if (superMethods.length == 0) {
                    numOverridePotentials += getSubclassCount(aClass);
                } else {
                    numOverridingMethods++;
                }
            }
        }
    }

    private int getSubclassCount(final PsiClass aClass) {
        if (subclassesPerClass.containsKey(aClass)) {
            return subclassesPerClass.get(aClass);
        }
        int numSubclasses = 0;
        final GlobalSearchScope globalScope = GlobalSearchScope.allScope(scope.getProject());
        final Query<PsiClass> query = ClassInheritorsSearch.search(
                aClass, globalScope, true, true, true);
            for (final PsiClass inheritor : query) {
                if (!inheritor.isInterface()) {
                    numSubclasses++;
                }
            }
        subclassesPerClass.put(aClass, numSubclasses);
        return numSubclasses;
    }
}
