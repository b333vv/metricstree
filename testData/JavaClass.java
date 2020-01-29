package org.jacoquev.model.code;

import com.intellij.psi.PsiClass;

import java.util.Objects;
import java.util.Set;

public class JavaClass extends JavaCode {
    private final PsiClass psiClass;

    public JavaClass(PsiClass aClass) {
        super(aClass.getName());
        this.psiClass = aClass;
    }

    @SuppressWarnings("unchecked")
    public Set<JavaMethod> getMethods() {
        return (Set<JavaMethod>) (Set<?>) getChildren();
    }

    public void addMethod(JavaMethod javaMethod) {
        addChild(javaMethod);
    }

    public JavaPackage getParentPackage() {
        return (JavaPackage) getParent();
    }

    @Override
    public String toString() {
        return "Type(" + this.getName() + ")";
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaClass)) return false;
        if (!super.equals(o)) return false;
        JavaClass javaClass = (JavaClass) o;
        return Objects.equals(getPsiClass(), javaClass.getPsiClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPsiClass());
    }
}
