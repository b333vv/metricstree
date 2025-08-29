package com.verification.coupling;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

/**
 * Test cases for RFC (Response For Class) metric verification.
 * RFC measures the number of methods that can be executed in response to a message received by an object of the class.
 * This includes both declared methods and methods directly called from within the class.
 */

/**
 * Simple test class with basic methods and method calls
 * Expected RFC: declared methods + unique called methods
 */
class RFC_BasicClass {
    private String name;
    private int value;

    // Constructor (counts as declared method)
    public RFC_BasicClass(String name) {
        this.name = name;
        this.value = 0;
    }

    // Declared method 1
    public void setName(String name) {
        this.name = name;
        // Calls System.out.println (external method call)
        System.out.println("Name set to: " + name);
    }

    // Declared method 2
    public String getName() {
        return this.name;
    }

    // Declared method 3
    public void setValue(int value) {
        this.value = value;
        // Calls validateValue (internal method call)
        validateValue(value);
    }

    // Declared method 4
    public int getValue() {
        return this.value;
    }

    // Declared method 5 - private method
    private void validateValue(int value) {
        if (value < 0) {
            // Another external method call
            System.err.println("Invalid value: " + value);
        }
    }

    // Declared method 6
    public void processData() {
        // Multiple method calls
        String currentName = getName();  // Internal method call
        setValue(42);                    // Internal method call
        System.out.println("Processing: " + currentName); // External method call
    }

    // Expected RFC: 6 declared methods + unique external methods called
    // External methods: System.out.println, System.err.println
    // Internal methods are already counted as declared
    // Total RFC = 6 (declared) + 2 (unique external) = 8
}

/**
 * Class with no method calls (only declared methods)
 */
class RFC_NoCallsClass {
    private String data;

    public RFC_NoCallsClass() {
        this.data = "default";
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public void simpleMethod() {
        // No method calls, just assignment
        this.data = "modified";
    }

    // Expected RFC: 4 (only declared methods, no external calls)
}

/**
 * Class with many external method calls
 */
class RFC_ManyCallsClass {
    private List<String> items;
    private Set<Integer> numbers;

    public RFC_ManyCallsClass() {
        // External method calls to constructors
        this.items = new ArrayList<>();
        this.numbers = Set.of(1, 2, 3);
    }

    public void addItem(String item) {
        // External method call
        items.add(item);
        // External method call
        System.out.println("Added: " + item);
    }

    public void processItems() {
        // Multiple external method calls
        if (items.isEmpty()) {           // List.isEmpty()
            System.out.println("No items"); // System.out.println
            return;
        }

        for (String item : items) {      // List iterator methods
            System.out.println("Item: " + item); // System.out.println (already counted)
        }

        // External method call
        items.clear();
    }

    public void workWithNumbers() {
        // External method calls
        numbers.size();                  // Set.size()
        numbers.contains(5);             // Set.contains()
        System.out.println("Numbers processed"); // System.out.println (already counted)
    }

    public void complexMethod() throws IOException {
        // Method with exception handling
        try {
            processItems();              // Internal method call
            workWithNumbers();           // Internal method call
        } catch (Exception e) {
            // External method calls
            e.printStackTrace();         // Exception.printStackTrace()
            System.err.println("Error occurred"); // System.err.println
        }
    }

    // Expected RFC: 5 declared methods + unique external methods
    // External methods: ArrayList constructor, Set.of, List.add, System.out.println, 
    //                  List.isEmpty, List.clear, Set.size, Set.contains, 
    //                  Exception.printStackTrace, System.err.println
    // Total RFC = 5 (declared) + ~10 (unique external) = ~15
}

/**
 * Class with inheritance and overridden methods
 */
class RFC_BaseForInheritance {
    protected String baseData;

    public RFC_BaseForInheritance() {
        this.baseData = "base";
    }

    public void baseMethod() {
        System.out.println("Base method: " + baseData);
    }

    public void virtualMethod() {
        System.out.println("Virtual method");
    }

    protected void protectedMethod() {
        System.out.println("Protected method");
    }

    // Expected RFC: 4 declared + unique external calls = 4 + 1 (System.out.println) = 5
}

class RFC_InheritanceClass extends RFC_BaseForInheritance {
    private String childData;

    public RFC_InheritanceClass() {
        super();                         // Call to parent constructor
        this.childData = "child";
    }

    @Override
    public void virtualMethod() {
        super.virtualMethod();           // Call to parent method
        System.out.println("Child virtual method");
    }

    public void childMethod() {
        baseMethod();                    // Call to inherited method
        protectedMethod();               // Call to inherited protected method
        virtualMethod();                 // Call to overridden method
        System.out.println("Child method executed");
    }

    public void utilityMethod() {
        // External library calls
        String result = String.valueOf(42);      // String.valueOf
        HashMap<String, String> map = new HashMap<>(); // HashMap constructor
        map.put("key", result);                  // HashMap.put
        map.get("key");                         // HashMap.get
    }

    // Expected RFC: 4 declared methods + inherited accessible methods + unique external calls
    // Declared: constructor, virtualMethod, childMethod, utilityMethod
    // Inherited accessible: baseMethod, protectedMethod (virtualMethod already declared)
    // External: System.out.println, String.valueOf, HashMap constructor, HashMap.put, HashMap.get
    // Total RFC = 4 (declared) + 2 (inherited) + 5 (external) = 11
}

/**
 * Interface to test method resolution
 */
interface RFC_TestInterface {
    void interfaceMethod();
    
    default void defaultMethod() {
        System.out.println("Default interface method");
    }
}

/**
 * Class implementing interface
 */
class RFC_InterfaceImpl implements RFC_TestInterface {
    private String data;

    public RFC_InterfaceImpl() {
        this.data = "implementation";
    }

    @Override
    public void interfaceMethod() {
        System.out.println("Interface method implementation");
        defaultMethod();                 // Call to default interface method
    }

    public void implementationMethod() {
        interfaceMethod();               // Call to implemented method
        System.out.println("Implementation method");
    }

    // Expected RFC: 3 declared methods + inherited interface methods + external calls
    // Declared: constructor, interfaceMethod, implementationMethod
    // Inherited: defaultMethod
    // External: System.out.println
    // Total RFC = 3 (declared) + 1 (inherited) + 1 (external) = 5
}

/**
 * Class with static methods and static method calls
 */
class RFC_StaticMethodsClass {
    private static int counter = 0;
    private String instance;

    public RFC_StaticMethodsClass() {
        this.instance = "instance";
        incrementCounter();              // Call to static method
    }

    public static void incrementCounter() {
        counter++;
        System.out.println("Counter: " + counter);
    }

    public static int getCounter() {
        return counter;
    }

    public void instanceMethod() {
        // Calls to static methods
        incrementCounter();              // Static method call
        int current = getCounter();      // Static method call
        System.out.println("Instance method, counter: " + current);

        // External static method calls
        String str = String.valueOf(current);    // String.valueOf
        Integer.parseInt("123");                 // Integer.parseInt
    }

    public void mixedCalls() {
        instanceMethod();                // Instance method call
        RFC_StaticMethodsClass.incrementCounter(); // Qualified static call
        System.gc();                     // External static method call
    }

    // Expected RFC: 5 declared methods + unique external calls
    // Declared: constructor, incrementCounter, getCounter, instanceMethod, mixedCalls
    // External: System.out.println, String.valueOf, Integer.parseInt, System.gc
    // Total RFC = 5 (declared) + 4 (external) = 9
}

/**
 * Empty class for baseline testing
 */
class RFC_EmptyClass {
    // Expected RFC: 0 (no declared methods, no method calls)
}

/**
 * Class with only constructor
 */
class RFC_ConstructorOnlyClass {
    private String data;

    public RFC_ConstructorOnlyClass(String data) {
        this.data = data;
        System.out.println("Constructor called with: " + data);
    }

    // Expected RFC: 1 declared method (constructor) + 1 external call = 2
}

/**
 * Class with complex method call chains
 */
class RFC_ComplexCallsClass {
    private List<String> data;

    public RFC_ComplexCallsClass() {
        this.data = new ArrayList<>();
    }

    public void chainedCalls() {
        // Complex method call chain
        data.stream()                    // List.stream()
            .filter(s -> s.length() > 3) // Stream.filter() + String.length()
            .map(String::toUpperCase)    // Stream.map() + String.toUpperCase()
            .forEach(System.out::println); // Stream.forEach() + System.out.println()

        // More chained calls
        data.add("test");                // List.add()
        data.size();                     // List.size()
    }

    public void lambdaMethod() {
        // Lambda expressions with method calls
        data.forEach(item -> {
            System.out.println("Processing: " + item); // System.out.println
            item.toUpperCase();          // String.toUpperCase
        });
    }

    // Expected RFC: 3 declared methods + many external method calls
    // External: ArrayList constructor, List.stream, Stream.filter, String.length,
    //          Stream.map, String.toUpperCase, Stream.forEach, System.out.println,
    //          List.add, List.size
    // Total RFC = 3 (declared) + ~10 (unique external) = ~13
}