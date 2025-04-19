# HD (Halstead Difficulty) Metric Calculation Logic

**HD (Halstead Difficulty)** quantifies the difficulty of writing or understanding the method, based on operators and operands.

### Main Calculation Logic

1. **Count Elements:**
   - Count number of unique operators (n1), unique operands (n2), total operands (N2).
2. **Calculate Difficulty:**
   - HD = (n1 / 2) * (N2 / n2)

### Example
If n1=5, n2=8, N2=20, then HD = (5/2) * (20/8) = 6.25.

### Purpose of the Metric
HD helps estimate the effort required to implement or understand the code.
