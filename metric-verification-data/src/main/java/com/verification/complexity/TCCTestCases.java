package com.verification.cohesion;

/**
 * Test cases for TCC (Tight Class Cohesion) metric verification.
 * TCC measures the relative number of directly connected method pairs,
 * where methods are connected if they access at least one common field.
 */
class TCC_TestClass {
    // Fields accessed by different methods to create various cohesion patterns
    private int fieldA;
    private String fieldB;
    private boolean fieldC;
    private double fieldD;

    // Methods that share fieldA - creates connected pairs
    public void methodA1() {
        fieldA = 1;
        System.out.println("Method A1: " + fieldA);
    }

    public void methodA2() {
        fieldA = fieldA + 1;
        System.out.println("Method A2: " + fieldA);
    }

    // Methods that share fieldB - creates connected pairs
    public void methodB1() {
        fieldB = "Hello";
        System.out.println("Method B1: " + fieldB);
    }

    public void methodB2() {
        fieldB = fieldB + " World";
        System.out.println("Method B2: " + fieldB);
    }

    // Method that bridges fieldA and fieldB - creates connections between A and B groups
    public void bridgeMethod() {
        fieldA = fieldB.length();
        System.out.println("Bridge: " + fieldA + ", " + fieldB);
    }

    // Isolated method using only fieldC - no connections to others
    public void isolatedMethod() {
        fieldC = true;
        System.out.println("Isolated: " + fieldC);
    }

    // Method using no fields - no connections
    public void noFieldMethod() {
        System.out.println("No fields used");
    }

    // Method using fieldD - isolated
    public void methodD() {
        fieldD = 3.14;
        System.out.println("Method D: " + fieldD);
    }

    // Constructor - may or may not be counted depending on implementation
    public TCC_TestClass() {
        fieldA = 0;
        fieldB = "";
        fieldC = false;
        fieldD = 0.0;
    }
}

/**
 * Simple class with perfect cohesion - all methods share all fields
 */
class PerfectCohesion_TestClass {
    private int sharedField;

    public void method1() {
        sharedField = 1;
    }

    public void method2() {
        sharedField = 2;
    }

    public void method3() {
        sharedField = 3;
    }
}

/**
 * Class with no cohesion - no methods share fields
 */
class NoCohesion_TestClass {
    private int fieldA;
    private int fieldB;
    private int fieldC;

    public void methodA() {
        fieldA = 1;
    }

    public void methodB() {
        fieldB = 2;
    }

    public void methodC() {
        fieldC = 3;
    }
}

/**
 * Single method class - edge case
 */
class SingleMethod_TestClass {
    private int field;

    public void onlyMethod() {
        field = 42;
    }
}