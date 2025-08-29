package com.verification.complexity;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Test cases for NOA (Number of Attributes) metric verification.
 * NOA simply counts the total number of fields (attributes) declared in a class.
 * The key question is whether inherited fields should be included.
 */

/**
 * Simple class with basic field types
 * Expected NOA: 4 (declared fields only)
 */
class NOA_BasicFields {
    private String name;           // Field 1
    private int age;               // Field 2
    private boolean active;        // Field 3
    private double salary;         // Field 4

    public NOA_BasicFields() {
        this.name = "default";
        this.age = 0;
        this.active = false;
        this.salary = 0.0;
    }

    // Methods for field access
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Expected NOA: 4 (name, age, active, salary)
}

/**
 * Empty class with no fields
 * Expected NOA: 0
 */
class NOA_EmptyClass {
    public NOA_EmptyClass() {
        // No fields declared
    }

    public void someMethod() {
        System.out.println("Method with no field access");
    }

    // Expected NOA: 0 (no fields)
}

/**
 * Class with only constants and static fields
 * Expected NOA: depends on whether static fields are counted
 */
class NOA_ConstantsOnly {
    public static final String CONSTANT1 = "constant1";     // Static final field 1
    public static final int CONSTANT2 = 42;                 // Static final field 2
    private static String staticField = "static";           // Static field 3

    public NOA_ConstantsOnly() {
        // No instance fields
    }

    public static String getStaticField() {
        return staticField;
    }

    // Expected NOA: 3 if static fields are counted, 0 if only instance fields are counted
}

/**
 * Parent class for inheritance testing
 */
class NOA_ParentClass {
    protected String parentField1;     // Inherited field 1
    protected int parentField2;        // Inherited field 2
    private String privateParentField; // Private inherited field (not accessible)

    public NOA_ParentClass() {
        this.parentField1 = "parent1";
        this.parentField2 = 100;
        this.privateParentField = "private";
    }

    // Expected NOA: 3 (parentField1, parentField2, privateParentField)
}

/**
 * Child class to test inheritance field counting
 * Key test: Should NOA include inherited fields?
 */
class NOA_InheritanceChild extends NOA_ParentClass {
    private String childField1;        // Declared field 1
    private String childField2;        // Declared field 2

    public NOA_InheritanceChild() {
        super();
        this.childField1 = "child1";
        this.childField2 = "child2";
    }

    // Expected NOA: 
    // - If declared only: 2 (childField1, childField2)
    // - If including inherited: 4 (childField1, childField2, parentField1, parentField2)
    // - If including all inherited: 5 (including privateParentField)
}

/**
 * Class with mixed field types and visibility modifiers
 */
class NOA_MixedFieldTypes {
    public String publicField;         // Field 1 - public
    protected int protectedField;      // Field 2 - protected
    private boolean privateField;      // Field 3 - private
    String packageField;               // Field 4 - package-private
    final String finalField;           // Field 5 - final instance field
    static String staticField;         // Field 6 - static (may or may not count)

    public NOA_MixedFieldTypes() {
        this.publicField = "public";
        this.protectedField = 1;
        this.privateField = true;
        this.packageField = "package";
        this.finalField = "final";
        staticField = "static";
    }

    // Expected NOA: 
    // - If only instance fields: 5 (publicField, protectedField, privateField, packageField, finalField)
    // - If including static: 6 (all above + staticField)
}

/**
 * Class with generic and collection fields
 */
class NOA_GenericFields {
    private List<String> stringList;        // Field 1 - generic collection
    private Map<String, Integer> dataMap;   // Field 2 - generic map
    private String[] stringArray;           // Field 3 - array
    private int[] intArray;                 // Field 4 - primitive array
    private List<? extends Number> wildcard; // Field 5 - wildcard generic

    public NOA_GenericFields() {
        this.stringList = List.of();
        this.dataMap = Map.of();
        this.stringArray = new String[0];
        this.intArray = new int[0];
        this.wildcard = List.of();
    }

    // Expected NOA: 5 (stringList, dataMap, stringArray, intArray, wildcard)
}

/**
 * Class with nested class fields
 */
class NOA_NestedClassFields {
    private InnerData innerData;       // Field 1 - inner class type
    private String regularField;       // Field 2 - regular field

    public NOA_NestedClassFields() {
        this.innerData = new InnerData();
        this.regularField = "regular";
    }

    // Inner class
    private static class InnerData {
        private String data;           // This field belongs to InnerData, not NOA_NestedClassFields

        public InnerData() {
            this.data = "inner";
        }
    }

    // Expected NOA: 2 (innerData, regularField)
    // Inner class fields don't count toward outer class NOA
}

/**
 * Interface with fields (constants)
 */
interface NOA_InterfaceWithFields {
    String INTERFACE_CONSTANT1 = "constant1";  // Implicitly public static final
    int INTERFACE_CONSTANT2 = 42;              // Implicitly public static final
}

/**
 * Class implementing interface with constants
 */
class NOA_ImplementsInterface implements NOA_InterfaceWithFields, Serializable {
    private String implementationField1;   // Field 1
    private String implementationField2;   // Field 2

    public NOA_ImplementsInterface() {
        this.implementationField1 = "impl1";
        this.implementationField2 = "impl2";
    }

    // Expected NOA: 
    // - If only declared: 2 (implementationField1, implementationField2)
    // - If including interface constants: 4 (+ INTERFACE_CONSTANT1, INTERFACE_CONSTANT2)
}

/**
 * Abstract class with fields
 */
abstract class NOA_AbstractParent {
    protected String abstractField1;   // Field 1
    protected String abstractField2;   // Field 2

    public NOA_AbstractParent() {
        this.abstractField1 = "abstract1";
        this.abstractField2 = "abstract2";
    }

    public abstract void abstractMethod();

    // Expected NOA: 2 (abstractField1, abstractField2)
}

/**
 * Concrete class extending abstract class
 */
class NOA_ExtendsAbstract extends NOA_AbstractParent {
    private String concreteField1;     // Field 1
    private String concreteField2;     // Field 2

    public NOA_ExtendsAbstract() {
        super();
        this.concreteField1 = "concrete1";
        this.concreteField2 = "concrete2";
    }

    @Override
    public void abstractMethod() {
        System.out.println("Abstract method implementation");
    }

    // Expected NOA:
    // - If declared only: 2 (concreteField1, concreteField2)
    // - If including inherited: 4 (+ abstractField1, abstractField2)
}

/**
 * Class with complex inheritance chain
 */
class NOA_GrandParent {
    protected String grandParentField; // Field 1

    public NOA_GrandParent() {
        this.grandParentField = "grandparent";
    }

    // Expected NOA: 1 (grandParentField)
}

class NOA_Parent extends NOA_GrandParent {
    protected String parentField;      // Field 1

    public NOA_Parent() {
        super();
        this.parentField = "parent";
    }

    // Expected NOA:
    // - If declared only: 1 (parentField)
    // - If including inherited: 2 (parentField, grandParentField)
}

class NOA_GrandChild extends NOA_Parent {
    private String grandChildField;    // Field 1

    public NOA_GrandChild() {
        super();
        this.grandChildField = "grandchild";
    }

    // Expected NOA:
    // - If declared only: 1 (grandChildField)
    // - If including inherited: 3 (grandChildField, parentField, grandParentField)
}

/**
 * Class with field shadowing
 */
class NOA_ShadowingParent {
    protected String shadowedField;    // Field 1

    public NOA_ShadowingParent() {
        this.shadowedField = "parent value";
    }

    // Expected NOA: 1 (shadowedField)
}

class NOA_ShadowingChild extends NOA_ShadowingParent {
    private String shadowedField;      // Field 1 - shadows parent field

    public NOA_ShadowingChild() {
        super();
        this.shadowedField = "child value";
    }

    // Expected NOA:
    // - If declared only: 1 (child's shadowedField)
    // - If including inherited: 2 (both parent and child shadowedField)
    // Note: Field shadowing creates two distinct fields with same name
}

/**
 * Class with enum fields
 */
enum NOA_TestEnum {
    VALUE1, VALUE2, VALUE3
}

class NOA_EnumFields {
    private NOA_TestEnum enumField1;   // Field 1 - enum type
    private NOA_TestEnum enumField2;   // Field 2 - enum type
    private String regularField;       // Field 3 - regular field

    public NOA_EnumFields() {
        this.enumField1 = NOA_TestEnum.VALUE1;
        this.enumField2 = NOA_TestEnum.VALUE2;
        this.regularField = "regular";
    }

    // Expected NOA: 3 (enumField1, enumField2, regularField)
}