# RFC (Response For Class) Metric Calculation Logic

**RFC (Response For Class)** measures the number of methods that can be executed in response to a message received by an object of the class.

### Main Calculation Logic in ResponseForClassVisitor

1. **Identify Methods:**
   - All methods declared in the class are collected.

2. **Collect Invoked Methods:**
   - For each method call expression in the class, the called method is resolved and added to the set.

3. **Result:**
   - RFC is the total number of unique methods that can be invoked in response to a message to the class (declared + directly called methods).

### Example
If a class declares 3 methods and calls 2 other methods, RFC = 5 (if all are unique).

### Purpose of the Metric
A high RFC value indicates a class with a large interface and potentially high complexity.
