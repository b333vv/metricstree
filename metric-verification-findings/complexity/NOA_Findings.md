# NOA (Number of Attributes) Metric Verification Findings

## Overview
Investigation of NOA metric discrepancies between PSI and JavaParser implementations revealed critical differences in field counting methodology and class filtering behavior.

## Test Results Summary

| Test Class | PSI Value | JavaParser Value | Expected PSI | Expected JP | Root Cause |
|------------|-----------|------------------|--------------|-------------|------------|
| NOA_BasicFields_TestClass | 0 | 0 | 4 | 4 | Class filtering issue |
| NOA_EmptyClass_TestClass | 0 | 0 | 0 | 0 | ✅ Correct (empty) |
| NOA_InheritanceChild_TestClass | 0 | 0 | 4 | 2 | **CRITICAL DISCREPANCY** |
| NOA_ConstantsOnly_TestClass | 0 | 0 | 3 | 3 | Class filtering issue |
| NOA_MixedFieldTypes_TestClass | 0 | 0 | 6 | 6 | Class filtering issue |

## Critical Finding: Fundamental Implementation Difference

### **PSI vs JavaParser Field Counting Methodology**

**PSI Implementation:**
```java
if (ClassUtils.isConcrete(psiClass)) {
    metric = Metric.of(NOA, psiClass.getAllFields().length);  // ← INCLUDES INHERITED
}
```

**JavaParser Implementation:**
```java
long numberOfAttributes = n.getFields().size();  // ← DECLARED ONLY
```

### **Expected Discrepancy Pattern**
- **No Inheritance:** PSI = JavaParser (both count same fields)
- **With Inheritance:** PSI > JavaParser (PSI includes inherited fields)

**Example: NOA_InheritanceChild_TestClass**
- **Base class:** 2 fields (baseField1, baseField2)
- **Child class:** 2 declared fields (childField1, childField2)
- **Expected PSI:** 4 fields (2 inherited + 2 declared)
- **Expected JavaParser:** 2 fields (declared only)
- **Expected Discrepancy:** +2 (PSI advantage)

## Root Cause Analysis

### 1. **Primary Issue: Class Filtering**
Same issue as NOM - `ClassUtils.isConcrete()` excludes test classes created via `configureByText()`:
- All test values return 0 instead of expected counts
- Prevents validation of the core algorithmic differences
- Affects both PSI and JavaParser implementations equally

### 2. **Secondary Issue: Semantic Difference**
**Fundamental disagreement on "Number of Attributes" definition:**

#### **PSI Interpretation (getAllFields)**
- **Scope:** All accessible fields including inherited
- **Rationale:** Complete picture of class data
- **Use Case:** Understanding total data complexity
- **Inheritance Impact:** Yes - counts inherited fields

#### **JavaParser Interpretation (getFields)**  
- **Scope:** Only locally declared fields
- **Rationale:** Class-specific implementation focus
- **Use Case:** Understanding class's direct contribution
- **Inheritance Impact:** No - ignores inherited fields

## Expected Discrepancy Patterns

### **No Discrepancy Cases**
- **Empty classes:** Both return 0
- **No inheritance:** Both count same declared fields
- **Constants/static fields:** Both count equally

### **Discrepancy Cases**
- **Single inheritance:** PSI = declared + inherited, JavaParser = declared
- **Multi-level inheritance:** PSI = declared + all inherited levels, JavaParser = declared
- **Interface implementation:** Depends on default field implementations

## Recommendations

### 1. **Immediate Actions**
- **Determine canonical definition:** Should NOA include inherited fields?
- **Check industry standards:** What do other metric tools count?
- **Validate against HashMap:** Test with `java.util.HashMap` to see actual behavior

### 2. **Investigation Priorities**
- **High:** Test against standard classes to bypass filtering issues
- **High:** Determine if inheritance counting is intentional design difference
- **Medium:** Consider separate metrics for declared vs total fields

### 3. **Implementation Options**

#### **Option A: Align to JavaParser (declared only)**
```java
// Change PSI to match JavaParser
metric = Metric.of(NOA, psiClass.getFields().length);  // Not getAllFields()
```

#### **Option B: Align to PSI (include inherited)**
```java  
// Change JavaParser to include inherited (complex - requires symbol resolution)
```

#### **Option C: Document as intentional difference**
- Clear documentation of different interpretations
- Possibly rename metrics to clarify scope

## Validation with Standard Classes

From existing tests: `java.util.HashMap` shows PSI NOA = 13
- Need to verify JavaParser value for comparison
- HashMap has complex inheritance hierarchy - good test case

## Conclusion

NOA metric investigation revealed **fundamental semantic differences** between PSI and JavaParser implementations:

**Key Insights:**
- ❌ **Critical algorithmic difference:** PSI includes inherited fields, JavaParser counts declared only
- ❌ **Class filtering prevents full validation** (same as NOM)  
- ✅ **Expected discrepancy pattern identified** for inheritance cases
- ❌ **No current mechanism to reconcile different interpretations**

**Impact Assessment:**
- **High impact** for classes with inheritance
- **Moderate impact** for composition-heavy designs
- **Low impact** for simple data classes

**Priority:** **HIGH** - This represents a fundamental design decision about what "Number of Attributes" means in object-oriented metrics.

**Next Steps:**
1. Test against standard classes to confirm hypothesis
2. Research industry standards for NOA metric definition  
3. Make architectural decision on canonical interpretation
4. Update implementation to ensure consistency