package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.Bag;
import org.jacoquev.model.metric.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodHidingFactorCalculator {
    private AnalysisScope scope;
    private int numMethods = 0;
    private int numPublicMethods = 0;
    private int numClasses = 0;
    private int totalVisibility = 0;
    private Bag<String> classesPerPackage = new Bag<>();
    private Bag<String> packageVisibleMethodsPerPackage = new Bag<>();
    private Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    public MethodHidingFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());
//        javaProject.getAllClasses()
//                .forEach(c -> c.getPsiClass().accept(new Visitor()));

        totalVisibility += numPublicMethods * (numClasses - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleMethods = packageVisibleMethodsPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalVisibility += visibleMethods * (classes - 1);
        }
        final int denominator = numMethods * (numClasses - 1);
        final int numerator = denominator - totalVisibility;
        double methodHidingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                            "MHF",
                            "Method Hiding Factor",
                            "/html/MethodHidingFactor.html",
                            methodHidingFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            numMethods++;
            final PsiClass containingClass = method.getContainingClass();

            if (method.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
                //don't do anything
            } else if (method.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalVisibility += getSubclassCount(containingClass);
            } else if ((method.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                numPublicMethods++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleMethodsPerPackage.add(packageName);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            numClasses++;
            final String packageName = ClassUtils.calculatePackageName(aClass);
            classesPerPackage.add(packageName);
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
