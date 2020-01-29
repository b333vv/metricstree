package org.jacoquev.model.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JavaPackage extends JavaCode {
    private Map<String, JavaClass> typeLookup;

    public JavaPackage(String name) {
        super(name);
        typeLookup = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public Set<JavaClass> getTypes() {
        return (Set<JavaClass>) (Set<?>) getChildren();
    }

    public void addType(JavaClass javaClass) {
        typeLookup.put(javaClass.getName(), javaClass);
        addChild(javaClass);
    }

    public JavaProject getParentProject() {
        return (JavaProject) getParent();
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
