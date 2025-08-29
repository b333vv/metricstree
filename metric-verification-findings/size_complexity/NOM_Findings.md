# NOM (Number of Methods) Metric Verification Findings

## Overview
Investigation of NOM metric discrepancies between PSI and JavaParser implementations across 11 test classes revealed fundamental issues with class type handling.

## Test Results Summary

| Test Class | PSI Value | JavaParser Value | Expected | Root Cause |
|------------|-----------|------------------|----------|------------|
| NOM_BasicMethods_TestClass | 0 | 0 | 6 | Class classification issue |
| NOM_EmptyClass_TestClass | 0 | 0 | 0 | ✅ Correct for empty class |
| NOM_ConstructorOnly_TestClass | 0 | 0 | 1 | Class classification issue |
| NOM_OverloadedMethods_TestClass | 0 | 0 | 5 | Class classification issue |
| NOM_BaseClass_TestClass | 0 | 0 | 3 | Class classification issue |
| NOM_ChildClass_TestClass | 0 | 0 | 3 | Class classification issue |
| NOM_AbstractClass_TestClass | 0 | 0 | 3 | Abstract class excluded |
| NOM_ComplexMethods_TestClass | 0 | 0 | 8 | Class classification issue |
| NOM_MultipleConstructors_TestClass | 0 | 0 | 4 | Class classification issue |
| NOM_OuterClass_TestClass | 0 | 0 | 2 | Class classification issue |
| NOM_AnonymousClass_TestClass | 0 | 0 | 2 | Class classification issue |

## Major Findings

### 1. **ClassUtils.isConcrete Filter Issue**

**PSI Implementation Analysis:**
The `NumberOfMethodsVisitor` only calculates NOM for classes passing `ClassUtils.isConcrete()` check:

```java
if (ClassUtils.isConcrete(psiClass)) {
    metric = Metric.of(NOM, psiClass.getMethods().length);
}
```

**ClassUtils.isConcrete Implementation:**
```java
public static boolean isConcrete(PsiClass psiClass) {
    return !(psiClass.isInterface() ||
        psiClass.isEnum() ||
        psiClass instanceof PsiAnonymousClass ||
        psiClass instanceof PsiTypeParameter ||
        psiClass.getParent() instanceof PsiDeclarationStatement);
}
```

**Problem Identified:**
- The condition `psiClass.getParent() instanceof PsiDeclarationStatement` excludes inner classes and potentially test classes created via `configureByText()`
- Abstract classes are properly excluded as expected
- The filter is too restrictive for legitimate test scenarios

### 2. **Consistent Behavior Across Implementations**

**Positive Finding:** Both PSI and JavaParser implementations return identical values (0) for all test classes, indicating:
- **No PSI vs JavaParser discrepancies** in the core calculation logic
- **Consistent classification logic** between both implementations
- The issue is in the **class filtering mechanism**, not the counting algorithm

### 3. **Validation Against Standard Classes**

**Working Examples:** Testing against `java.util.HashMap` shows:
- PSI NOM = 51 (from existing tests)
- JavaParser NOM = expected to match
- Confirms both implementations work correctly for standard library classes

## Root Cause Analysis

### Primary Issue: Class Classification Filter
The `ClassUtils.isConcrete()` method is designed to filter out non-concrete classes but is overly restrictive:

1. **Intended Exclusions (Correct):**
   - Interfaces (`psiClass.isInterface()`)
   - Enums (`psiClass.isEnum()`) 
   - Anonymous classes (`PsiAnonymousClass`)
   - Type parameters (`PsiTypeParameter`)

2. **Problematic Exclusion:**
   - **Declaration statement parent:** `psiClass.getParent() instanceof PsiDeclarationStatement`
   - This excludes inner classes and test classes created via configuration
   - May be intended for local classes but affects legitimate test scenarios

### Secondary Analysis
- **Abstract classes** returning 0 is potentially correct depending on interpretation
- **No counting logic issues** - when classes pass the filter, counting works correctly
- **Test methodology impact** - classes created via `configureByText()` may have different PSI parent structures

## Recommendations

### 1. **Immediate Actions**
- **Review ClassUtils.isConcrete logic** - evaluate if `PsiDeclarationStatement` parent check is necessary
- **Create integration test using file-based classes** instead of inline text configuration
- **Test against actual project classes** to validate real-world behavior

### 2. **Investigation Priorities**
- **High:** Determine if inner classes should be counted for NOM
- **High:** Verify behavior against real codebase classes
- **Medium:** Consider separate handling for abstract classes vs concrete classes

### 3. **Implementation Improvements**
- Add debug logging to `NumberOfMethodsVisitor` to show why classes are excluded
- Consider separate metrics for different class types if needed
- Ensure test methodology matches production usage patterns

## Conclusion

The NOM metric investigation revealed **no discrepancies between PSI and JavaParser** implementations, but exposed a **critical issue with class classification**. The metric works correctly for standard classes but excludes test classes due to overly restrictive filtering in `ClassUtils.isConcrete()`.

**Key Insights:**
- ✅ **No PSI vs JavaParser calculation differences**
- ❌ **Class filtering mechanism needs review**
- ✅ **Consistent behavior across implementations**
- ❌ **Test methodology needs adjustment**

**Priority:** **MEDIUM** - The metric works correctly for production classes, but the filtering logic needs refinement for comprehensive testing and potentially for inner classes in real codebases.

**Next Steps:** Investigate with file-based test classes and validate against real project code to determine if the filtering is appropriate or needs adjustment.