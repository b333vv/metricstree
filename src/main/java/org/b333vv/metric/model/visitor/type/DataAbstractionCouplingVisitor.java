package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import org.b333vv.metric.model.metric.Metric;

import java.util.HashSet;
import java.util.Set;

public class DataAbstractionCouplingVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        final Set<PsiClass> psiClasses = new HashSet<>();
        final PsiField[] psiClassFields = psiClass.getFields();
        for (final PsiField psiField : psiClassFields) {
            if (psiField.isPhysical()) {
                final PsiType psiType = psiField.getType().getDeepComponentType();
                final PsiClass resolvedClassInType = PsiUtil.resolveClassInType(psiType);
                if (resolvedClassInType != null) {
                    psiClasses.add(resolvedClassInType);
                }
            }
        }
        metric = Metric.of("DAC", "Data Abstraction Coupling",
                "/html/DataAbstractionCoupling.html", psiClasses.size());
        super.visitClass(psiClass);
    }
}