package org.b333vv.metricsTree.model.code;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.MethodSignature;
import org.b333vv.metricsTree.model.visitor.method.JavaMethodVisitor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaMethod extends JavaCode {
    private final PsiMethod psiMethod;

    public JavaMethod(PsiMethod aMethod) {
        super(signature(aMethod));
        psiMethod = aMethod;
    }

    private static String signature(PsiMethod aMethod) {
        StringBuilder signature = new StringBuilder();
        MethodSignature methodSignature = aMethod.getSignature(PsiSubstitutor.EMPTY);
        signature.append(aMethod.getName());
        signature.append("(");
        signature.append(Arrays.stream(
                methodSignature.getParameterTypes())
                .map(PsiType::getPresentableText)
                .collect(Collectors.joining(", ")));
        signature.append(")");
        return signature.toString();
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public JavaClass getParentType() {
        return (JavaClass) getParent();
    }

    @Override
    public String toString() {
        return "Method(" + this.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaMethod)) return false;
        if (!super.equals(o)) return false;
        JavaMethod that = (JavaMethod) o;
        return Objects.equals(getPsiMethod(), that.getPsiMethod());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiMethod());
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof JavaMethodVisitor) {
            ((JavaMethodVisitor) visitor).visitJavaMethod(this);
        }
    }
}
