package org.b333vv.metric.model.code;

import com.intellij.psi.PsiPackage;

import java.util.stream.Stream;

public class JavaPackage extends JavaCode {
    private final PsiPackage psiPackage;

    public JavaPackage(String name, PsiPackage psiPackage) {
        super(name);
        this.psiPackage = psiPackage;
    }

    public PsiPackage getPsiPackage() {
        return psiPackage;
    }

    public Stream<JavaClass> getClasses() {
        return children.stream()
                .filter(c -> c instanceof JavaClass)
                .map(c -> (JavaClass) c);
    }

    public Stream<JavaPackage> getPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c);
    }

    public void addClass(JavaClass javaClass) {
        addChild(javaClass);
    }

    public void addPackage(JavaPackage javaPackage) {
        addChild(javaPackage);
    }

    @Override
    public String toString() {
        return "Package(" + this.getName() + ")";
    }
}
