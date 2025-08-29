package com.verification.coupling;

class BaseClass {
    public void baseMethod() {}
    protected void protectedBaseMethod() {}
}

interface InterfaceA {
    void interfaceMethodA();
}

interface InterfaceB {
    void interfaceMethodB();
}

public class RFC_TestClass extends BaseClass implements InterfaceA, InterfaceB {
    
    // Own methods
    public void ownMethod1() {
        System.out.println("Method 1"); // Call to external method
    }
    
    public void ownMethod2() {
        helper(); // Call to own method
    }
    
    private void helper() {
        protectedBaseMethod(); // Call to inherited method
    }
    
    // Implemented methods from interfaces
    @Override
    public void interfaceMethodA() {
        String str = "test"; // Call to String constructor/methods
        int length = str.length(); // Call to String.length()
    }
    
    @Override
    public void interfaceMethodB() {
        java.util.List<String> list = java.util.Arrays.asList("a", "b"); // Calls to Arrays.asList
        list.size(); // Call to List.size()
    }
    
    // Constructor
    public RFC_TestClass() {
        super(); // Call to parent constructor
    }
}