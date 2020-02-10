package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfOperationsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("NOO");
        metric.setDescription("Number Of Operations");
        metric.setDescriptionUrl("/html/NumberOfOperations.html");
        if (ClassUtils.isConcrete(psiClass)) {
            metric.setValue(Value.of(psiClass.getAllMethods().length));
        }
    }
}