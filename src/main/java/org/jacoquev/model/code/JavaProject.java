package org.jacoquev.model.code;

import java.util.Set;

public class JavaProject extends JavaCode {

    public JavaProject(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    public Set<JavaPackage> getPackages() {
        return (Set<JavaPackage>) (Set<?>) getChildren();
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
}
