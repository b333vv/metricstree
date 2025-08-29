package com.verification.inheritance;

/**
 * Test cases for NOOM (Number of Overridden Methods) metric verification.
 * NOOM counts how many methods in a class override methods from its superclasses.
 */

/**
 * Base class with methods that can be overridden (NOOM should be 0)
 */
class NOOM_BaseClass {
    protected String name;
    protected int value;

    public NOOM_BaseClass(String name) {
        this.name = name;
        this.value = 0;
    }

    // Public method that can be overridden
    public void baseMethod() {
        System.out.println("Base method: " + name);
    }

    // Protected method that can be overridden
    protected void protectedBaseMethod() {
        System.out.println("Protected base method");
    }

    // Private method (cannot be overridden)
    private void privateBaseMethod() {
        System.out.println("Private base method");
    }

    // Static method (cannot be overridden, can be hidden)
    public static void staticBaseMethod() {
        System.out.println("Static base method");
    }

    // Final method (cannot be overridden)
    public final void finalBaseMethod() {
        System.out.println("Final base method");
    }

    // Getter method
    public String getName() {
        return name;
    }

    // Setter method  
    public void setName(String name) {
        this.name = name;
    }

    // Virtual method (designed to be overridden)
    public void virtualMethod() {
        System.out.println("Base virtual method");
    }
}

/**
 * Child class that overrides multiple methods
 * Expected NOOM: Count all methods that override parent methods
 */
class NOOM_ChildClass extends NOOM_BaseClass {
    private String childProperty;

    public NOOM_ChildClass(String name) {
        super(name);
        this.childProperty = "child";
    }

    // Override parent method (should count toward NOOM)
    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Child override of base method");
    }

    // Override protected method (should count toward NOOM)
    @Override
    protected void protectedBaseMethod() {
        System.out.println("Child override of protected method");
    }

    // Override getter (should count toward NOOM)
    @Override
    public String getName() {
        return "Child: " + super.getName();
    }

    // Override setter (should count toward NOOM)
    @Override
    public void setName(String name) {
        super.setName("Child: " + name);
    }

    // Override virtual method (should count toward NOOM)
    @Override
    public void virtualMethod() {
        System.out.println("Child override of virtual method");
    }

    // NEW method unique to child (should NOT count toward NOOM)
    public void childUniqueMethod() {
        System.out.println("Method unique to child class");
    }

    // Another NEW method (should NOT count toward NOOM)
    public String getChildProperty() {
        return childProperty;
    }

    // Private method (should NOT count toward NOOM - not an override)
    private void privateChildMethod() {
        System.out.println("Private child method");
    }

    // Static method (may or may not count - depends on implementation)
    public static void staticBaseMethod() {
        System.out.println("Static child method");
    }
    
    // Expected NOOM: 5 (baseMethod, protectedBaseMethod, getName, setName, virtualMethod)
}

/**
 * Class with no inheritance (NOOM should be 0)
 */
class NoInheritance_TestClass {
    private String data;

    public NoInheritance_TestClass() {
        this.data = "default";
    }

    // No methods are overridden since there's no inheritance
    public void methodOne() {
        System.out.println("Method one");
    }

    public void methodTwo() {
        System.out.println("Method two");
    }

    public String getData() {
        return data;
    }
    
    // Expected NOOM: 0 (no inheritance, no overrides)
}

/**
 * Child class that adds new methods but overrides nothing
 */
class NoOverrides_TestClass extends NOOM_BaseClass {
    private String additionalData;

    public NoOverrides_TestClass(String name) {
        super(name);
        this.additionalData = "additional";
    }

    // NEW methods (should NOT count toward NOOM)
    public void newMethod() {
        System.out.println("New method not overriding anything");
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    private void privateMethod() {
        System.out.println("Private method");
    }
    
    // Expected NOOM: 0 (no methods override parent methods)
}

/**
 * Child class that only overrides methods (no new methods)
 */
class OnlyOverrides_TestClass extends NOOM_BaseClass {
    public OnlyOverrides_TestClass(String name) {
        super(name);
    }

    // All methods are overrides (should count toward NOOM)
    @Override
    public void baseMethod() {
        System.out.println("Only override implementation");
    }

    @Override
    protected void protectedBaseMethod() {
        System.out.println("Override of protected method");
    }

    @Override
    public String getName() {
        return "Override: " + super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName("Override: " + name);
    }

    @Override
    public void virtualMethod() {
        System.out.println("Override of virtual method");
    }
    
    // Expected NOOM: 5 (all methods are overrides)
}

/**
 * Abstract class with abstract and concrete methods
 */
abstract class AbstractParent_TestClass {
    protected String abstractProperty;

    public AbstractParent_TestClass(String property) {
        this.abstractProperty = property;
    }

    // Abstract method (to be implemented by children)
    public abstract void abstractMethod();

    // Concrete method that can be overridden
    public void concreteMethod() {
        System.out.println("Concrete method in abstract class");
    }

    // Protected method that can be overridden
    protected void protectedMethod() {
        System.out.println("Protected method in abstract class");
    }
    
    // Expected NOOM: 0 (base abstract class has no superclass to override from)
}

/**
 * Concrete implementation of abstract class
 */
class ConcreteImplementation extends AbstractParent_TestClass {
    private String implementationData;

    public ConcreteImplementation(String property) {
        super(property);
        this.implementationData = "implementation";
    }

    // Implementation of abstract method (may or may not count as override)
    @Override
    public void abstractMethod() {
        System.out.println("Concrete implementation of abstract method");
    }

    // Override of concrete method (should count toward NOOM)
    @Override
    public void concreteMethod() {
        super.concreteMethod();
        System.out.println("Override of concrete method");
    }

    // New method (should NOT count toward NOOM)
    public void implementationSpecificMethod() {
        System.out.println("Method specific to concrete implementation");
    }
    
    // Expected NOOM: 1 or 2 (depending on whether abstract implementations count)
}

/**
 * Interface to test method overriding with interface implementation
 */
interface TestInterface {
    void interfaceMethod();
    
    default void defaultMethod() {
        System.out.println("Default interface method");
    }
}

/**
 * Class that implements interface and extends class
 */
class MultipleInheritance extends NOOM_BaseClass implements TestInterface {
    private String multipleData;

    public MultipleInheritance(String name) {
        super(name);
        this.multipleData = "multiple";
    }

    // Implementation of interface method (may or may not count as override)
    @Override
    public void interfaceMethod() {
        System.out.println("Interface method implementation");
    }

    // Override of default interface method (may count as override)
    @Override
    public void defaultMethod() {
        System.out.println("Override of default interface method");
    }

    // Override of parent class method (should count toward NOOM)
    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Multiple inheritance override");
    }

    // New method (should NOT count toward NOOM)
    public void multipleInheritanceMethod() {
        System.out.println("Method unique to multiple inheritance class");
    }
    
    // Expected NOOM: 1, 2, or 3 depending on interface method counting
}

/**
 * Deep inheritance chain to test overriding at different levels
 */
class DeepInheritance_GrandParent {
    public void grandParentMethod() {
        System.out.println("Grand parent method");
    }
    
    protected void protectedGrandParentMethod() {
        System.out.println("Protected grand parent method");
    }

    public void anotherGrandParentMethod() {
        System.out.println("Another grand parent method");
    }
    
    // Expected NOOM: 0 (no superclass to override from)
}

class DeepInheritance_Parent extends DeepInheritance_GrandParent {
    // Override grand parent method (should count toward NOOM)
    @Override
    public void grandParentMethod() {
        super.grandParentMethod();
        System.out.println("Parent override of grand parent");
    }
    
    // New method at parent level (should NOT count toward NOOM)
    public void parentMethod() {
        System.out.println("Parent method");
    }
    
    // Expected NOOM: 1 (one override of grandparent method)
}

class DeepInheritance_Child extends DeepInheritance_Parent {
    // Override parent method (should count toward NOOM)
    @Override
    public void parentMethod() {
        super.parentMethod();
        System.out.println("Child override of parent");
    }
    
    // Override grand parent method through inheritance chain (should count toward NOOM)
    @Override
    protected void protectedGrandParentMethod() {
        System.out.println("Child override of grand parent protected method");
    }

    // Override another grand parent method (should count toward NOOM)
    @Override
    public void anotherGrandParentMethod() {
        super.anotherGrandParentMethod();
        System.out.println("Child override of another grand parent method");
    }
    
    // New method at child level (should NOT count toward NOOM)
    public void childMethod() {
        System.out.println("Child method");
    }
    
    // Expected NOOM: 3 (three overrides: parentMethod, protectedGrandParentMethod, anotherGrandParentMethod)
}