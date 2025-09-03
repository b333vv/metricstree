# SIZE2 (Number of Attributes and Methods) Metric Calculation Logic

**SIZE2 (Number of Attributes and Methods)** is part of the Li & Henry metric suite and measures the structural size of a class. It is the sum of all available attributes (fields) and operations (methods).

### Main Calculation Logic

1.  **Count Methods:**
    -   All non-static methods of the class, including inherited ones, are counted.
    -   Constructors are included in this count.
    -   Static methods are **excluded**.

2.  **Count Fields:**
    -   All non-static attributes (fields) of the class, including inherited ones, are counted.
    -   Static fields (constants, etc.) are **excluded**.

3.  **Result:**
    -   SIZE2 is the sum of the number of non-static operations and non-static attributes.

### Example
If a class declares 2 instance methods and 1 instance field, and inherits 3 instance methods and 2 instance fields, its SIZE2 value is (2 + 3) + (1 + 2) = 8.

### Purpose of the Metric
SIZE2 provides an overall measure of a class's size in terms of its interface and state, including functionality inherited from its superclasses. It is a more comprehensive size measure than simply counting declared members.