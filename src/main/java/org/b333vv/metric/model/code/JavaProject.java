package org.b333vv.metric.model.code;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JavaProject extends JavaCode {
    private final Map<String, JavaPackage> packagesMap;
    private final Set<JavaClass> classes;


    public JavaProject(String name) {
        super(name);
        packagesMap = new HashMap<>();
        classes = new HashSet<>();
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

    @Override
    public String toString() {
        return "Project(" + this.getName() + ")";
    }

    public Map<String, JavaPackage> getPackagesMap() {
        return packagesMap;
    }
    public Stream<JavaPackage> getAllPackages() { return packagesMap.values().stream(); }
    public Stream<JavaClass> getAllClasses() { return classes.stream(); }


}
