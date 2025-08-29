# CBO_Findings.md

## Metric Definition
**Metric**: CBO (Coupling Between Objects)  
**Source**: Chidamber & Kemerer (1994)  
**Definition**: A count of the number of other classes to which a class is coupled. A class is coupled to another class if it uses its member functions and/or instance variables.

## Test Cases Created
**Test Data File**: `metric-verification-data/src/main/java/com/verification/coupling/CBOTestCases.java`  
**Test Class**: `src/integration-test/java/org/b333vv/metric/research/coupling/CBOMetricVerificationTest.java`

### Test Data Analysis
The `CBO_TestClass` test case was designed to include various coupling scenarios:

1. **Inheritance Coupling**: `extends Parent implements Serializable`
2. **Field Type Coupling**: `DependencyA fieldA`, `List<String> fieldB`, `GenericDependency<DependencyB> fieldC`
3. **Method Parameter Coupling**: `DependencyC paramC`
4. **Local Variable Type Coupling**: `Set<String> localSet`
5. **Static Method Call Coupling**: `System.out.println()`
6. **Return Type Coupling**: `Map<String, Integer>`, `InterfaceDep`

## Manual Calculation (Ground Truth)
**Expected CBO Value**: 11

**Detailed Count**:
1. Parent (inheritance - extends)
2. Serializable (inheritance - implements) 
3. DependencyA (field type)
4. List (field type - java.util)
5. GenericDependency (field type)
6. DependencyB (generic type parameter in field)
7. DependencyC (method parameter)
8. Set (local variable type - java.util)
9. System (static method call - java.lang)
10. Map (return type - java.util)
11. InterfaceDep (return type)

**Note**: This count includes java.lang.* and java.util.* classes. Different CBO implementations may exclude standard library classes.

## PSI Implementation Analysis
**Observed Behavior**: Test execution returns null values  
**Potential Issues**:
- Class name resolution may be failing ("CBO_TestClass" lookup)
- PSI calculation strategy may not be executing properly
- Metric may not be calculated for the test class

**Visitor Class**: `CouplingBetweenObjectsVisitor`  
**Investigation Needed**: 
- Verify visitor is being called
- Check if PSI tree traversal is working correctly
- Validate type resolution logic

## JavaParser Implementation Analysis
**Observed Behavior**: Test execution returns null values  
**Potential Issues**:
- JavaParser calculation strategy may not be augmenting results
- Type resolution might be failing (common JavaParser issue)
- Class parsing may not be working in test environment

**Visitor Class**: `JavaParserCouplingBetweenObjectsVisitor`  
**Investigation Needed**:
- Check TypeSolverProvider configuration
- Verify JavaParser can resolve project dependencies
- Validate AST traversal logic

## Discrepancy Analysis
**Root Cause**: Preliminary investigation suggests infrastructure/setup issues rather than algorithmic differences between PSI and JavaParser implementations.

**Primary Issues Identified**:
1. **Test Setup**: Null return values indicate fundamental setup problems
2. **Class Resolution**: Either class name lookup is failing or metrics aren't being calculated
3. **Strategy Execution**: PSI/JavaParser calculation strategies may not be running correctly

## Recommended Actions
**Immediate Priority**:
1. **Fix Test Infrastructure**: Resolve null value issues to enable proper comparison
2. **Debug Class Resolution**: Verify test harness correctly identifies classes
3. **Validate Calculation Strategies**: Ensure both PSI and JavaParser strategies execute

**Investigation Priority**:
1. **Verify Visitor Execution**: Add logging to confirm visitors are called
2. **Type Resolution Testing**: Create simpler test cases to isolate issues
3. **Library Dependency Handling**: Test how both implementations handle java.util.* classes

**Long-term Actions**:
1. **Algorithm Comparison**: Once infrastructure works, compare actual calculation logic
2. **Standard Library Handling**: Determine consistent approach for java.* classes
3. **Performance Analysis**: Compare calculation speed between implementations

## Status
**Current State**: Infrastructure Setup Issues  
**Next Steps**: Fix test execution environment before proceeding with algorithmic analysis  
**Timeline**: Foundational issues must be resolved before meaningful metric comparison can occur

## Additional Notes
- Test framework appears correctly designed based on Phase 1 implementation
- CBO is a complex metric that depends heavily on accurate type resolution
- Both PSI and JavaParser implementations will likely require careful configuration for cross-class dependencies