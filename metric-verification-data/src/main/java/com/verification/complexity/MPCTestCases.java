package com.verification.coupling;

/**
 * Test cases for MPC (Message Passing Coupling) metric verification.
 * MPC counts the number of method calls made by a class to methods of other classes.
 */

/**
 * Helper classes for MPC testing
 */
class ExternalService {
    public void process() {
        System.out.println("External service processing");
    }
    
    public String getData() {
        return "external data";
    }
    
    public void validate(String input) {
        System.out.println("Validating: " + input);
    }
    
    public static void staticMethod() {
        System.out.println("Static external method");
    }
}

class AnotherService {
    public void execute() {
        System.out.println("Another service executing");
    }
    
    public int calculate(int value) {
        return value * 2;
    }
}

class UtilityClass {
    public static String format(String input) {
        return input.toUpperCase();
    }
    
    public static void log(String message) {
        System.out.println("LOG: " + message);
    }
}

/**
 * Primary test class with multiple types of method calls
 * Expected MPC: Count all method calls to other classes
 */
class MPC_TestClass {
    private ExternalService externalService;
    private AnotherService anotherService;
    private String localField;
    private int count;

    public MPC_TestClass() {
        this.externalService = new ExternalService();
        this.anotherService = new AnotherService();
        this.localField = "local";
        this.count = 0;
    }

    public void performOperations() {
        // Method call to ExternalService (should count toward MPC)
        externalService.process();
        
        // Another method call to ExternalService (should count toward MPC)
        String data = externalService.getData();
        
        // Method call to AnotherService (should count toward MPC)
        anotherService.execute();
        
        // Method call with parameter to ExternalService (should count toward MPC)
        externalService.validate(data);
        
        // Method call with return value to AnotherService (should count toward MPC)
        int result = anotherService.calculate(42);
        
        // Static method call to UtilityClass (should count toward MPC)
        String formatted = UtilityClass.format(data);
        
        // Another static method call to UtilityClass (should count toward MPC)
        UtilityClass.log("Operation completed");
        
        // Static method call to ExternalService (should count toward MPC)
        ExternalService.staticMethod();
        
        // Method call to standard library (may or may not count depending on implementation)
        System.out.println("Result: " + result);
        
        // Local field access (should NOT count toward MPC)
        this.count++;
        
        // Local method call (should NOT count toward MPC)
        this.localMethod();
    }
    
    public void chainingCalls() {
        // Chained method calls (each call should count separately)
        String result = externalService.getData()
                .toLowerCase() // String method call (may or may not count)
                .trim();       // String method call (may or may not count)
        
        // Method call result used in another call
        anotherService.execute();
        int value = anotherService.calculate(10);
        externalService.validate(String.valueOf(value)); // String.valueOf may count
    }
    
    public void conditionalCalls() {
        if (count > 0) {
            // Conditional method call (should count toward MPC)
            externalService.process();
        }
        
        // Loop with method calls
        for (int i = 0; i < 3; i++) {
            // Method call in loop (should count toward MPC, possibly multiple times)
            anotherService.execute();
        }
    }
    
    // Private local method (calls to this should NOT count toward MPC)
    private void localMethod() {
        System.out.println("Local method: " + localField);
    }
    
    // Public local method (calls to this should NOT count toward MPC)
    public void publicLocalMethod() {
        System.out.println("Public local method");
    }
    
    // Expected MPC: Approximately 8-15 depending on:
    // - Whether standard library calls (System.out.println, String methods) count
    // - Whether method calls in loops count multiple times or once
    // - Whether chained method calls count individually
    // Core external calls: externalService.process(), getData(), validate(), 
    //                     anotherService.execute(), calculate(), 
    //                     ExternalService.staticMethod(), UtilityClass.format(), log()
}

/**
 * Class with no external method calls (MPC should be 0)
 */
class NoExternalCalls_TestClass {
    private String data;
    private int value;

    public NoExternalCalls_TestClass() {
        this.data = "test";
        this.value = 42;
    }

    public void selfContainedMethod() {
        // Only local field access and local method calls
        this.data = "updated";
        this.value++;
        this.helperMethod();
        String result = this.processLocally();
        this.setData(result);
    }
    
    public String processLocally() {
        return this.data + "_processed";
    }
    
    private void helperMethod() {
        this.value *= 2;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getData() {
        return this.data;
    }
    
    // Expected MPC: 0 (no external method calls)
}

/**
 * Class with only static method calls to external classes
 */
class StaticCalls_TestClass {
    public void performStaticCalls() {
        // Static method calls to external classes (should count toward MPC)
        UtilityClass.format("test");
        UtilityClass.log("message");
        ExternalService.staticMethod();
        
        // Static calls to standard library (may or may not count)
        Math.abs(-5);
        Math.max(1, 2);
        Integer.parseInt("123");
        String.valueOf(456);
    }
    
    // Expected MPC: 3-7 depending on whether standard library static calls count
}

/**
 * Class that creates instances and immediately calls methods
 */
class InstantiationCalls_TestClass {
    public void createAndCall() {
        // Create instance and call method (both instantiation and method call may count)
        ExternalService service = new ExternalService();
        service.process();
        
        // Anonymous instance method call (should count toward MPC)
        new AnotherService().execute();
        
        // Chained instantiation and method call
        new ExternalService().getData();
        
        // Multiple calls on same instance
        AnotherService another = new AnotherService();
        another.execute();
        another.calculate(10);
    }
    
    // Expected MPC: 5 (method calls only, not counting constructors)
    // Or more if constructor calls also count toward MPC
}

/**
 * Class with method calls in different contexts
 */
class VariousContexts_TestClass {
    private ExternalService service = new ExternalService();
    
    public void methodCallsInDifferentContexts() {
        // Method call in assignment
        String data = service.getData();
        
        // Method call in conditional
        if (service.getData().length() > 0) {
            service.process();
        }
        
        // Method call in return statement
        returnExternalData();
        
        // Method call in parameter
        processData(service.getData());
        
        // Method call in try-catch
        try {
            service.validate("test");
        } catch (Exception e) {
            UtilityClass.log("Error occurred");
        }
    }
    
    public String returnExternalData() {
        return service.getData(); // Method call in return
    }
    
    public void processData(String data) {
        UtilityClass.log("Processing: " + data);
    }
    
    // Expected MPC: Approximately 6-8 method calls
}

/**
 * Class with recursive and complex call patterns
 */
class ComplexCalls_TestClass {
    private ExternalService service = new ExternalService();
    
    public void complexCallPatterns() {
        // Nested method calls
        service.validate(new AnotherService().calculate(10) + "");
        
        // Method call with method call as parameter
        UtilityClass.log(service.getData());
        
        // Multiple services interaction
        ExternalService service1 = new ExternalService();
        AnotherService service2 = new AnotherService();
        
        service1.validate(String.valueOf(service2.calculate(5)));
        
        // Array/Collection operations (if they count)
        String[] array = {service.getData(), "test"};
        
        // Lambda expressions with method calls (may or may not count)
        java.util.Arrays.stream(array)
                .forEach(UtilityClass::log);
    }
    
    // Expected MPC: Variable depending on how nested calls and lambdas are counted
}

/**
 * Class with inheritance to test method call attribution
 */
class InheritanceBase {
    protected ExternalService baseService = new ExternalService();
    
    public void baseMethod() {
        baseService.process();
    }
}

class InheritanceDerived extends InheritanceBase {
    private AnotherService derivedService = new AnotherService();
    
    public void derivedMethod() {
        // Call to inherited field's method (should count toward derived class MPC)
        baseService.getData();
        
        // Call to own field's method (should count toward derived class MPC)
        derivedService.execute();
        
        // Call to inherited method (should NOT count toward MPC - internal call)
        this.baseMethod();
        
        // Call to parent method explicitly (should NOT count toward MPC - internal call)
        super.baseMethod();
    }
    
    // Expected MPC: 2 (baseService.getData(), derivedService.execute())
    // Calls to inherited methods should not count as external method calls
}