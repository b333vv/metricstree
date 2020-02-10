package org.jacoquev.model.code;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JavaProject extends JavaCode {
    private Map<String, JavaPackage> packagesMap;
    private Set<JavaClass> classes;
    private Set<JavaMethod> methods;


    public JavaProject(String name) {
        super(name);
        packagesMap = new HashMap<>();
        classes = new HashSet<>();
        methods = new HashSet<>();
    }

    public Stream<JavaPackage> getPackages() {
        return children.stream()
                .filter(c -> c instanceof JavaPackage)
                .map(c -> (JavaPackage) c);
    }

    public void addPackage(JavaPackage aJavaPackage) {
        addChild(aJavaPackage);
    }

    public void addClassToClassesSet(JavaClass javaClass) {
        classes.add(javaClass);
    }

    public void addMethodToMethodsSet(JavaMethod javaMethod) {
        methods.add(javaMethod);
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

    public Map<String, JavaPackage> getPackagesMap() {
        return packagesMap;
    }
    public Stream<JavaPackage> getAllPackages() { return packagesMap.values().stream(); }
    public Stream<JavaClass> getAllClasses() { return classes.stream(); }
    public Stream<JavaMethod> getAllMethods() { return methods.stream(); }

}
