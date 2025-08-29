package com.verification.complexity;

import java.util.List;
import java.util.Arrays;

/**
 * Test cases for NOM (Number of Methods) metric verification
 * Tests various method counting scenarios including constructors, static methods,
 * private methods, overloaded methods, and inheritance patterns
 */
public class NOMTestCases {

    // ========== Test Class 1: Basic Method Counting ==========
    static class NOM_BasicMethods_TestClass {
        // Ground Truth: 6 methods
        // 1. Constructor
        // 2. publicMethod
        // 3. privateMethod  
        // 4. staticMethod
        // 5. getField
        // 6. setField
        
        private String field;
        
        public NOM_BasicMethods_TestClass() {
            this.field = "default";
        }
        
        public void publicMethod() {
            System.out.println("public method");
        }
        
        private void privateMethod() {
            System.out.println("private method");
        }
        
        public static void staticMethod() {
            System.out.println("static method");
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
    }

    // ========== Test Class 2: No Methods (Empty Class) ==========
    static class NOM_EmptyClass_TestClass {
        // Ground Truth: 0 methods (no explicit constructor = default constructor not counted by NOM)
        private String field = "value";
    }

    // ========== Test Class 3: Constructor Only ==========
    static class NOM_ConstructorOnly_TestClass {
        // Ground Truth: 1 method (explicit constructor)
        private int value;
        
        public NOM_ConstructorOnly_TestClass(int value) {
            this.value = value;
        }
    }

    // ========== Test Class 4: Method Overloading ==========
    static class NOM_OverloadedMethods_TestClass {
        // Ground Truth: 5 methods
        // 1. Constructor
        // 2. process(String)
        // 3. process(int)
        // 4. process(String, int)
        // 5. calculate(double)
        
        public NOM_OverloadedMethods_TestClass() {}
        
        public void process(String text) {
            System.out.println("Processing: " + text);
        }
        
        public void process(int number) {
            System.out.println("Processing: " + number);
        }
        
        public void process(String text, int number) {
            System.out.println("Processing: " + text + " " + number);
        }
        
        public double calculate(double value) {
            return value * 2.0;
        }
    }

    // ========== Test Class 5: Inheritance (Child Class) ==========
    static class NOM_BaseClass_TestClass {
        // Ground Truth: 3 methods
        // 1. Constructor
        // 2. baseMethod
        // 3. inheritedMethod
        
        public NOM_BaseClass_TestClass() {}
        
        public void baseMethod() {
            System.out.println("base method");
        }
        
        protected void inheritedMethod() {
            System.out.println("inherited method");
        }
    }
    
    static class NOM_ChildClass_TestClass extends NOM_BaseClass_TestClass {
        // Ground Truth: 3 methods (only locally declared, inherited methods not counted)
        // 1. Constructor
        // 2. childMethod
        // 3. inheritedMethod (overridden - counts as local method)
        
        public NOM_ChildClass_TestClass() {
            super();
        }
        
        public void childMethod() {
            System.out.println("child method");
        }
        
        @Override
        protected void inheritedMethod() {
            System.out.println("overridden inherited method");
        }
    }

    // ========== Test Class 6: Abstract Methods and Interface Implementation ==========
    interface NOM_TestInterface {
        void interfaceMethod();
        default void defaultMethod() {
            System.out.println("default interface method");
        }
    }
    
    static abstract class NOM_AbstractClass_TestClass implements NOM_TestInterface {
        // Ground Truth: 3 methods
        // 1. Constructor
        // 2. concreteMethod
        // 3. abstractMethod (abstract methods count as declared methods)
        
        public NOM_AbstractClass_TestClass() {}
        
        public void concreteMethod() {
            System.out.println("concrete method");
        }
        
        public abstract void abstractMethod();
    }

    // ========== Test Class 7: Complex Method Scenarios ==========
    static class NOM_ComplexMethods_TestClass {
        // Ground Truth: 8 methods
        // 1. Constructor
        // 2. regularMethod
        // 3. synchronizedMethod
        // 4. finalMethod
        // 5. methodWithGeneric
        // 6. methodWithVarargs
        // 7. methodWithExceptions
        // 8. staticFinalMethod
        
        public NOM_ComplexMethods_TestClass() {}
        
        public void regularMethod() {
            System.out.println("regular");
        }
        
        public synchronized void synchronizedMethod() {
            System.out.println("synchronized");
        }
        
        public final void finalMethod() {
            System.out.println("final");
        }
        
        public <T> void methodWithGeneric(T item) {
            System.out.println("generic: " + item);
        }
        
        public void methodWithVarargs(String... args) {
            System.out.println("varargs: " + Arrays.toString(args));
        }
        
        public void methodWithExceptions() throws Exception {
            throw new Exception("test exception");
        }
        
        public static final void staticFinalMethod() {
            System.out.println("static final");
        }
    }

    // ========== Test Class 8: Multiple Constructors ==========
    static class NOM_MultipleConstructors_TestClass {
        // Ground Truth: 4 methods
        // 1. Default constructor
        // 2. Constructor with int
        // 3. Constructor with String
        // 4. getValue method
        
        private String value;
        private int number;
        
        public NOM_MultipleConstructors_TestClass() {
            this("default");
        }
        
        public NOM_MultipleConstructors_TestClass(int number) {
            this.number = number;
            this.value = String.valueOf(number);
        }
        
        public NOM_MultipleConstructors_TestClass(String value) {
            this.value = value;
            this.number = 0;
        }
        
        public String getValue() {
            return value + ":" + number;
        }
    }

    // ========== Test Class 9: Nested Classes (Inner methods should not count for outer) ==========
    static class NOM_OuterClass_TestClass {
        // Ground Truth: 2 methods (only outer class methods count)
        // 1. Constructor
        // 2. outerMethod
        // Note: Inner class methods should NOT count towards outer class NOM
        
        public NOM_OuterClass_TestClass() {}
        
        public void outerMethod() {
            System.out.println("outer method");
        }
        
        class InnerClass {
            public InnerClass() {}
            public void innerMethod() {
                System.out.println("inner method");
            }
        }
        
        static class StaticNestedClass {
            public StaticNestedClass() {}
            public void nestedMethod() {
                System.out.println("nested method");
            }
        }
    }

    // ========== Test Class 10: Anonymous Class Usage ==========
    static class NOM_AnonymousClass_TestClass {
        // Ground Truth: 2 methods
        // 1. Constructor  
        // 2. createRunnable
        // Note: Anonymous class methods should NOT count towards outer class NOM
        
        public NOM_AnonymousClass_TestClass() {}
        
        public Runnable createRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    System.out.println("anonymous method");
                }
            };
        }
    }
}