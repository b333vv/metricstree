package com.verification.inheritance;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

/**
 * Test cases for DIT (Depth of Inheritance Tree) metric verification.
 * DIT measures the number of ancestor classes from the analyzed class up to the root of the inheritance hierarchy.
 */

/**
 * Base class with no superclass (extends Object implicitly)
 * Expected DIT: 0 or 1 depending on whether Object is counted
 */
class DIT_RootClass {
    protected String data;

    public DIT_RootClass() {
        this.data = "root";
    }

    public void rootMethod() {
        System.out.println("Root method");
    }

    // Expected DIT: 0 (no explicit superclass) or 1 if Object is counted
}

/**
 * First level inheritance - extends root class
 * Expected DIT: 1 or 2 depending on Object counting
 */
class DIT_Level1 extends DIT_RootClass {
    private String level1Data;

    public DIT_Level1() {
        super();
        this.level1Data = "level1";
    }

    public void level1Method() {
        System.out.println("Level 1 method");
    }

    // Expected DIT: 1 (DIT_RootClass) or 2 if Object is counted
}

/**
 * Second level inheritance
 * Expected DIT: 2 or 3 depending on Object counting
 */
class DIT_Level2 extends DIT_Level1 {
    private String level2Data;

    public DIT_Level2() {
        super();
        this.level2Data = "level2";
    }

    public void level2Method() {
        System.out.println("Level 2 method");
    }

    // Expected DIT: 2 (DIT_Level1 -> DIT_RootClass) or 3 if Object is counted
}

/**
 * Third level inheritance
 * Expected DIT: 3 or 4 depending on Object counting
 */
class DIT_Level3 extends DIT_Level2 {
    private String level3Data;

    public DIT_Level3() {
        super();
        this.level3Data = "level3";
    }

    public void level3Method() {
        System.out.println("Level 3 method");
    }

    // Expected DIT: 3 (DIT_Level2 -> DIT_Level1 -> DIT_RootClass) or 4 if Object is counted
}

/**
 * Fourth level inheritance
 * Expected DIT: 4 or 5 depending on Object counting
 */
class DIT_Level4 extends DIT_Level3 {
    private String level4Data;

    public DIT_Level4() {
        super();
        this.level4Data = "level4";
    }

    public void level4Method() {
        System.out.println("Level 4 method");
    }

    // Expected DIT: 4 (DIT_Level3 -> DIT_Level2 -> DIT_Level1 -> DIT_RootClass) or 5 if Object is counted
}

/**
 * Class extending standard Java library class
 * Expected DIT: depends on hierarchy depth of AbstractList
 */
class DIT_ExtendsLibraryClass extends AbstractList<String> {
    private String[] data;
    private int size;

    public DIT_ExtendsLibraryClass() {
        this.data = new String[10];
        this.size = 0;
    }

    @Override
    public String get(int index) {
        return data[index];
    }

    @Override
    public int size() {
        return size;
    }

    // Expected DIT: AbstractList hierarchy depth
    // AbstractList -> AbstractCollection -> Object (typical Java library hierarchy)
    // So DIT likely = 2 or 3 depending on Object counting
}

/**
 * Class implementing interface (interfaces don't contribute to DIT)
 * Expected DIT: 0 or 1 depending on Object counting
 */
class DIT_ImplementsInterface implements Serializable {
    private String data;

    public DIT_ImplementsInterface() {
        this.data = "interface implementation";
    }

    public void interfaceMethod() {
        System.out.println("Interface implementation method");
    }

    // Expected DIT: 0 (no class inheritance, only interface implementation) or 1 if Object is counted
}

/**
 * Class both extending class and implementing interface
 * Expected DIT: based only on class inheritance, interface ignored
 */
class DIT_ExtendsAndImplements extends DIT_RootClass implements Serializable {
    private String extraData;

    public DIT_ExtendsAndImplements() {
        super();
        this.extraData = "extends and implements";
    }

    public void combinedMethod() {
        System.out.println("Combined inheritance method");
    }

    // Expected DIT: 1 (DIT_RootClass) or 2 if Object is counted
    // Interface implementation doesn't affect DIT
}

/**
 * Abstract class in inheritance hierarchy
 */
abstract class DIT_AbstractParent {
    protected String abstractData;

    public DIT_AbstractParent() {
        this.abstractData = "abstract";
    }

    public abstract void abstractMethod();

    public void concreteMethod() {
        System.out.println("Concrete method in abstract class");
    }

    // Expected DIT: 0 or 1 depending on Object counting
}

/**
 * Class extending abstract class
 * Expected DIT: 1 or 2 depending on Object counting
 */
class DIT_ExtendsAbstract extends DIT_AbstractParent {
    private String concreteData;

    public DIT_ExtendsAbstract() {
        super();
        this.concreteData = "concrete";
    }

    @Override
    public void abstractMethod() {
        System.out.println("Implementation of abstract method");
    }

    // Expected DIT: 1 (DIT_AbstractParent) or 2 if Object is counted
}

/**
 * Interface for testing interface inheritance (doesn't contribute to DIT)
 */
interface DIT_BaseInterface {
    void baseInterfaceMethod();
}

/**
 * Extended interface
 */
interface DIT_ExtendedInterface extends DIT_BaseInterface {
    void extendedInterfaceMethod();
}

/**
 * Class implementing extended interface (interface hierarchy doesn't affect DIT)
 * Expected DIT: 0 or 1 depending on Object counting
 */
class DIT_ImplementsExtendedInterface implements DIT_ExtendedInterface {
    private String implementationData;

    public DIT_ImplementsExtendedInterface() {
        this.implementationData = "extended interface implementation";
    }

    @Override
    public void baseInterfaceMethod() {
        System.out.println("Base interface method implementation");
    }

    @Override
    public void extendedInterfaceMethod() {
        System.out.println("Extended interface method implementation");
    }

    // Expected DIT: 0 (no class inheritance) or 1 if Object is counted
    // Interface inheritance hierarchy doesn't contribute to DIT
}

/**
 * Generic class with inheritance
 */
class DIT_GenericParent<T> {
    protected T genericData;

    public DIT_GenericParent(T data) {
        this.genericData = data;
    }

    public T getGenericData() {
        return genericData;
    }

    // Expected DIT: 0 or 1 depending on Object counting
}

/**
 * Class extending generic parent
 * Expected DIT: 1 or 2 depending on Object counting
 */
class DIT_ExtendsGeneric extends DIT_GenericParent<String> {
    private String specificData;

    public DIT_ExtendsGeneric() {
        super("generic inheritance");
        this.specificData = "specific";
    }

    public void specificMethod() {
        System.out.println("Specific method: " + getGenericData());
    }

    // Expected DIT: 1 (DIT_GenericParent) or 2 if Object is counted
}

/**
 * Multiple inheritance simulation through composition (doesn't affect DIT)
 */
class DIT_CompositionNotInheritance {
    private DIT_RootClass rootComponent;
    private DIT_Level1 level1Component;

    public DIT_CompositionNotInheritance() {
        this.rootComponent = new DIT_RootClass();
        this.level1Component = new DIT_Level1();
    }

    public void useComponents() {
        rootComponent.rootMethod();
        level1Component.level1Method();
    }

    // Expected DIT: 0 or 1 depending on Object counting
    // Composition doesn't create inheritance relationship
}

/**
 * Inner class inheritance
 */
class DIT_OuterClassWithInner {
    private String outerData;

    public DIT_OuterClassWithInner() {
        this.outerData = "outer";
    }

    // Expected DIT: 0 or 1 depending on Object counting

    /**
     * Inner class extending outer class type
     * Expected DIT: depends on whether inner classes are treated normally
     */
    class DIT_InnerClass extends DIT_RootClass {
        private String innerData;

        public DIT_InnerClass() {
            super();
            this.innerData = "inner";
        }

        public void innerMethod() {
            System.out.println("Inner method: " + outerData);
        }

        // Expected DIT: 1 (DIT_RootClass) or 2 if Object is counted
        // Inner class status shouldn't affect DIT calculation
    }
}

/**
 * Anonymous class test case
 */
class DIT_AnonymousClassTest {
    private String data;

    public DIT_AnonymousClassTest() {
        this.data = "anonymous test";
    }

    public DIT_RootClass createAnonymous() {
        // Anonymous class extending DIT_RootClass
        return new DIT_RootClass() {
            @Override
            public void rootMethod() {
                System.out.println("Anonymous override: " + data);
            }

            // Expected DIT for anonymous class: 1 (DIT_RootClass) or 2 if Object is counted
        };
    }

    // Expected DIT for DIT_AnonymousClassTest: 0 or 1 depending on Object counting
}

/**
 * Class with no explicit constructor (uses default)
 * Expected DIT: 0 or 1 depending on Object counting
 */
class DIT_DefaultConstructor {
    private String data = "default constructor";

    public void someMethod() {
        System.out.println("Method in default constructor class: " + data);
    }

    // Expected DIT: 0 (no explicit superclass) or 1 if Object is counted
}