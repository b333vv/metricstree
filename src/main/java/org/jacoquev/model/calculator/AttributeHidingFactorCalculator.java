package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
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

public class AttributeHidingFactorCalculator {
    private AnalysisScope scope;
    private int numAttributes = 0;
    private int numPublicAttributes = 0;
    private int numClasses = 0;
    private int totalVisibility = 0;
    private Bag<String> classesPerPackage = new Bag<>();
    private Bag<String> packageVisibleAttributesPerPackage = new Bag<>();
    private Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    public AttributeHidingFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());
//        javaProject.getAllClasses()
//                .forEach(c -> c.getPsiClass().accept(new Visitor()));

        totalVisibility += numPublicAttributes * (numClasses - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleAttributes = packageVisibleAttributesPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalVisibility += visibleAttributes * (classes - 1);
        }
        final int denominator = numAttributes * (numClasses - 1);
        final int numerator = denominator - totalVisibility;
        double attributeHidingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                            "AHF",
                            "Attribute Hiding Factor",
                            "/html/AttributeHidingFactor.html",
                            attributeHidingFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            numClasses++;
            final String packageName = ClassUtils.calculatePackageName(aClass);
            classesPerPackage.add(packageName);
        }

        @Override
        public void visitField(PsiField field) {
            super.visitField(field);
            numAttributes++;
            final PsiClass containingClass = field.getContainingClass();

            if (field.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
            } else if (field.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalVisibility += getSubclassCount(containingClass);
            } else if ((field.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                numPublicAttributes++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleAttributesPerPackage.add(packageName);
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
