package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MethodInheritanceFactorCalculator {
    private AnalysisScope scope;
    private int availableMethods = 0;
    private int inheritedMethods = 0;

    public MethodInheritanceFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());
//        javaProject.getAllClasses()
//                .forEach(c -> c.getPsiClass().accept(new Visitor()));


        double methodInheritanceFactor = (double) inheritedMethods / (double) availableMethods;

        javaProject.addMetric(Metric.of(
                            "MIF",
                            "Method Inheritance Factor",
                            "/html/MethodInheritanceFactor.html",
                            methodInheritanceFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            final PsiMethod[] allMethods = aClass.getAllMethods();
            final Set<PsiMethod> nonOverriddenMethods = new HashSet<>();
            for (PsiMethod method : allMethods) {
                boolean overrideFound = false;
                for (PsiMethod testMethod : allMethods) {
                    if (overrides(testMethod, method)) {
                        overrideFound = true;
                        break;
                    }
                }
                if (!overrideFound) {
                    nonOverriddenMethods.add(method);
                }
            }
            for (PsiMethod method : nonOverriddenMethods) {
                final PsiClass containingClass = method.getContainingClass();
                if (containingClass != null) {
                    if (containingClass.equals(aClass)) {
                        availableMethods++;
                    } else if (classIsInLibrary(containingClass)) {

                    } else if (!method.hasModifierProperty(PsiModifier.PRIVATE)) {
                        availableMethods++;
                        inheritedMethods++;
                    }
                }
            }
        }

        private boolean overrides(PsiMethod testMethod, PsiMethod method) {
            if (testMethod.equals(method)) {
                return false;
            }
            final PsiMethod[] superMethods = testMethod.findSuperMethods();
            for (PsiMethod superMethod : superMethods) {
                if (superMethod.equals(method)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean classIsInLibrary(@NotNull PsiClass aClass) {
        PsiFile file = aClass.getContainingFile();
        if (file == null) {
            return false;
        }
        String fileName = file.getName();
        return !fileName.endsWith(".java");
    }
}
