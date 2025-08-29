package com.verification.cohesion;

import java.util.List;
import java.util.ArrayList;

/**
 * Test cases for LCOM (Lack of Cohesion of Methods) metric verification.
 * LCOM measures how methods of a class are related to each other through shared field usage.
 * Methods are linked if they access at least one common field.
 * LCOM is the number of disconnected components of methods.
 */

/**
 * Class with perfect cohesion - all methods use the same field
 * Expected LCOM: 1 (all methods form one connected component)
 */
class LCOM_PerfectCohesion {
    private String sharedField;

    public LCOM_PerfectCohesion() {
        this.sharedField = "initial";
    }

    public void setSharedField(String value) {
        this.sharedField = value;  // Uses sharedField
    }

    public String getSharedField() {
        return this.sharedField;   // Uses sharedField
    }

    public void processSharedField() {
        if (this.sharedField != null) {  // Uses sharedField
            this.sharedField = this.sharedField.toUpperCase();
        }
    }

    public boolean isSharedFieldEmpty() {
        return this.sharedField == null || this.sharedField.isEmpty(); // Uses sharedField
    }

    // Expected LCOM: 1 (all 5 methods use sharedField, forming one component)
}

/**
 * Class with no cohesion - each method uses different fields
 * Expected LCOM: equal to number of methods using fields
 */
class LCOM_NoCohesion {
    private String field1;
    private int field2;
    private boolean field3;
    private List<String> field4;

    public LCOM_NoCohesion() {
        this.field1 = "default";
        this.field2 = 0;
        this.field3 = false;
        this.field4 = new ArrayList<>();
    }

    public void methodUsingField1() {
        this.field1 = "modified";  // Uses only field1
    }

    public void methodUsingField2() {
        this.field2 = 42;          // Uses only field2
    }

    public void methodUsingField3() {
        this.field3 = true;        // Uses only field3
    }

    public void methodUsingField4() {
        this.field4.add("item");   // Uses only field4
    }

    public void methodUsingNoFields() {
        // This method doesn't use any fields
        System.out.println("No field access");
    }

    // Expected LCOM: 4 (methods using field1, field2, field3, field4 are disconnected)
    // methodUsingNoFields doesn't contribute to LCOM as it uses no fields
}

/**
 * Class with partial cohesion - some methods share fields
 * Expected LCOM: 2 (two disconnected components)
 */
class LCOM_PartialCohesion {
    private String name;
    private String description;
    private int count;
    private boolean flag;

    public LCOM_PartialCohesion() {
        this.name = "default";
        this.description = "default description";
        this.count = 0;
        this.flag = false;
    }

    // Component 1: Methods sharing name and description fields
    public void setName(String name) {
        this.name = name;              // Uses name
    }

    public String getName() {
        return this.name;              // Uses name
    }

    public void setDescription(String description) {
        this.description = description; // Uses description
    }

    public String getFullInfo() {
        return this.name + ": " + this.description; // Uses both name and description
    }

    // Component 2: Methods sharing count and flag fields
    public void incrementCount() {
        this.count++;                  // Uses count
        this.flag = true;              // Uses flag
    }

    public int getCount() {
        return this.count;             // Uses count
    }

    public boolean isEnabled() {
        return this.flag;              // Uses flag
    }

    public void resetCounters() {
        this.count = 0;                // Uses count
        this.flag = false;             // Uses flag
    }

    // Expected LCOM: 2 
    // Component 1: {setName, getName, setDescription, getFullInfo} - connected via name/description
    // Component 2: {incrementCount, getCount, isEnabled, resetCounters} - connected via count/flag
}

/**
 * Class with complex field sharing patterns
 * Expected LCOM: depends on connection analysis
 */
class LCOM_ComplexSharing {
    private String fieldA;
    private String fieldB;
    private String fieldC;
    private int fieldD;

    public LCOM_ComplexSharing() {
        this.fieldA = "A";
        this.fieldB = "B";
        this.fieldC = "C";
        this.fieldD = 0;
    }

    // Method 1: uses fieldA
    public void method1() {
        this.fieldA = "modified A";
    }

    // Method 2: uses fieldA and fieldB (connects to method1 via fieldA)
    public void method2() {
        this.fieldA = this.fieldA + this.fieldB;
    }

    // Method 3: uses fieldB and fieldC (connects to method2 via fieldB)
    public void method3() {
        this.fieldB = this.fieldC;
    }

    // Method 4: uses only fieldD (disconnected)
    public void method4() {
        this.fieldD = 100;
    }

    // Method 5: uses fieldC (connects to method3 via fieldC)
    public void method5() {
        this.fieldC = "new C";
    }

    // Method 6: no field access (doesn't contribute to LCOM)
    public void method6() {
        System.out.println("No fields used");
    }

    // Expected LCOM: 2
    // Component 1: {method1, method2, method3, method5} - connected through field sharing chain
    // Component 2: {method4} - isolated (uses only fieldD)
    // method6 is ignored (no field access)
}

/**
 * Class with only static methods and no instance fields
 * Expected LCOM: 0 (no instance methods using fields)
 */
class LCOM_StaticOnly {
    private static final String CONSTANT = "constant";

    public static void staticMethod1() {
        System.out.println("Static method 1");
    }

    public static void staticMethod2() {
        System.out.println("Static method 2");
    }

    public static String getConstant() {
        return CONSTANT;  // Uses static field, not instance field
    }

    // Expected LCOM: 0 (no instance methods using instance fields)
}

/**
 * Class with methods that don't access fields
 * Expected LCOM: 0 (no methods use fields)
 */
class LCOM_NoFieldAccess {
    private String field1;
    private int field2;

    public LCOM_NoFieldAccess() {
        this.field1 = "default";
        this.field2 = 0;
    }

    public void method1() {
        // No field access
        System.out.println("Method 1");
    }

    public void method2() {
        // No field access
        System.out.println("Method 2");
    }

    public int calculate(int a, int b) {
        // No field access, only parameter usage
        return a + b;
    }

    // Expected LCOM: 0 (no methods access instance fields)
}

/**
 * Empty class for baseline
 * Expected LCOM: 0 (no methods, no fields)
 */
class LCOM_Empty {
    // Expected LCOM: 0
}

/**
 * Class with inheritance to test field access from parent
 */
class LCOM_ParentClass {
    protected String parentField;
    private String privateParentField;

    public LCOM_ParentClass() {
        this.parentField = "parent";
        this.privateParentField = "private";
    }

    public void parentMethod() {
        this.parentField = "modified parent";
    }
}

class LCOM_ChildClass extends LCOM_ParentClass {
    private String childField;

    public LCOM_ChildClass() {
        super();
        this.childField = "child";
    }

    // Method 1: uses inherited field
    public void useParentField() {
        this.parentField = "child modified parent";  // Uses inherited field
    }

    // Method 2: uses child field
    public void useChildField() {
        this.childField = "modified child";          // Uses own field
    }

    // Method 3: uses both inherited and own field
    public void useBothFields() {
        this.childField = this.parentField + " child"; // Uses both fields
    }

    // Expected LCOM: 1 (all three methods are connected through field sharing)
    // useParentField and useBothFields share parentField
    // useChildField and useBothFields share childField
    // Therefore all methods form one connected component
}

/**
 * Class with read-only field access patterns
 */
class LCOM_ReadOnlyAccess {
    private String field1;
    private String field2;
    private String field3;

    public LCOM_ReadOnlyAccess() {
        this.field1 = "value1";
        this.field2 = "value2";
        this.field3 = "value3";
    }

    // Methods that only read fields
    public String getField1() {
        return this.field1;  // Reads field1
    }

    public String getField2() {
        return this.field2;  // Reads field2
    }

    public String getField1And2() {
        return this.field1 + this.field2;  // Reads field1 and field2
    }

    public String getField2And3() {
        return this.field2 + this.field3;  // Reads field2 and field3
    }

    // Expected LCOM: 1 (all methods connected through field2 as the bridge)
    // getField1 and getField1And2 share field1
    // getField1And2 and getField2And3 share field2
    // getField2 and getField2And3 share field2
    // All methods form one connected component through field sharing
}

/**
 * Class with mixed read/write access patterns
 */
class LCOM_MixedAccess {
    private String status;
    private int counter;
    private boolean initialized;

    public LCOM_MixedAccess() {
        this.status = "created";
        this.counter = 0;
        this.initialized = false;
    }

    // Component 1: Methods working with status
    public void setStatus(String status) {
        this.status = status;          // Writes to status
    }

    public String getStatus() {
        return this.status;            // Reads status
    }

    // Component 2: Methods working with counter and initialized
    public void initialize() {
        this.counter = 1;              // Writes to counter
        this.initialized = true;       // Writes to initialized
    }

    public boolean isReady() {
        return this.initialized && this.counter > 0; // Reads both counter and initialized
    }

    public void reset() {
        this.counter = 0;              // Writes to counter
        this.initialized = false;      // Writes to initialized
    }

    // Expected LCOM: 2
    // Component 1: {setStatus, getStatus} - connected via status field
    // Component 2: {initialize, isReady, reset} - connected via counter and initialized fields
}