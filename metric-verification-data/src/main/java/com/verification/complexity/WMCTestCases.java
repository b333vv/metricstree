package com.verification.complexity;

import java.util.List;
import java.util.ArrayList;

/**
 * Test cases for WMC (Weighted Method Count) metric verification.
 * WMC sums the complexities of all methods in a class, providing a measure of overall class complexity.
 * Typically uses cyclomatic complexity for each method.
 */

/**
 * Simple class with basic methods (low complexity)
 * Expected WMC: sum of cyclomatic complexities of all methods
 */
class WMC_SimpleClass {
    private String name;
    private int value;

    // Constructor - complexity = 1 (no branching)
    public WMC_SimpleClass(String name) {
        this.name = name;
        this.value = 0;
    }

    // Simple setter - complexity = 1 (no branching)
    public void setName(String name) {
        this.name = name;
    }

    // Simple getter - complexity = 1 (no branching)
    public String getName() {
        return this.name;
    }

    // Simple setter - complexity = 1 (no branching)
    public void setValue(int value) {
        this.value = value;
    }

    // Simple getter - complexity = 1 (no branching)
    public int getValue() {
        return this.value;
    }

    // Expected WMC: 5 (each method has complexity 1)
}

/**
 * Class with conditional logic (medium complexity)
 */
class WMC_ConditionalClass {
    private int number;
    private String status;

    // Constructor - complexity = 1
    public WMC_ConditionalClass(int number) {
        this.number = number;
        this.status = "initial";
    }

    // Method with simple if - complexity = 2 (1 + 1 decision point)
    public void setNumber(int number) {
        if (number >= 0) {
            this.number = number;
        }
    }

    // Method with if-else - complexity = 2 (1 + 1 decision point)
    public String getNumberDescription() {
        if (this.number > 0) {
            return "positive";
        } else {
            return "non-positive";
        }
    }

    // Method with multiple conditions - complexity = 3 (1 + 2 decision points)
    public void updateStatus() {
        if (this.number > 10) {
            this.status = "high";
        } else if (this.number > 0) {
            this.status = "low";
        } else {
            this.status = "zero or negative";
        }
    }

    // Method with logical operators - complexity = 3 (1 + 2 logical operators)
    public boolean isValidRange() {
        return this.number >= 0 && this.number <= 100;
    }

    // Expected WMC: 1 + 2 + 2 + 3 + 3 = 11
}

/**
 * Class with loops (higher complexity)
 */
class WMC_LoopClass {
    private List<Integer> numbers;

    // Constructor - complexity = 1
    public WMC_LoopClass() {
        this.numbers = new ArrayList<>();
    }

    // Method with simple loop - complexity = 2 (1 + 1 loop)
    public void addNumbers(int count) {
        for (int i = 0; i < count; i++) {
            this.numbers.add(i);
        }
    }

    // Method with nested loops - complexity = 3 (1 + 2 loops)
    public void generateMatrix(int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.numbers.add(i * size + j);
            }
        }
    }

    // Method with loop and condition - complexity = 3 (1 + 1 loop + 1 condition)
    public int countPositive() {
        int count = 0;
        for (Integer num : this.numbers) {
            if (num > 0) {
                count++;
            }
        }
        return count;
    }

    // Method with while loop and multiple conditions - complexity = 4 (1 + 1 while + 2 conditions)
    public void processUntilCondition() {
        int index = 0;
        while (index < this.numbers.size()) {
            Integer current = this.numbers.get(index);
            if (current > 10) {
                break;
            } else if (current < 0) {
                this.numbers.remove(index);
                continue;
            }
            index++;
        }
    }

    // Expected WMC: 1 + 2 + 3 + 3 + 4 = 13
}

/**
 * Class with complex control flow
 */
class WMC_ComplexControlFlow {
    private String[] data;
    private int status;

    // Constructor - complexity = 1
    public WMC_ComplexControlFlow(int size) {
        this.data = new String[size];
        this.status = 0;
    }

    // Method with switch statement - complexity = 5 (1 + 4 case branches)
    public void setStatusDescription(int code) {
        switch (code) {
            case 1:
                this.status = 1;
                break;
            case 2:
                this.status = 2;
                break;
            case 3:
                this.status = 3;
                break;
            default:
                this.status = 0;
        }
    }

    // Method with exception handling - complexity = 3 (1 + 1 try + 1 catch)
    public boolean processData(int index, String value) {
        try {
            this.data[index] = value;
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    // Method with complex nested conditions - complexity = 6
    public String analyzeData() {
        if (this.data != null) {                    // +1
            if (this.data.length > 0) {             // +1
                for (String item : this.data) {     // +1
                    if (item != null) {             // +1
                        if (item.length() > 5) {    // +1
                            return "complex";
                        }
                    }
                }
                return "simple";
            } else {
                return "empty";
            }
        } else {
            return "null";
        }
    }

    // Method with ternary operators - complexity = 3 (1 + 2 ternary operators)
    public String getStatusText() {
        return this.status > 0 ? 
               (this.status > 2 ? "high" : "medium") : 
               "low";
    }

    // Expected WMC: 1 + 5 + 3 + 6 + 3 = 18
}

/**
 * Class with exception handling patterns
 */
class WMC_ExceptionHandling {
    private String data;

    // Constructor - complexity = 1
    public WMC_ExceptionHandling() {
        this.data = "initial";
    }

    // Method with try-catch - complexity = 2 (1 + 1 catch)
    public boolean setData(String input) {
        try {
            this.data = input.trim();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    // Method with multiple catch blocks - complexity = 3 (1 + 2 catch blocks)
    public String processData() {
        try {
            return this.data.toUpperCase();
        } catch (NullPointerException e) {
            return "NULL";
        } catch (Exception e) {
            return "ERROR";
        }
    }

    // Method with try-catch-finally - complexity = 2 (1 + 1 catch, finally doesn't add complexity)
    public void complexMethod() {
        try {
            this.data = this.data.toLowerCase();
        } catch (Exception e) {
            this.data = "error";
        } finally {
            System.out.println("Method completed");
        }
    }

    // Method with nested try-catch - complexity = 3 (1 + 2 nested try-catch)
    public String nestedTryCatch() {
        try {
            try {
                return this.data.substring(0, 5);
            } catch (StringIndexOutOfBoundsException e) {
                return this.data;
            }
        } catch (NullPointerException e) {
            return "null";
        }
    }

    // Expected WMC: 1 + 2 + 3 + 2 + 3 = 11
}

/**
 * Empty class for baseline
 */
class WMC_EmptyClass {
    // Expected WMC: 0 (no methods)
}

/**
 * Class with only simple methods (no control flow)
 */
class WMC_NoControlFlow {
    private int value1;
    private int value2;
    private int value3;

    // Simple methods with no branching - each has complexity = 1
    public void setValue1(int value) {
        this.value1 = value;
    }

    public void setValue2(int value) {
        this.value2 = value;
    }

    public void setValue3(int value) {
        this.value3 = value;
    }

    public int getValue1() {
        return this.value1;
    }

    public int getValue2() {
        return this.value2;
    }

    public int getValue3() {
        return this.value3;
    }

    public int getSum() {
        return this.value1 + this.value2 + this.value3;
    }

    // Expected WMC: 7 (7 methods × complexity 1)
}

/**
 * Class with recursive methods
 */
class WMC_RecursiveClass {
    private int value;

    // Constructor - complexity = 1
    public WMC_RecursiveClass(int value) {
        this.value = value;
    }

    // Simple recursive method - complexity = 2 (1 + 1 recursive condition)
    public int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    // More complex recursive method - complexity = 3 (1 + 2 conditions)
    public int fibonacci(int n) {
        if (n <= 0) {
            return 0;
        } else if (n == 1) {
            return 1;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    // Recursive method with additional logic - complexity = 4 (1 + 3 branches)
    public void recursiveProcess(int depth) {
        if (depth <= 0) {
            return;
        }
        
        if (depth % 2 == 0) {
            this.value *= 2;
            recursiveProcess(depth - 1);
        } else {
            this.value += 1;
            recursiveProcess(depth - 1);
        }
    }

    // Expected WMC: 1 + 2 + 3 + 4 = 10
}

/**
 * Class with lambda expressions and streams (modern Java complexity)
 */
class WMC_ModernJava {
    private List<Integer> numbers;

    // Constructor - complexity = 1
    public WMC_ModernJava() {
        this.numbers = new ArrayList<>();
    }

    // Method with lambda - complexity depends on how lambdas are counted
    // Conservative: 1 (no control flow in main method)
    // Liberal: might count lambda conditions
    public void processNumbers() {
        numbers.stream()
               .filter(n -> n > 0)     // Condition in lambda
               .map(n -> n * 2)
               .forEach(System.out::println);
    }

    // Method with complex stream operations - complexity = 2 or more
    public long countFilteredNumbers() {
        return numbers.stream()
                     .filter(n -> n > 0 && n < 100)  // Multiple conditions in lambda
                     .count();
    }

    // Method with conditional and lambda - complexity = 2 + lambda complexity
    public void conditionalProcess(boolean flag) {
        if (flag) {
            numbers.stream()
                   .filter(n -> n % 2 == 0)
                   .forEach(n -> System.out.println("Even: " + n));
        }
    }

    // Expected WMC: depends on lambda complexity counting methodology
    // Conservative estimate: 1 + 1 + 2 = 4
    // Liberal estimate: higher due to lambda conditions
}