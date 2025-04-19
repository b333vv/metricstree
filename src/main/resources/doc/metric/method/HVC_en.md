# HVC (Halstead Vocabulary) Metric Calculation Logic

**HVC (Halstead Vocabulary)** is the number of distinct operators and operands in a method.

### Main Calculation Logic

1. **Identify Unique Elements:**
   - Count distinct operators and distinct operands.
2. **Sum Unique Counts:**
   - HVC = (number of unique operators) + (number of unique operands).

### Example
If there are 5 unique operators and 8 unique operands, HVC = 13.

### Purpose of the Metric
HVC indicates the diversity of code elements, which reflects code complexity.
