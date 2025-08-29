package com.verification.inheritance;

/**
 * Test cases for NOC (Number of Children) metric verification.
 * NOC counts the number of immediate subclasses (children) that inherit from a given class.
 */

/**
 * Base class with multiple direct children (NOC = 3)
 */
class NOC_BaseClass {
    protected String name;
    protected int value;

    public NOC_BaseClass(String name) {
        this.name = name;
        this.value = 0;
    }

    public void baseMethod() {
        System.out.println("Base method: " + name);
    }

    protected void protectedMethod() {
        System.out.println("Protected method");
    }
}

/**
 * First direct child of NOC_BaseClass
 */
class NOC_ChildA extends NOC_BaseClass {
    private String childAProperty;

    public NOC_ChildA(String name) {
        super(name);
        this.childAProperty = "ChildA";
    }

    public void childAMethod() {
        System.out.println("Child A specific method");
    }

    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Child A override");
    }
}

/**
 * Second direct child of NOC_BaseClass
 */
class NOC_ChildB extends NOC_BaseClass {
    private int childBProperty;

    public NOC_ChildB(String name) {
        super(name);
        this.childBProperty = 42;
    }

    public void childBMethod() {
        System.out.println("Child B specific method");
    }

    public int getChildBProperty() {
        return childBProperty;
    }
}

/**
 * Third direct child of NOC_BaseClass
 */
class NOC_ChildC extends NOC_BaseClass {
    private boolean childCProperty;

    public NOC_ChildC(String name) {
        super(name);
        this.childCProperty = true;
    }

    public void childCMethod() {
        System.out.println("Child C specific method");
    }

    public boolean isChildCProperty() {
        return childCProperty;
    }
}

/**
 * Grandchild - extends ChildA but should NOT count as direct child of NOC_BaseClass
 */
class NOC_GrandchildA extends NOC_ChildA {
    private String grandchildProperty;

    public NOC_GrandchildA(String name) {
        super(name);
        this.grandchildProperty = "Grandchild";
    }

    public void grandchildMethod() {
        System.out.println("Grandchild specific method");
    }
}

/**
 * Another grandchild - extends ChildB but should NOT count as direct child of NOC_BaseClass
 */
class NOC_GrandchildB extends NOC_ChildB {
    public NOC_GrandchildB(String name) {
        super(name);
    }

    public void anotherGrandchildMethod() {
        System.out.println("Another grandchild method");
    }
}

/**
 * Class with no children (NOC = 0)
 */
class NoChildren_TestClass {
    private String data;

    public NoChildren_TestClass(String data) {
        this.data = data;
    }

    public void someMethod() {
        System.out.println("Method with no inheritance");
    }

    public String getData() {
        return data;
    }
}

/**
 * Class with single child (NOC = 1)
 */
class SingleParent_TestClass {
    protected int value;

    public SingleParent_TestClass(int value) {
        this.value = value;
    }

    public void parentMethod() {
        System.out.println("Parent method");
    }
}

/**
 * Single child of SingleParent_TestClass
 */
class OnlyChild_TestClass extends SingleParent_TestClass {
    private String childData;

    public OnlyChild_TestClass(int value, String childData) {
        super(value);
        this.childData = childData;
    }

    public void childMethod() {
        System.out.println("Only child method: " + childData);
    }
}

/**
 * Abstract class with children (NOC = 2)
 */
abstract class AbstractParent_TestClass {
    protected String abstractProperty;

    public AbstractParent_TestClass(String property) {
        this.abstractProperty = property;
    }

    public abstract void abstractMethod();

    public void concreteMethod() {
        System.out.println("Concrete method in abstract class");
    }
}

/**
 * First concrete implementation of AbstractParent_TestClass
 */
class ConcreteChildA extends AbstractParent_TestClass {
    public ConcreteChildA(String property) {
        super(property);
    }

    @Override
    public void abstractMethod() {
        System.out.println("ConcreteChildA implementation");
    }

    public void specificMethodA() {
        System.out.println("Specific to ConcreteChildA");
    }
}

/**
 * Second concrete implementation of AbstractParent_TestClass
 */
class ConcreteChildB extends AbstractParent_TestClass {
    public ConcreteChildB(String property) {
        super(property);
    }

    @Override
    public void abstractMethod() {
        System.out.println("ConcreteChildB implementation");
    }

    public void specificMethodB() {
        System.out.println("Specific to ConcreteChildB");
    }
}

/**
 * Interface to test interface inheritance (should have NOC = 2)
 */
interface TestInterface {
    void interfaceMethod();
    
    default void defaultMethod() {
        System.out.println("Default interface method");
    }
}

/**
 * First implementation of TestInterface
 */
class InterfaceImplA implements TestInterface {
    @Override
    public void interfaceMethod() {
        System.out.println("Implementation A");
    }

    public void specificToA() {
        System.out.println("Specific to A");
    }
}

/**
 * Second implementation of TestInterface
 */
class InterfaceImplB implements TestInterface {
    @Override
    public void interfaceMethod() {
        System.out.println("Implementation B");
    }

    public void specificToB() {
        System.out.println("Specific to B");
    }
}

/**
 * Class that both extends and implements (multiple inheritance scenario)
 */
class MultipleInheritance extends NOC_BaseClass implements TestInterface {
    public MultipleInheritance(String name) {
        super(name);
    }

    @Override
    public void interfaceMethod() {
        System.out.println("Multiple inheritance implementation");
    }

    public void multipleInheritanceMethod() {
        System.out.println("Specific to multiple inheritance");
    }
}