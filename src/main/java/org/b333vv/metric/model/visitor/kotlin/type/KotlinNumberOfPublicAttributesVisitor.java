package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import com.intellij.psi.PsiElement;

import static org.b333vv.metric.model.metric.MetricType.NOPA;

/**
 * Kotlin Number Of Public Attributes (NOPA) metric visitor.
 *
 * <p>
 * Counts properties declared in the class that are effectively public
 * instance attributes with backing fields.
 * </p>
 *
 * <h3>Calculation Rules:</h3>
 * <ul>
 * <li><b>Includes:</b>
 * <ul>
 * <li>Public properties declared in class body (val/var)</li>
 * <li>Public properties declared in primary constructor (val/var
 * parameters)</li>
 * <li>Properties with backing fields (have actual storage)</li>
 * </ul>
 * </li>
 * <li><b>Excludes:</b>
 * <ul>
 * <li>Properties with 'private', 'protected', or 'internal' visibility</li>
 * <li>Properties in companion objects (static-like members)</li>
 * <li>Properties with 'const' modifier (compile-time constants)</li>
 * <li>Computed properties (custom accessors without backing fields)</li>
 * <li>Delegated properties (using 'by' keyword)</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Kotlin-Specific Considerations:</h3>
 * <p>
 * Computed properties are identified by having custom getters without backing
 * fields.
 * For example, {@code val fullName get() = "$firstName $lastName"} is not
 * counted
 * as an attribute since it doesn't store data.
 * </p>
 *
 * <p>
 * Delegated properties (e.g., {@code by lazy},
 * {@code by Delegates.observable()})
 * are excluded as they represent behavioral patterns rather than simple data
 * storage.
 * </p>
 *
 * @see KotlinMetricUtils#isPublicProperty(KtProperty)
 * @see KotlinMetricUtils#hasCustomAccessor(KtProperty)
 */
public class KotlinNumberOfPublicAttributesVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        compute(klass);
    }

    @Override
    public void visitObjectDeclaration(@NotNull KtObjectDeclaration declaration) {
        compute(declaration);
    }

    @Override
    public void visitKtFile(@NotNull KtFile file) {
        compute(file);
    }

    private void compute(@NotNull KtElement element) {
        int publicAttrs = 0;

        // Count properties from primary constructor (only for classes)
        if (element instanceof KtClass) {
            publicAttrs += countPrimaryConstructorProperties((KtClass) element);
        }

        // Count properties from declarations
        java.util.List<KtDeclaration> declarations = java.util.Collections.emptyList();
        if (element instanceof KtClassOrObject) {
            KtClassBody body = ((KtClassOrObject) element).getBody();
            if (body != null) {
                declarations = body.getDeclarations();
            }
        } else if (element instanceof KtFile) {
            declarations = ((KtFile) element).getDeclarations();
        }

        for (KtDeclaration d : declarations) {
            if (d instanceof KtProperty) {
                KtProperty p = (KtProperty) d;
                if (shouldCountProperty(p)) {
                    publicAttrs++;
                }
            }
        }

        metric = Metric.of(NOPA, publicAttrs);
    }

    /**
     * Counts public properties declared in the primary constructor.
     * Only val/var parameters are considered (regular parameters are excluded).
     *
     * @param klass the Kotlin class to analyze
     * @return count of public properties in primary constructor
     */
    private int countPrimaryConstructorProperties(@NotNull KtClass klass) {
        int count = 0;
        KtPrimaryConstructor primaryConstructor = klass.getPrimaryConstructor();

        if (primaryConstructor != null) {
            for (KtParameter param : primaryConstructor.getValueParameters()) {
                // Only val/var parameters create properties
                if (param.hasValOrVar()) {
                    // Check if the parameter property is public
                    if (isPublicParameter(param)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Checks if a constructor parameter is public.
     * Parameters are public by default unless marked with visibility modifiers.
     *
     * @param param the parameter to check
     * @return true if the parameter is public
     */
    private boolean isPublicParameter(@NotNull KtParameter param) {
        return !param.hasModifier(KtTokens.PRIVATE_KEYWORD) &&
                !param.hasModifier(KtTokens.PROTECTED_KEYWORD) &&
                !param.hasModifier(KtTokens.INTERNAL_KEYWORD);
    }

    /**
     * Determines if a property should be counted as a public attribute.
     * Applies all filtering rules for NOPA calculation.
     *
     * @param property the property to check
     * @return true if the property should be counted
     */
    private boolean shouldCountProperty(@NotNull KtProperty property) {
        // Exclude properties in companion objects
        if (isInCompanionObject(property)) {
            return false;
        }

        // Exclude const properties (compile-time constants)
        if (property.hasModifier(KtTokens.CONST_KEYWORD)) {
            return false;
        }

        // Exclude delegated properties (using 'by' keyword)
        if (property.getDelegate() != null) {
            return false;
        }

        // Exclude non-public properties
        if (!KotlinMetricUtils.isPublicProperty(property)) {
            return false;
        }

        // Exclude computed properties (custom accessors without backing fields)
        if (isComputedProperty(property)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a property is a computed property (no backing field).
     * A property is considered computed if it has a custom getter and either:
     * - No setter (for val)
     * - A custom setter (for var)
     *
     * This heuristic identifies properties that don't store data directly.
     *
     * @param property the property to check
     * @return true if the property is computed
     */
    private boolean isComputedProperty(@NotNull KtProperty property) {
        KtPropertyAccessor getter = property.getGetter();

        // Property with custom getter might be computed
        if (getter != null && getter.hasBody()) {
            // For val: custom getter means no backing field
            if (!property.isVar()) {
                return true;
            }

            // For var: check if setter is also custom (both custom = likely computed)
            KtPropertyAccessor setter = property.getSetter();
            if (setter != null && setter.hasBody()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a property is declared inside a companion object.
     *
     * @param property the property to check
     * @return true if the property is in a companion object
     */
    private boolean isInCompanionObject(@NotNull KtProperty property) {
        PsiElement parent = property.getParent();
        while (parent != null && !(parent instanceof KtClassBody)) {
            parent = parent.getParent();
        }
        if (parent instanceof KtClassBody) {
            PsiElement maybeCompanion = parent.getParent();
            if (maybeCompanion instanceof KtObjectDeclaration) {
                KtObjectDeclaration obj = (KtObjectDeclaration) maybeCompanion;
                return obj.isCompanion();
            }
        }
        return false;
    }
}
