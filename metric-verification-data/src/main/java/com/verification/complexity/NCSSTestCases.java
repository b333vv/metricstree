package com.verification.complexity;

/**
 * Test cases for NCSS (Non-Commenting Source Statements) metric verification.
 * NCSS counts the number of executable statements in a class, excluding comments and empty statements.
 */

/**
 * Primary test class with various types of statements
 * Expected NCSS: Count all executable statements, exclude comments and empty statements
 */
class NCSS_TestClass {
    // Field declarations (may or may not count as statements)
    private String field1 = "value1";
    private int field2;
    
    /*
     * Multi-line comment should not count
     */
    private boolean field3 = true;
    
    // Constructor statements
    public NCSS_TestClass() {
        // Comment inside constructor - should not count
        this.field2 = 42; // Statement 1: assignment
        
        // Empty line above - should not count
        
        this.field3 = false; // Statement 2: assignment
    } // Total constructor statements: 2
    
    public void methodWithVariousStatements() {
        // Local variable declarations
        String localVar = "local"; // Statement 3: declaration with assignment
        int number;                // Statement 4: declaration (may or may not count)
        
        // Assignment statements
        number = 10;               // Statement 5: assignment
        localVar = "updated";      // Statement 6: assignment
        
        // Method call statements
        System.out.println(localVar); // Statement 7: method call
        processData(number);           // Statement 8: method call
        
        // Control flow statements
        if (number > 5) {              // Statement 9: if statement
            localVar = "greater";      // Statement 10: assignment in if
            System.out.println("greater than 5"); // Statement 11: method call in if
        } else {                       // else clause
            localVar = "smaller";      // Statement 12: assignment in else
        }
        
        // Loop statements
        for (int i = 0; i < 3; i++) {  // Statement 13: for loop
            System.out.println("Loop: " + i); // Statement 14: method call in loop
        }
        
        // While loop
        int counter = 0;               // Statement 15: declaration with assignment
        while (counter < 2) {          // Statement 16: while loop
            counter++;                 // Statement 17: increment
        }
        
        // Try-catch statements
        try {                          // try block
            Integer.parseInt("123");   // Statement 18: method call in try
        } catch (NumberFormatException e) { // catch clause
            System.err.println("Error"); // Statement 19: method call in catch
        }
        
        // Switch statement
        switch (number) {              // Statement 20: switch
            case 10:                   // case label
                localVar = "ten";      // Statement 21: assignment in case
                break;                 // Statement 22: break
            default:                   // default label
                localVar = "other";    // Statement 23: assignment in default
                break;                 // Statement 24: break
        }
        
        // Return statement
        // Note: void method, no return statement needed
    } // Expected: ~20-24 statements depending on what counts
    
    private void processData(int data) {
        /* 
         * Multi-line comment
         * Should not count as statements
         */
        
        // Single line comment - should not count
        
        if (data > 0) {                // Statement 25: if
            System.out.println("Positive: " + data); // Statement 26: method call
        }
        
        // Empty statement below (just semicolon) - should not count
        ;
        
        return; // Statement 27: return (even though void, explicit return may count)
    }
    
    public String getField1() {
        return field1;                 // Statement 28: return
    }
    
    public void setField1(String field1) {
        this.field1 = field1;          // Statement 29: assignment
    }
    
    // Expected NCSS: Approximately 25-30 depending on:
    // - Whether field declarations count as statements
    // - Whether variable declarations without assignment count
    // - How control structures are counted (header vs body)
    // - Whether explicit returns in void methods count
}

/**
 * Class with minimal statements (test baseline)
 */
class NCSS_MinimalStatements_TestClass {
    // No field initializations
    private String data;
    private int value;
    
    // Empty constructor
    public NCSS_MinimalStatements_TestClass() {
        // No statements in constructor
    }
    
    // Single statement method
    public void singleStatement() {
        System.out.println("single"); // Statement 1: method call
    }
    
    // Getter with single return
    public String getData() {
        return data;                   // Statement 2: return
    }
    
    // Setter with single assignment
    public void setData(String data) {
        this.data = data;              // Statement 3: assignment
    }
    
    // Expected NCSS: 3 (minimal executable statements)
}

/**
 * Class with only comments and empty methods (should have NCSS near 0)
 */
class NCSS_CommentsOnly_TestClass {
    /*
     * This entire class is mostly comments
     * and empty methods to test NCSS filtering
     */
    
    // Field comment
    private String field; // No initialization
    
    // Constructor comment
    public NCSS_CommentsOnly_TestClass() {
        // Empty constructor body
        // Just comments here
    }
    
    /*
     * Method with only comments
     */
    public void emptyMethod() {
        // This method has no executable statements
        
        /* Another comment block */
        
        // More comments
    }
    
    /**
     * Javadoc comment method
     * @param input parameter description
     */
    public void javadocMethod(String input) {
        // Parameter not used
        // No executable statements
    }
    
    // Expected NCSS: 0 or very close to 0 (no executable statements)
}

/**
 * Class with complex control structures
 */
class NCSS_ComplexControlFlow_TestClass {
    public void complexMethod() {
        int x = 5;                     // Statement 1: declaration with assignment
        
        // Nested if statements
        if (x > 0) {                   // Statement 2: if
            if (x > 3) {               // Statement 3: nested if
                x = x * 2;             // Statement 4: assignment
            }
        }
        
        // Nested loops
        for (int i = 0; i < x; i++) {  // Statement 5: for loop
            for (int j = 0; j < 2; j++) { // Statement 6: nested for loop
                System.out.println(i + "," + j); // Statement 7: method call
            }
        }
        
        // Complex conditional
        int result = (x > 10) ? x * 2 : x / 2; // Statement 8: ternary assignment
        
        // Method chain
        String text = "hello"          // Statement 9: method chain assignment
                .toUpperCase()
                .substring(0, 3);
                
        // Lambda expression (may or may not count as single statement)
        java.util.Arrays.asList(1, 2, 3)
                .forEach(num -> System.out.println(num)); // Statement 10: lambda expression
    }
    
    // Expected NCSS: ~10 statements depending on how complex structures are counted
}

/**
 * Class with exception handling
 */
class NCSS_ExceptionHandling_TestClass {
    public void methodWithExceptions() {
        String data = "test";          // Statement 1: declaration with assignment
        
        try {                          // try block
            Integer value = Integer.parseInt(data); // Statement 2: assignment in try
            System.out.println(value); // Statement 3: method call in try
            
            if (value > 100) {         // Statement 4: if in try
                throw new IllegalArgumentException("Too large"); // Statement 5: throw
            }
            
        } catch (NumberFormatException e1) { // catch block 1
            System.err.println("Number format error"); // Statement 6: method call in catch
            data = "0";                // Statement 7: assignment in catch
            
        } catch (IllegalArgumentException e2) { // catch block 2
            System.err.println("Illegal argument"); // Statement 8: method call in catch
            
        } finally {                    // finally block
            System.out.println("Cleanup"); // Statement 9: method call in finally
            data = null;               // Statement 10: assignment in finally
        }
    }
    
    // Expected NCSS: ~10 statements including exception handling
}

/**
 * Class with various declaration patterns
 */
class NCSS_VariableDeclarations_TestClass {
    // Class fields with different initialization patterns
    private String field1 = "initialized";
    private String field2;
    private int field3 = 42, field4 = 84; // Multiple declarations
    
    public void declarationPatterns() {
        // Various local variable declaration patterns
        String local1;                 // Declaration only
        String local2 = "value";       // Declaration with assignment
        int a = 1, b = 2, c = 3;      // Multiple declarations with assignment
        
        // Array declarations
        int[] array1 = new int[5];     // Array declaration with new
        int[] array2 = {1, 2, 3, 4};  // Array declaration with initializer
        String[] array3;               // Array declaration only
        
        // Assignment after declaration
        local1 = "assigned later";     // Assignment to previously declared variable
        array3 = new String[10];       // Assignment to previously declared array
        
        // For loop with declaration
        for (int i = 0; i < 5; i++) {  // Loop with declaration
            System.out.println(array1[i]); // Method call in loop
        }
        
        // Enhanced for loop
        for (int item : array2) {      // Enhanced for with declaration
            System.out.println(item);  // Method call in enhanced for
        }
    }
    
    // Expected NCSS: Variable depending on how declarations are counted
}

/**
 * Class with modern Java features
 */
class NCSS_ModernJava_TestClass {
    public void modernFeatures() {
        // Stream operations
        java.util.Arrays.asList(1, 2, 3, 4, 5) // Statement 1: stream chain
                .stream()
                .filter(n -> n > 2)
                .map(n -> n * 2)
                .forEach(System.out::println);
                
        // Optional usage
        java.util.Optional<String> optional = java.util.Optional.of("test"); // Statement 2: optional creation
        optional.ifPresent(System.out::println); // Statement 3: optional operation
        
        // Lambda with block
        Runnable task = () -> {        // Statement 4: lambda assignment
            System.out.println("Lambda task"); // Statement inside lambda - may or may not count
            String data = "lambda data";        // Statement inside lambda - may or may not count
        };
        
        // Method reference
        java.util.Arrays.asList("a", "b", "c") // Statement 5: method reference
                .forEach(System.out::println);
                
        // Try-with-resources
        try (java.io.StringReader reader = new java.io.StringReader("data")) { // Statement 6: try-with-resources
            reader.read();             // Statement 7: method call in try-with-resources
        } catch (Exception e) {        // catch block
            e.printStackTrace();       // Statement 8: method call in catch
        }
    }
    
    // Expected NCSS: Variable depending on how modern Java features are counted
}