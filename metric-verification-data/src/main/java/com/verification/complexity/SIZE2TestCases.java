package com.verification.complexity;

/**
 * Test cases for SIZE2 (Number of Attributes and Methods) metric verification.
 * SIZE2 = (non-static methods including inherited) + (non-static fields including inherited)
 */

class SIZE2_BaseClass {
    // 1 instance field
    private int baseField;

    // 1 static field (should be ignored)
    public static final String CONSTANT = "value";

    // 2 instance methods (constructor + 1 method)
    public SIZE2_BaseClass() {
        this.baseField = 0;
    }

    public void baseMethod() {
        System.out.println("Base method");
    }

    // 1 static method (should be ignored)
    public static void staticBaseMethod() {}
}

class SIZE2_ChildClass extends SIZE2_BaseClass {
    // 2 instance fields
    private String childField1;
    protected double childField2;

    // 1 static field (should be ignored)
    private static int childConstant = 10;

    // 3 instance methods (constructor + 2 methods)
    public SIZE2_ChildClass() {
        super();
    }

    public void childMethod1() {}

    private void childMethod2() {}

    // 1 static method (should be ignored)
    public static void staticChildMethod() {}

    // Expected SIZE2 Calculation for SIZE2_ChildClass:
    // Inherited Instance Fields: 1 (baseField)
    // Declared Instance Fields: 2 (childField1, childField2)
    // Total Fields = 1 + 2 = 3

    // Inherited Instance Methods: 2 (constructor, baseMethod)
    // Declared Instance Methods: 3 (constructor, childMethod1, childMethod2)
    // Total Methods = 2 + 3 = 5

    // Expected SIZE2 = Total Fields + Total Methods = 3 + 5 = 8
}