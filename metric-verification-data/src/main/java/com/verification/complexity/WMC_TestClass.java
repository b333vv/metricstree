package com.verification.complexity;

public class WMC_TestClass {
    
    // Simple method - complexity 1
    public void simpleMethod() {
        System.out.println("Simple");
    }
    
    // Method with if statement - complexity 2
    public void methodWithIf(boolean condition) {
        if (condition) {
            System.out.println("True");
        } else {
            System.out.println("False");
        }
    }
    
    // Method with loop - complexity 2  
    public void methodWithLoop() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }
    
    // Method with switch - complexity based on case count
    public void methodWithSwitch(int value) {
        switch (value) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            case 3:
                System.out.println("Three");
                break;
            default:
                System.out.println("Other");
        }
    }
    
    // Complex method with multiple control structures - higher complexity
    public void complexMethod(int x, boolean flag) {
        if (x > 0) { // +1
            for (int i = 0; i < x; i++) { // +1
                if (flag && i % 2 == 0) { // +2 (&&, %)
                    try {
                        System.out.println("Even: " + i);
                    } catch (Exception e) { // +1
                        System.err.println("Error");
                    }
                } else if (flag || i > 5) { // +2 (else if, ||)
                    System.out.println("Odd or large: " + i);
                }
            }
        }
        // Base complexity: 1, Additional: 1+1+2+1+2 = 7, Total: 8
    }
    
    // Constructor - should be counted
    public WMC_TestClass() {
        // Simple constructor - complexity 1
    }
    
    // Static method - may or may not be counted depending on implementation
    public static void staticMethod() {
        // Simple static method - complexity 1
    }
}