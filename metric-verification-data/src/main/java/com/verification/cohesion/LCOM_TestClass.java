package com.verification.cohesion;

public class LCOM_TestClass {
    private int fieldA;
    private String fieldB; 
    private double fieldC;
    private boolean fieldD; // Unused field

    // Group 1: Methods using fieldA
    public void methodA1() {
        fieldA = 10;
    }
    
    public void methodA2() {
        System.out.println(fieldA);
    }
    
    // Group 2: Methods using fieldB
    public void methodB1() {
        fieldB = "test";
    }
    
    public void methodB2() {
        System.out.println(fieldB.length());
    }
    
    // Group 3: Methods using fieldC
    public void methodC1() {
        fieldC = 3.14;
    }
    
    // Bridge method: uses both fieldA and fieldB
    public void bridgeMethod() {
        fieldA = fieldB.length();
    }
    
    // Isolated method: uses fieldC only
    public void isolatedMethod() {
        fieldC = fieldC * 2;
    }
    
    // Method using no fields
    public void utilityMethod() {
        System.out.println("Utility");
    }
    
    // Static method (should not be counted)
    public static void staticMethod() {
        System.out.println("Static");
    }
}