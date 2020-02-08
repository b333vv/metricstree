package org.jacoquev.model.code;

import com.intellij.psi.PsiPackage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JavaPackage extends JavaCode {
    private Map<String, JavaClass> typeLookup;
    private final PsiPackage psiPackage;

    public JavaPackage(String name, PsiPackage psiPackage) {
        super(name);
        this.psiPackage = psiPackage;
        typeLookup = new HashMap<>();
    }

    public PsiPackage getPsiPackage() {
        return psiPackage;
    }

    public Set<JavaClass> getClasses() {
        return children.stream()
                .filter(c -> c instanceof JavaClass)
                .map(c -> (JavaClass) c)
                .collect(Collectors.toSet());
    }

    public Set<JavaPackage> getPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c)
                .collect(Collectors.toSet());
    }

    public void addClass(JavaClass javaClass) {
        typeLookup.put(javaClass.getName(), javaClass);
        addChild(javaClass);
    }

    public void addPackage(JavaPackage javaPackage) {
        addChild(javaPackage);
    }

    @Override
    public String toString() {
        return "Package(" + this.getName() + ")";
    }

    public Optional<JavaClass> lookupTypeByName(String typeName) {
        if (typeLookup.containsKey(typeName)) {
            return Optional.of(typeLookup.get(typeName));
        } else {
            return Optional.empty();
        }
    }
}
