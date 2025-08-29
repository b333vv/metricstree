package com.verification.coupling;

import java.util.*;
import java.io.File;
import java.net.URL;

/**
 * Test cases for DAC (Data Abstraction Coupling) metric verification.
 * DAC measures the number of different abstract data types (classes) used as attributes in a class.
 */

/**
 * Helper classes for DAC testing
 */
class CustomDataType {
    private String value;
    
    public CustomDataType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

class AnotherDataType {
    private int number;
    
    public AnotherDataType(int number) {
        this.number = number;
    }
    
    public int getNumber() {
        return number;
    }
}

class GenericContainer<T> {
    private T data;
    
    public GenericContainer(T data) {
        this.data = data;
    }
    
    public T getData() {
        return data;
    }
}

interface DataInterface {
    void process();
}

abstract class AbstractDataType {
    protected String name;
    
    public AbstractDataType(String name) {
        this.name = name;
    }
    
    public abstract void performAction();
}

/**
 * Primary test class with various field types
 * Expected DAC: Count unique class types used as fields
 */
class DAC_TestClass {
    // Primitive types (should NOT count toward DAC)
    private int primitiveInt;
    private double primitiveDouble;
    private boolean primitiveBoolean;
    private char primitiveChar;
    
    // Wrapper classes (should count toward DAC)
    private Integer wrapperInteger;
    private Double wrapperDouble;
    private Boolean wrapperBoolean;
    private Character wrapperCharacter;
    
    // Standard library classes (may or may not count depending on implementation)
    private String stringField;
    private List<String> listField;
    private Map<String, Integer> mapField;
    private Set<CustomDataType> setField;
    private Date dateField;
    private File fileField;
    private URL urlField;
    
    // Custom classes (should count toward DAC)
    private CustomDataType customData;
    private AnotherDataType anotherData;
    private GenericContainer<String> genericContainer;
    
    // Interface and abstract types (should count toward DAC)
    private DataInterface interfaceField;
    private AbstractDataType abstractField;
    
    // Array types (behavior may vary)
    private String[] stringArray;
    private CustomDataType[] customArray;
    private int[] primitiveArray; // primitive array - may or may not count
    
    // Static fields (should count toward DAC same as instance fields)
    private static CustomDataType staticCustomData;
    private static String staticStringField;
    
    // Final fields (should count toward DAC same as regular fields)
    private final AnotherDataType finalField = new AnotherDataType(42);
    
    // Expected DAC count (excluding primitives):
    // Wrapper classes: Integer, Double, Boolean, Character (4)
    // Standard library: String, List, Map, Set, Date, File, URL (7) - if counted
    // Custom classes: CustomDataType, AnotherDataType, GenericContainer (3)
    // Interface/Abstract: DataInterface, AbstractDataType (2)
    // Arrays: String, CustomDataType (2) - if array component types count
    // Total: 18+ depending on standard library inclusion and array handling
}

/**
 * Class with no fields (DAC should be 0)
 */
class NoFields_TestClass {
    // No fields at all
    
    public void methodWithLocalVariables() {
        // Local variables should NOT count toward DAC
        String localString = "local";
        CustomDataType localCustom = new CustomDataType("local");
        List<String> localList = new ArrayList<>();
    }
    
    public String processData(CustomDataType input) {
        // Method parameters should NOT count toward DAC
        return input.getValue();
    }
    
    // Expected DAC: 0 (no fields)
}

/**
 * Class with only primitive fields (DAC should be 0)
 */
class PrimitivesOnly_TestClass {
    private int intField;
    private double doubleField;
    private boolean booleanField;
    private char charField;
    private long longField;
    private float floatField;
    private byte byteField;
    private short shortField;
    
    // Expected DAC: 0 (only primitive types)
}

/**
 * Class with duplicate field types (should count unique types only)
 */
class DuplicateTypes_TestClass {
    private String firstString;
    private String secondString;
    private String thirdString;
    
    private CustomDataType firstCustom;
    private CustomDataType secondCustom;
    
    private List<String> firstList;
    private List<Integer> secondList; // Different generic type, but same List class
    
    private Map<String, String> stringMap;
    private Map<Integer, CustomDataType> mixedMap; // Different generics, same Map class
    
    // Expected DAC: 4 unique types (String, CustomDataType, List, Map)
    // Generic type parameters typically don't create separate types for DAC
}

/**
 * Class with complex generic types
 */
class ComplexGenerics_TestClass {
    private List<CustomDataType> customList;
    private Map<String, AnotherDataType> customMap;
    private Set<GenericContainer<String>> nestedGenericSet;
    private GenericContainer<List<String>> containerOfList;
    
    // Wildcard generics
    private List<?> wildcardList;
    private Map<? extends CustomDataType, ? super AnotherDataType> boundedMap;
    
    // Raw types (without generics)
    @SuppressWarnings("rawtypes")
    private List rawList;
    @SuppressWarnings("rawtypes")
    private Map rawMap;
    
    // Expected DAC: Depends on whether generic type parameters are considered
    // Base types: List, Map, Set, GenericContainer (4)
    // Plus any generic parameter types if they count separately
}

/**
 * Class with inheritance to test field attribution
 */
class InheritanceBase {
    protected String baseField;
    protected CustomDataType baseCustomField;
    private int basePrivateField; // primitive, shouldn't count
}

class InheritanceDerived extends InheritanceBase {
    private AnotherDataType derivedField;
    private String derivedStringField; // String type already used in base
    
    // Expected DAC: Should count fields from both base and derived classes
    // Unique types: String, CustomDataType, AnotherDataType (3)
    // Or just derived class fields: AnotherDataType, String (2) - depending on implementation
}

/**
 * Class with nested and inner class fields
 */
class NestedClasses_TestClass {
    
    static class StaticNestedClass {
        private String nestedData;
        
        public StaticNestedClass(String data) {
            this.nestedData = data;
        }
    }
    
    class InnerClass {
        private int innerData;
        
        public InnerClass(int data) {
            this.innerData = data;
        }
    }
    
    // Fields using nested classes
    private StaticNestedClass staticNestedField;
    private InnerClass innerClassField;
    private NestedClasses_TestClass.StaticNestedClass fullyQualifiedField;
    
    // Regular fields
    private String regularField;
    
    // Expected DAC: StaticNestedClass, InnerClass, String (3)
    // Nested classes should count as distinct types
}

/**
 * Class with enum fields
 */
enum TestEnum {
    VALUE1, VALUE2, VALUE3
}

class EnumFields_TestClass {
    private TestEnum enumField;
    private TestEnum anotherEnumField; // Duplicate type
    
    // Standard enum
    private java.time.DayOfWeek dayField;
    
    // Regular fields
    private String stringField;
    private CustomDataType customField;
    
    // Expected DAC: TestEnum, DayOfWeek, String, CustomDataType (4)
    // Enums should count as class types
}

/**
 * Class with annotation and special types
 */
class SpecialTypes_TestClass {
    // Functional interface field
    private Runnable runnableField;
    private java.util.function.Function<String, Integer> functionField;
    
    // Exception type field
    private Exception exceptionField;
    private RuntimeException runtimeExceptionField;
    
    // Class type field
    private Class<?> classField;
    private Class<String> typedClassField;
    
    // Regular fields
    private String stringField;
    
    // Expected DAC: Runnable, Function, Exception, RuntimeException, Class, String
    // All should count as distinct class types
}