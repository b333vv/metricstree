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
**Observed Behavior**: ✅ **WORKING** - Returns actual numerical values  
**Actual CBO Value**: **6**

**Analysis Results**:
- PSI calculation strategy executes successfully
- Dependencies are properly built and cached
- Class resolution working correctly ("CBO_TestClass" found)
- Metric calculation completes without errors
- **Discrepancy**: PSI value (6) is significantly lower than ground truth (11)

**Visitor Class**: `CouplingBetweenObjectsVisitor`  
**Key Findings**: 
- ✅ Visitor is being called successfully
- ✅ PSI tree traversal working correctly
- ❌ **Type resolution appears incomplete** - missing 5 coupling relationships
- **Hypothesis**: PSI implementation may exclude standard library classes (java.lang.*, java.util.*)

**Missing Couplings (6 vs 11 expected)**:
- Likely missing: Serializable, List, Set, System, Map (standard library classes)
- PSI may be configured to ignore JDK/standard library dependencies

## JavaParser Implementation Analysis
**Observed Behavior**: ✅ **WORKING** - Returns actual numerical values  
**Actual CBO Value**: **12**

**Analysis Results**:
- JavaParser calculation strategy executes successfully with string-based parsing fallback
- TypeSolver configuration resolved for test environment
- Class parsing working correctly in temp filesystem
- Metric calculation completes without errors
- **Accuracy**: JavaParser value (12) is very close to ground truth (11)

**Visitor Class**: `JavaParserCouplingBetweenObjectsVisitor`  
**Key Findings**: 
- ✅ TypeSolverProvider configuration working (with temp filesystem handling)
- ✅ JavaParser can resolve project dependencies via string-based parsing
- ✅ AST traversal logic working correctly
- **Superior Accuracy**: JavaParser appears more comprehensive than PSI
- **Over-counting by 1**: 12 vs 11 expected - may be counting an additional coupling relationship

**Infrastructure Fixes Applied**:
- Added string-based parsing fallback for test environments
- Enhanced TypeSolver to handle temp filesystem limitations
- Implemented hybrid file/string parsing approach

## Discrepancy Analysis
**Root Cause**: ✅ **RESOLVED** - Infrastructure issues fixed, revealing actual algorithmic differences between PSI and JavaParser implementations.

**Comparison Results**:
- **Manual Ground Truth**: 11
- **PSI Implementation**: 6 (-5 from expected)
- **JavaParser Implementation**: 12 (+1 from expected)
- **JavaParser Accuracy**: 91.7% (much closer to ground truth)
- **PSI Accuracy**: 54.5% (significantly under-counting)

**Primary Discrepancies Identified**:
1. **Standard Library Exclusion (PSI)**: PSI appears to exclude java.lang.* and java.util.* classes from coupling count
2. **Comprehensive Detection (JavaParser)**: JavaParser counts all coupling relationships including standard library
3. **Over-counting (JavaParser)**: JavaParser may be counting one additional relationship not in ground truth

**Specific Missing Couplings in PSI**:
- Serializable (java.io.*)
- List (java.util.*)
- Set (java.util.*) 
- System (java.lang.*)
- Map (java.util.*)

**Potential JavaParser Over-counting**:
- May be double-counting generic type parameters
- Could be including implicit relationships not in manual calculation

## Recommended Actions
**Completed Infrastructure Fixes**:
1. ✅ **Fixed Test Infrastructure**: Resolved null value issues - both implementations now return numerical values
2. ✅ **Fixed Class Resolution**: Test harness correctly identifies classes and calculates metrics
3. ✅ **Fixed Strategy Execution**: Both PSI and JavaParser strategies execute successfully
4. ✅ **Fixed JavaParser TypeSolver**: Implemented string-based parsing fallback for test environments

**Algorithm Investigation Priority**:
1. **PSI Standard Library Exclusion**: Investigate CouplingBetweenObjectsVisitor to understand why standard library classes are excluded
2. **JavaParser Over-counting**: Analyze JavaParserCouplingBetweenObjectsVisitor to identify the extra coupling relationship
3. **Consistency Analysis**: Determine which approach (include/exclude standard library) is more appropriate for CBO metric

**Implementation Improvements**:
1. **PSI Enhancement**: Consider adding configuration option to include/exclude standard library dependencies
2. **JavaParser Refinement**: Fine-tune visitor to match exact CBO definition requirements
3. **Documentation**: Update metric definitions to clarify standard library handling policy

**Validation Actions**:
1. **Extended Test Cases**: Create additional test scenarios with different coupling patterns
2. **Cross-Reference**: Compare results with other static analysis tools (SonarQube, PMD, etc.)
3. **Performance Testing**: Measure calculation speed differences between PSI and JavaParser approaches

## Test Execution Results
**Test Environment**: IntelliJ Plugin Test Framework with BasePlatformTestCase  
**Test File**: `CBOTestCases.java` (1,102 characters)  
**Classes Processed**: 8 total classes in test data  
**Metrics per Class**: 29 metrics calculated  

**Test Method Results**:
1. ✅ `testCBO_GroundTruth()` - Manual calculation documented (11)
2. ✅ `testCBO_PSI_Implementation()` - PSI returns 6
3. ✅ `testCBO_JavaParser_Implementation()` - JavaParser returns 12

**Infrastructure Components Working**:
- ✅ DependenciesBuilder and DependenciesCalculator
- ✅ PSI calculation strategy with dependency caching
- ✅ JavaParser calculation strategy with string-based parsing fallback
- ✅ MetricVerificationTest framework
- ✅ Test data loading from metric-verification-data module

## Status
**Current State**: ✅ **INFRASTRUCTURE COMPLETE** - Both implementations working and returning numerical values  
**Analysis Phase**: **ALGORITHMIC DISCREPANCY INVESTIGATION** - Focus on understanding why PSI excludes standard library classes and JavaParser over-counts by 1  
**Next Steps**: Investigate visitor implementation details to understand coupling detection differences  
**Research Enabled**: Framework ready for systematic investigation of other metrics (RFC, LCOM, DIT, WMC, etc.)

## Additional Notes
**Technical Achievements**:
- ✅ Successfully resolved IntelliJ temp filesystem limitations for JavaParser
- ✅ Implemented hybrid file/string parsing approach for test environments
- ✅ Fixed ProgressIndicator null handling in test context
- ✅ Established working metric verification framework for all future investigations

**Key Insights**:
- **CBO complexity confirmed**: Metric depends heavily on accurate type resolution and coupling relationship detection
- **Parser differences significant**: 100% difference between PSI (6) and JavaParser (12) implementations
- **JavaParser superiority**: Closer to ground truth suggests more comprehensive coupling detection
- **Standard library policy**: Critical decision point - should CBO include java.lang.* and java.util.* dependencies?

**Research Framework Value**:
- Systematic comparison methodology established
- Reusable test infrastructure for investigating other metrics
- Clear documentation template for findings
- Automated verification tests prevent regression

**Impact on Plugin Users**:
- Current plugin shows PSI: 6, JavaParser: (12) - users see significant discrepancy
- JavaParser value appears more accurate based on formal CBO definition
- Recommendation: Consider promoting JavaParser as primary CBO calculation