package com.verification.coupling;

/**
 * Test cases for ATFD (Access To Foreign Data) metric verification.
 * ATFD measures the number of different classes whose data is accessed directly by the analyzed class.
 */

/**
 * Helper classes for ATFD testing
 */
class ForeignDataSource {
    public String publicField = "public data";
    protected String protectedField = "protected data";
    private String privateField = "private data";
    
    public String getPublicData() {
        return publicField;
    }
    
    public void setPublicData(String data) {
        this.publicField = data;
    }
    
    protected String getProtectedData() {
        return protectedField;
    }
    
    private String getPrivateData() {
        return privateField;
    }
    
    public static String staticField = "static data";
    
    public static String getStaticData() {
        return staticField;
    }
}

class AnotherForeignSource {
    public int numberField = 42;
    public boolean flagField = true;
    
    public int getNumber() {
        return numberField;
    }
    
    public boolean isFlag() {
        return flagField;
    }
    
    public void performAction() {
        System.out.println("Action performed");
    }
}

class ThirdForeignSource {
    public String data = "third source";
    
    public String getData() {
        return data;
    }
    
    public void updateData(String newData) {
        this.data = newData;
    }
}

/**
 * Primary test class with various types of foreign data access
 * Expected ATFD: Count unique external classes whose data is accessed
 */
class ATFD_TestClass {
    private ForeignDataSource foreignSource;
    private AnotherForeignSource anotherSource;
    private String localField;

    public ATFD_TestClass() {
        this.foreignSource = new ForeignDataSource();
        this.anotherSource = new AnotherForeignSource();
        this.localField = "local";
    }

    public void accessForeignData() {
        // Direct field access to ForeignDataSource (should count toward ATFD)
        String data1 = foreignSource.publicField;
        foreignSource.publicField = "modified";
        
        // Method access to ForeignDataSource (should count toward ATFD)
        String data2 = foreignSource.getPublicData();
        foreignSource.setPublicData("new data");
        
        // Direct field access to AnotherForeignSource (should count toward ATFD)
        int number = anotherSource.numberField;
        boolean flag = anotherSource.flagField;
        
        // Method access to AnotherForeignSource (should count toward ATFD)
        int retrievedNumber = anotherSource.getNumber();
        boolean retrievedFlag = anotherSource.isFlag();
        anotherSource.performAction();
        
        // Multiple accesses to same foreign class should count as 1
        foreignSource.getProtectedData(); // Still ForeignDataSource
        
        // Local field access (should NOT count toward ATFD)
        this.localField = "updated";
        String localData = this.localField;
        
        // Local method call (should NOT count toward ATFD)
        this.localMethod();
    }
    
    public void accessStaticForeignData() {
        // Static field access (may or may not count depending on implementation)
        String staticData = ForeignDataSource.staticField;
        ForeignDataSource.staticField = "modified static";
        
        // Static method access (may or may not count depending on implementation)
        String retrievedStatic = ForeignDataSource.getStaticData();
    }
    
    public void accessThroughParameters(ThirdForeignSource external) {
        // Direct access to parameter object's data (should count toward ATFD)
        String paramData = external.data;
        external.data = "modified param";
        
        // Method call on parameter object (should count toward ATFD)
        String retrievedParam = external.getData();
        external.updateData("updated param");
    }
    
    public void accessThroughLocalVariables() {
        // Create local instance and access its data
        ThirdForeignSource localInstance = new ThirdForeignSource();
        
        // Access to local instance data (should count toward ATFD)
        String localInstanceData = localInstance.data;
        localInstance.data = "modified local instance";
        
        // Method call on local instance (should count toward ATFD)
        String retrievedLocal = localInstance.getData();
        localInstance.updateData("updated local");
    }
    
    public void accessThroughChaining() {
        // Access foreign data through method chaining
        // Each access should potentially count toward ATFD
        foreignSource.getPublicData(); // ForeignDataSource
        anotherSource.getNumber();     // AnotherForeignSource
        
        // Chained access (behavior may vary)
        // foreignSource.getPublicData().length(); // String method - may or may not count
    }
    
    private void localMethod() {
        // Local method that also accesses foreign data
        String data = foreignSource.getPublicData(); // Should still count toward ATFD
        anotherSource.performAction();               // Should still count toward ATFD
    }
    
    // Expected ATFD: 3 unique external classes (ForeignDataSource, AnotherForeignSource, ThirdForeignSource)
    // Note: Static access and standard library access behavior may vary by implementation
}

/**
 * Class with no foreign data access (ATFD should be 0)
 */
class NoForeignAccess_TestClass {
    private String localField;
    private int localNumber;
    
    public NoForeignAccess_TestClass() {
        this.localField = "local";
        this.localNumber = 42;
    }
    
    public void selfContainedOperations() {
        // Only local field access (should NOT count toward ATFD)
        this.localField = "updated";
        this.localNumber++;
        
        // Only local method calls (should NOT count toward ATFD)
        this.helperMethod();
        String result = this.processData();
        
        // Only access to own fields and methods
        this.setLocalField("new value");
        String data = this.getLocalField();
    }
    
    private void helperMethod() {
        this.localNumber *= 2;
    }
    
    private String processData() {
        return this.localField + "_processed";
    }
    
    public void setLocalField(String field) {
        this.localField = field;
    }
    
    public String getLocalField() {
        return this.localField;
    }
    
    // Expected ATFD: 0 (no foreign data access)
}

/**
 * Class that only accesses one foreign class (ATFD should be 1)
 */
class SingleForeignAccess_TestClass {
    private ForeignDataSource singleSource;
    
    public SingleForeignAccess_TestClass() {
        this.singleSource = new ForeignDataSource();
    }
    
    public void accessSingleForeign() {
        // Multiple accesses to same foreign class should count as 1
        String data1 = singleSource.publicField;
        String data2 = singleSource.getPublicData();
        singleSource.setPublicData("modified");
        String data3 = singleSource.getProtectedData();
        
        // All accesses are to ForeignDataSource - should count as 1
    }
    
    // Expected ATFD: 1 (only ForeignDataSource accessed)
}

/**
 * Class with inheritance to test foreign data access attribution
 */
class InheritanceBase3 {
    protected ForeignDataSource baseSource = new ForeignDataSource();
    
    public void baseAccessForeign() {
        // Access foreign data in base class
        String data = baseSource.getPublicData();
    }
}

class InheritanceDerived1 extends InheritanceBase3 {
    private AnotherForeignSource derivedSource = new AnotherForeignSource();
    
    public void derivedAccessForeign() {
        // Access foreign data through inherited field
        String baseData = baseSource.getPublicData(); // ForeignDataSource access
        
        // Access foreign data through own field
        int number = derivedSource.getNumber(); // AnotherForeignSource access
        
        // Call inherited method (should NOT count as foreign access)
        this.baseAccessForeign();
        
        // Call parent method explicitly (should NOT count as foreign access)
        super.baseAccessForeign();
    }
    
    // Expected ATFD: 2 (ForeignDataSource, AnotherForeignSource)
    // Access to inherited methods should not count as foreign data access
}

/**
 * Class with standard library access to test filtering
 */
class StandardLibraryAccess_TestClass {
    private String stringField = "test";
    private java.util.List<String> listField = new java.util.ArrayList<>();
    
    public void accessStandardLibrary() {
        // Access to standard library objects (may or may not count)
        int length = stringField.length();
        String upper = stringField.toUpperCase();
        
        // List operations (may or may not count)
        listField.add("item");
        int size = listField.size();
        
        // System operations (may or may not count)
        System.out.println("output");
        System.currentTimeMillis();
        
        // Math operations (may or may not count)
        double result = Math.sqrt(25.0);
        int max = Math.max(1, 2);
    }
    
    // Expected ATFD: 0 (if standard library is filtered) or variable (if included)
}

/**
 * Class with complex access patterns
 */
class ComplexAccess_TestClass {
    private ForeignDataSource primary = new ForeignDataSource();
    
    public void complexAccessPatterns() {
        // Nested field access
        String data = primary.getPublicData();
        
        // Access through array (if applicable)
        ForeignDataSource[] sources = {primary, new ForeignDataSource()};
        String arrayData = sources[0].getPublicData(); // Still ForeignDataSource
        
        // Access in conditional
        if (primary.publicField != null) {
            primary.setPublicData("conditional");
        }
        
        // Access in loop
        for (int i = 0; i < 3; i++) {
            primary.getPublicData(); // Multiple calls, same class
        }
        
        // Access in try-catch
        try {
            primary.getPublicData();
        } catch (Exception e) {
            // Exception handling
        }
    }
    
    public String returnForeignData() {
        // Return foreign data
        return primary.getPublicData();
    }
    
    public void passForeignData() {
        // Pass foreign data as parameter
        processData(primary.getPublicData());
    }
    
    private void processData(String data) {
        System.out.println("Processing: " + data);
    }
    
    // Expected ATFD: 1 (only ForeignDataSource accessed in various ways)
}

/**
 * Class with anonymous and lambda access patterns
 */
class ModernJavaAccess_TestClass {
    private ForeignDataSource source = new ForeignDataSource();
    
    public void modernAccessPatterns() {
        // Access in lambda (may or may not count depending on implementation)
        java.util.Arrays.asList("a", "b", "c")
                .forEach(item -> {
                    String data = source.getPublicData(); // Foreign access in lambda
                });
        
        // Access in anonymous inner class
        Runnable task = new Runnable() {
            @Override
            public void run() {
                String data = source.getPublicData(); // Foreign access in anonymous class
            }
        };
        
        // Method reference (may or may not count)
        java.util.Arrays.asList("a", "b", "c")
                .forEach(System.out::println);
    }
    
    // Expected ATFD: 1+ depending on how lambda and anonymous class access is handled
}