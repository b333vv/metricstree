package com.verification.coupling;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.io.Serializable;

class Parent {}
class DependencyA {}
class DependencyB {}
class DependencyC {}
class GenericDependency<T> {}
class Unused {}

interface InterfaceDep {}

public class CBO_TestClass extends Parent implements Serializable { // Coupling to Parent and Serializable
    private DependencyA fieldA; // Coupling to DependencyA
    private List<String> fieldB; // Coupling to List (from java.util)
    private GenericDependency<DependencyB> fieldC; // Coupling to GenericDependency and DependencyB

    public void method(DependencyC paramC) { // Coupling to DependencyC (parameter)
        Set<String> localSet = Set.of("a", "b"); // Coupling to Set (from java.util)
        System.out.println(localSet.size()); // Coupling to System (from java.lang)
    }

    public Map<String, Integer> anotherMethod() { // Coupling to Map (from java.util)
        return Map.of("key", 1);
    }

    public InterfaceDep interfaceMethod() { // Coupling to InterfaceDep
        return null;
    }
}