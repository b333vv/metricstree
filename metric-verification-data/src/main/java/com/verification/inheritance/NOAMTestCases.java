package com.verification.inheritance;

/**
 * Test cases for NOAM (Number of Added Methods) metric verification.
 * NOAM counts the number of methods added in a class that are not inherited or overridden from superclasses.
 */

/**
 * Base class with methods that can be inherited (NOAM should count all methods)
 */
class NOAM_BaseClass {
    protected String name;
    protected int value;

    public NOAM_BaseClass(String name) {
        this.name = name;
        this.value = 0;
    }

    // Public method that can be inherited or overridden
    public void baseMethod() {
        System.out.println("Base method: " + name);
    }

    // Protected method that can be inherited or overridden
    protected void protectedBaseMethod() {
        System.out.println("Protected base method");
    }

    // Private method (always counted as added, can't be inherited)
    private void privateBaseMethod() {
        System.out.println("Private base method");
    }

    // Static method (behavior with inheritance may vary)
    public static void staticBaseMethod() {
        System.out.println("Static base method");
    }

    // Final method (can't be overridden but can be inherited)
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
}

/**
 * Child class that overrides some methods and adds new ones
 * Expected NOAM: Count only truly new methods, not overridden ones
 */
class NOAM_ChildClass extends NOAM_BaseClass {
    private String childProperty;

    public NOAM_ChildClass(String name) {
        super(name);
        this.childProperty = "child";
    }

    // Override parent method (should NOT count toward NOAM)
    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Child override of base method");
    }

    // Override protected method (should NOT count toward NOAM)
    @Override
    protected void protectedBaseMethod() {
        System.out.println("Child override of protected method");
    }

    // NEW method unique to child (should count toward NOAM)
    public void childUniqueMethod() {
        System.out.println("Method unique to child class");
    }

    // Another NEW method unique to child (should count toward NOAM)
    public String getChildProperty() {
        return childProperty;
    }

    // Another NEW method unique to child (should count toward NOAM)
    public void setChildProperty(String childProperty) {
        this.childProperty = childProperty;
    }

    // Private method (should count toward NOAM - can't be inherited)
    private void privateChildMethod() {
        System.out.println("Private child method");
    }

    // Static method (behavior may vary)
    public static void staticChildMethod() {
        System.out.println("Static child method");
    }
}

/**
 * Class with no inheritance (all methods are "added")
 */
class NoInheritance_TestClass1 {
    private String data;
    private int count;

    public NoInheritance_TestClass1() {
        this.data = "default";
        this.count = 0;
    }

    // All methods in this class should count toward NOAM
    public void methodOne() {
        System.out.println("Method one");
    }

    public void methodTwo() {
        System.out.println("Method two");
    }

    private void privateMethod() {
        System.out.println("Private method");
    }

    public static void staticMethod() {
        System.out.println("Static method");
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }
    
    // Total: 7 methods (excluding constructor)
}

/**
 * Child class that only overrides methods (no new methods added)
 */
class OnlyOverrides_TestClass1 extends NOAM_BaseClass {
    public OnlyOverrides_TestClass1(String name) {
        super(name);
    }

    // Override - should NOT count toward NOAM
    @Override
    public void baseMethod() {
        System.out.println("Only override implementation");
    }

    // Override - should NOT count toward NOAM
    @Override
    protected void protectedBaseMethod() {
        System.out.println("Override of protected method");
    }

    // Override - should NOT count toward NOAM
    @Override
    public String getName() {
        return "Override: " + super.getName();
    }

    // Override - should NOT count toward NOAM
    @Override
    public void setName(String name) {
        super.setName("Override: " + name);
    }
    
    // Expected NOAM: 0 (all methods are overrides)
}

/**
 * Abstract class with abstract and concrete methods
 */
abstract class AbstractParent_TestClass2 {
    protected String abstractProperty;

    public AbstractParent_TestClass2(String property) {
        this.abstractProperty = property;
    }

    // Abstract method (may or may not count depending on implementation)
    public abstract void abstractMethod();

    // Concrete method in abstract class (should count toward NOAM)
    public void concreteMethod() {
        System.out.println("Concrete method in abstract class");
    }

    // Protected method (should count toward NOAM)
    protected void protectedMethod() {
        System.out.println("Protected method in abstract class");
    }

    // Expected NOAM: 2 or 3 depending on whether abstract methods count
}

/**
 * Concrete implementation of abstract class
 */
class ConcreteImplementation1 extends AbstractParent_TestClass {
    private String implementationData;

    public ConcreteImplementation1(String property) {
        super(property);
        this.implementationData = "implementation";
    }

    // Implementation of abstract method (may or may not count as "added")
    @Override
    public void abstractMethod() {
        System.out.println("Concrete implementation of abstract method");
    }

    // New method unique to this class (should count toward NOAM)
    public void implementationSpecificMethod() {
        System.out.println("Method specific to concrete implementation");
    }

    // New getter (should count toward NOAM)
    public String getImplementationData() {
        return implementationData;
    }

    // Expected NOAM: 2 or 3 depending on whether abstract method implementations count
}

/**
 * Interface to test method addition with interface implementation
 */
interface TestInterface1 {
    void interfaceMethod();
    
    default void defaultMethod() {
        System.out.println("Default interface method");
    }
}

/**
 * Class that implements interface and extends class
 */
class MultipleInheritance1 extends NOAM_BaseClass implements TestInterface {
    private String multipleData;

    public MultipleInheritance1(String name) {
        super(name);
        this.multipleData = "multiple";
    }

    // Implementation of interface method (may or may not count as "added")
    @Override
    public void interfaceMethod() {
        System.out.println("Interface method implementation");
    }

    // Override of parent class method (should NOT count toward NOAM)
    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Multiple inheritance override");
    }

    // New method unique to this class (should count toward NOAM)
    public void multipleInheritanceMethod() {
        System.out.println("Method unique to multiple inheritance class");
    }

    // New getter (should count toward NOAM)
    public String getMultipleData() {
        return multipleData;
    }

    // Expected NOAM: 2 or 3 depending on whether interface implementations count
}

/**
 * Deep inheritance chain to test method addition at different levels
 */
class DeepInheritance_GrandParent1 {
    public void grandParentMethod() {
        System.out.println("Grand parent method");
    }
    
    protected void protectedGrandParentMethod() {
        System.out.println("Protected grand parent method");
    }
}

class DeepInheritance_Parent1 extends DeepInheritance_GrandParent {
    // New method at parent level (should count toward parent's NOAM)
    public void parentMethod() {
        System.out.println("Parent method");
    }
    
    // Override grand parent method (should NOT count toward parent's NOAM)
    @Override
    public void grandParentMethod() {
        super.grandParentMethod();
        System.out.println("Parent override of grand parent");
    }
}

class DeepInheritance_Child1 extends DeepInheritance_Parent {
    // New method at child level (should count toward child's NOAM)
    public void childMethod() {
        System.out.println("Child method");
    }
    
    // Override parent method (should NOT count toward child's NOAM)
    @Override
    public void parentMethod() {
        super.parentMethod();
        System.out.println("Child override of parent");
    }
    
    // Override grand parent method through inheritance chain (should NOT count)
    @Override
    protected void protectedGrandParentMethod() {
        System.out.println("Child override of grand parent protected method");
    }
    
    // Expected NOAM for child: 1 (only childMethod is truly added)
}