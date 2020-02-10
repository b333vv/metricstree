package org.jacoquev.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.value.Value;
import org.jacoquev.model.visitor.util.MethodComplexityVisitor;

public class McCabeCyclomaticComplexityVisitor extends JavaMethodVisitor {

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        metric.setName("CC");
        metric.setDescription("McCabe Cyclomatic Complexity");
        metric.setDescriptionUrl("/html/McCabeCyclomaticComplexity.html");
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        metric.setValue(Value.of(visitor.getMethodComplexity()));
    }
}
