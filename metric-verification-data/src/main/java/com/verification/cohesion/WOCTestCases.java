package com.verification.cohesion;

/**
 * Test cases for WOC (Weight of a Class) metric verification.
 * WOC measures the proportion of functional (non-trivial) public methods
 * to all public methods in a class.
 */
class WOC_TestClass {
    private String name;
    private int age;
    private boolean active;
    private double score;

    // Simple getter methods (trivial)
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean isActive() {
        return active;
    }

    // Simple setter methods (trivial)
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Functional public methods (non-trivial)
    public void processData() {
        if (active) {
            score = age * 1.5;
            System.out.println("Processing data for " + name);
        }
    }

    public double calculateScore() {
        return active ? age * 2.5 + Math.random() : 0.0;
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Name: ").append(name).append("\n");
        report.append("Age: ").append(age).append("\n");
        report.append("Active: ").append(active).append("\n");
        report.append("Score: ").append(calculateScore()).append("\n");
        return report.toString();
    }

    // Private methods (should not be counted)
    private void internalProcess() {
        score = age * 0.5;
    }

    private String formatName() {
        return name != null ? name.toUpperCase() : "UNKNOWN";
    }

    // Protected method (should not be counted for WOC)
    protected void protectedMethod() {
        System.out.println("Protected method");
    }

    // Package-private method (should not be counted for WOC)
    void packageMethod() {
        System.out.println("Package method");
    }

    // Constructor (may or may not be counted depending on implementation)
    public WOC_TestClass() {
        this.name = "";
        this.age = 0;
        this.active = false;
        this.score = 0.0;
    }

    public WOC_TestClass(String name, int age) {
        this.name = name;
        this.age = age;
        this.active = true;
        this.score = 0.0;
    }
}

/**
 * Class with only functional public methods (WOC = 1.0)
 */
class AllFunctional_TestClass {
    public void businessOperation1() {
        System.out.println("Complex business logic 1");
        // Complex logic here
    }

    public void businessOperation2() {
        System.out.println("Complex business logic 2");
        // Complex logic here
    }

    public String complexCalculation() {
        return "Result: " + (Math.random() * 100);
    }
}

/**
 * Class with only getters/setters (WOC = 0.0)
 */
class OnlyAccessors_TestClass {
    private String value;
    private int count;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

/**
 * Class with no public methods (edge case)
 */
class NoPublicMethods_TestClass {
    private String data;

    private void privateMethod() {
        data = "private";
    }

    protected void protectedMethod() {
        data = "protected";
    }

    void packageMethod() {
        data = "package";
    }
}

/**
 * Class with mixed method types to test getter/setter detection
 */
class MixedMethods_TestClass {
    private String name;
    private int value;

    // Standard getter
    public String getName() {
        return name;
    }

    // Standard setter
    public void setName(String name) {
        this.name = name;
    }

    // Getter with validation (might be considered functional)
    public int getValue() {
        if (value < 0) {
            throw new IllegalStateException("Invalid value");
        }
        return value;
    }

    // Setter with validation (might be considered functional)
    public void setValue(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        this.value = value;
    }

    // Clearly functional method
    public void processValue() {
        if (value > 100) {
            value = 100;
        }
        System.out.println("Processed: " + value);
    }
}