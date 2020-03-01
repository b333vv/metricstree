package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import org.jacoquev.model.metric.Metric;

import java.util.HashSet;
import java.util.Set;

public class DataAbstractingCouplingVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        final Set<PsiClass> classes = new HashSet<PsiClass>();
        final PsiField[] fields = psiClass.getFields();
        for (final PsiField field : fields) {
            if (!field.isPhysical()) {
                continue;
            }
            final PsiType type = field.getType().getDeepComponentType();
            final PsiClass classInType = PsiUtil.resolveClassInType(type);
            if (classInType == null) {
                continue;
            }
            classes.add(classInType);
        }
        metric = Metric.of("DAC", "Data Abstracting Coupling",
                "/html/DataAbstractingCoupling.html", classes.size());
        super.visitClass(psiClass);
    }
}