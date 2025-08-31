# CBO (Coupling Between Objects) Metric Calculation Logic

**CBO (Coupling Between Objects)** measures the number of classes to which a given class is coupled. This implementation counts outgoing dependencies only - the number of distinct classes that the analyzed class depends upon.

## Overview

The MetricsTree plugin implements CBO calculation using two complementary static analysis engines:
- **PSI (Program Structure Interface)** - IntelliJ's native AST representation
- **JavaParser** - External AST parser with enhanced symbol resolution

Both engines are designed to produce identical results through comprehensive dependency analysis.

## Core Calculation Logic

### Dependencies Counted

The CBO metric counts the following types of class dependencies:

1. **Import Dependencies**
   - Direct class imports (not package imports with `.*`)
   - Static imports are resolved to their declaring classes

2. **Type References**
   - Class declarations and interface implementations
   - Method parameter and return types
   - Field declarations
   - Generic type parameters
   - Array component types

3. **Method Call Dependencies**
   - Instance method calls resolved to their declaring classes
   - Static method calls (e.g., `Objects.requireNonNull()`)
   - Constructor calls (`new ClassName()`)

4. **Method Reference Expressions**
   - Method references like `PsiType::getPresentableText`
   - Lambda expressions with type dependencies

5. **Annotation Dependencies**
   - All annotation types (e.g., `@Override`, `@NotNull`, `@Service`)
   - Annotation parameter types

6. **Field Access Dependencies**
   - Static field references (e.g., `Value.UNDEFINED`)
   - Instance field access to external classes
   - Qualified field references for static imports

7. **Nested Class References**
   - Inner and nested class types (e.g., `Service.Level`)
   - Enum constants from external classes

### Dependencies Excluded

- **Self-references**: The class being analyzed is never counted as a dependency
- **Primitive types**: `int`, `boolean`, `char`, etc. are not counted
- **Package imports**: Wildcard imports (`import java.util.*;`) are not counted
- **Duplicate dependencies**: Each class is counted only once regardless of usage frequency

## Implementation Details

### PSI-Based Analysis (`CouplingBetweenObjectsVisitor`)

The PSI visitor implements a comprehensive `JavaRecursiveElementVisitor` that analyzes:

```java
// Key visitor methods:
- visitTypeElement(PsiTypeElement)
- visitReferenceElement(PsiJavaCodeReferenceElement) 
- visitMethodCallExpression(PsiMethodCallExpression)
- visitMethodReferenceExpression(PsiMethodReferenceExpression)
- visitNewExpression(PsiNewExpression)
- visitReferenceExpression(PsiReferenceExpression) // For field access
```

**Enhanced Features:**
- **Import Analysis**: Resolves all import statements to their target classes
- **Type Resolution**: Handles complex generic types and nested classes
- **Method Call Resolution**: Resolves method calls to their declaring classes
- **Field Access Detection**: Captures static field references like `Value.UNDEFINED`
- **Annotation Processing**: Includes all annotation dependencies
- **Fallback Inference**: Uses text-based inference when symbol resolution fails

### JavaParser-Based Analysis (`JavaParserCouplingBetweenObjectsVisitor`)

The JavaParser visitor uses AST walking with comprehensive node type analysis:

```java
// Key AST node types analyzed:
- ClassOrInterfaceType (type references)
- MethodCallExpr (method calls)
- ObjectCreationExpr (constructor calls)
- MethodReferenceExpr (method references)
- AnnotationExpr (annotations)
- FieldAccessExpr (field access)
- NameExpr (name expressions that might be fields)
```

**Enhanced Features:**
- **Cross-file Resolution**: Uses TypeSolver for accurate symbol resolution
- **Import Dependency Analysis**: Processes compilation unit imports
- **Field Access Detection**: Handles both `FieldAccessExpr` and `NameExpr` patterns
- **Nested Type Handling**: Resolves nested class references like `Service.Level`
- **Fallback Patterns**: Text-based inference for common static field patterns
- **Error Recovery**: Graceful handling of unresolvable symbols

## Specific Implementation Cases

### Static Field References

**Pattern**: `Value.UNDEFINED`
- **PSI**: Detected via `visitReferenceExpression()` resolving `PsiField` elements
- **JavaParser**: Detected via `FieldAccessExpr` and `NameExpr` walking
- **Fallback**: Text pattern matching for common cases like `UNDEFINED`

### Nested Class References

**Pattern**: `Service.Level`
- **PSI**: Resolved through standard type resolution mechanisms
- **JavaParser**: Enhanced type text analysis for dot-separated names
- **Special Handling**: Inference for annotation parameter types

### Method References

**Pattern**: `PsiType::getPresentableText`
- **PSI**: Handled via `visitMethodReferenceExpression()`
- **JavaParser**: Analyzed through `MethodReferenceExpr` scope resolution
- **Resolution**: Qualifier type is resolved to determine the declaring class

### Annotation Dependencies

**Pattern**: `@Override`, `@Service(level = Service.Level.PROJECT)`
- **PSI**: Complete annotation element analysis with parameter resolution
- **JavaParser**: `AnnotationExpr` walking with qualified name resolution
- **Fallback**: Common annotation inference (e.g., `Override` → `java.lang.Override`)

## Quality Assurance

### Alignment Verification

Both analysis engines are designed to produce identical results:
- Comprehensive test coverage verifies PSI-JavaParser alignment
- Debug logging capabilities for discrepancy investigation
- Fallback mechanisms ensure consistent behavior

### Error Handling

- **Symbol Resolution Failures**: Graceful fallback to text-based inference
- **Missing Dependencies**: Conservative estimation to avoid under-counting
- **Type Resolution Errors**: Robust error recovery with logging

### Performance Considerations

- **Incremental Analysis**: Only re-analyzes changed classes
- **Caching**: Results are cached to avoid redundant calculations
- **Memory Management**: Efficient collection handling for large codebases

## Calculation Formula

```
CBO = |Dependencies|

where Dependencies = {
    ImportDependencies ∪
    TypeReferences ∪ 
    MethodCallDependencies ∪
    FieldAccessDependencies ∪
    AnnotationDependencies ∪
    NestedClassReferences
} - {CurrentClass}
```

## Example Calculation

For a class with:
- 3 import statements
- 2 method parameter types
- 1 return type
- 2 method calls to external classes
- 1 annotation
- 1 static field reference
- 1 overlap between categories

**Result**: CBO = 9 (total unique external class dependencies)

## Interpretation Guidelines

### CBO Value Ranges
- **0-7**: Low coupling (good modularity)
- **8-15**: Moderate coupling (acceptable)
- **16+**: High coupling (consider refactoring)

### Design Implications
- **High CBO**: Indicates classes with many responsibilities
- **Very Low CBO**: May indicate utility classes or well-encapsulated components
- **Trending Increases**: Suggests growing complexity and potential design debt

## Related Metrics

- **LCOM (Lack of Cohesion of Methods)**: Measures internal cohesion
- **DIT (Depth of Inheritance Tree)**: Measures inheritance coupling
- **NOC (Number of Children)**: Measures inheritance fan-out
- **RFC (Response for Class)**: Measures method-level coupling
