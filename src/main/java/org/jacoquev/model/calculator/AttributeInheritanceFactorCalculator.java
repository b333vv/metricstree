package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;

public class AttributeInheritanceFactorCalculator {
    private AnalysisScope scope;
    private int availableFields = 0;
    private int inheritedFields = 0;

    public AttributeInheritanceFactorCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());


        double attributeInheritanceFactor = (double) inheritedFields / (double) availableFields;

        javaProject.addMetric(Metric.of(
                "AIF",
                "Attribute Inheritance Factor",
                "/html/AttributeInheritanceFactor.html",
                attributeInheritanceFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            final PsiField[] allFields = aClass.getAllFields();
            for (PsiField field : allFields) {
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                final String className = containingClass.getName();
                if (containingClass.equals(aClass)) {
                    availableFields++;
                } else if ("java.lang.Object".equals(className)) {

                } else if (!field.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableFields++;
                    inheritedFields++;
                }
            }
        }
    }
}
