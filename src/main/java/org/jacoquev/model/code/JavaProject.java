package org.jacoquev.model.code;

import org.jacoquev.model.visitor.type.JavaClassVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaProject extends JavaCode {
    private Map<String, JavaPackage> packageMap;

    public JavaProject(String name) {
        super(name);
        packageMap = new HashMap<>();
    }

    public Set<JavaPackage> getPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c)
                .collect(Collectors.toSet());
    }

    public void addPackage(JavaPackage aJavaPackage) {
        addChild(aJavaPackage);
    }

    @Override
    public String toString() {
        return "Project(" + this.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Map<String, JavaPackage> getPackageMap() {
        return packageMap;
    }

}
