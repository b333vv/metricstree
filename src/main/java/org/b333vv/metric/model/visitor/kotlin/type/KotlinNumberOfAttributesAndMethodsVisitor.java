package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;
import com.intellij.psi.PsiElement;

import static org.b333vv.metric.model.metric.MetricType.SIZE2;

/**
 * Kotlin Number Of Attributes And Methods (SIZE2) Visitor.
 * 
 * <p>This visitor calculates the SIZE2 metric for Kotlin classes, which represents
 * the total number of attributes (properties) and methods (functions) in a class.
 * 
 * <h3>What is counted as ATTRIBUTES:</h3>
 * <ul>
 *   <li>Properties declared in the class body (val/var)</li>
 *   <li>Primary constructor parameters with val/var modifiers</li>
 *   <li>Properties with custom getters/setters (counted as attributes, with accessors counted as methods)</li>
 *   <li>Delegated properties (by lazy, by Delegates, etc.)</li>
 *   <li>Late-initialized properties (lateinit var)</li>
 * </ul>
 * 
 * <h3>What is counted as METHODS:</h3>
 * <ul>
 *   <li>Named functions declared in the class (fun)</li>
 *   <li>Primary constructor (if present)</li>
 *   <li>Secondary constructors (constructor)</li>
 *   <li>Custom property getters (get())</li>
 *   <li>Custom property setters (set())</li>
 *   <li>Operator functions (operator fun)</li>
 *   <li>Infix functions (infix fun)</li>
 * </ul>
 * 
 * <h3>What is NOT counted:</h3>
 * <ul>
 *   <li>Companion object members (treated as static, not instance members)</li>
 *   <li>Nested/inner class declarations (counted separately)</li>
 *   <li>Init blocks (initialization code, not methods)</li>
 *   <li>Anonymous initializers</li>
 *   <li>Type aliases</li>
 *   <li>Default property accessors (synthetic getters/setters without custom implementation)</li>
 * </ul>
 * 
 * <h3>Examples:</h3>
 * <pre>
 * class Example(val x: Int) {              // x counts as 1 attribute, constructor as 1 method
 *     var y: String = ""                    // y counts as 1 attribute
 *     
 *     val computed: Int                     // computed counts as 1 attribute
 *         get() = x * 2                     // custom getter counts as 1 method
 *     
 *     var mutable: String = ""              // mutable counts as 1 attribute
 *         get() = field.uppercase()         // custom getter counts as 1 method
 *         set(value) { field = value }      // custom setter counts as 1 method
 *     
 *     fun process() {}                      // counts as 1 method
 *     
 *     companion object {
 *         val CONSTANT = 42                 // NOT counted (companion member)
 *         fun create() = Example(0)         // NOT counted (companion member)
 *     }
 * }
 * // Total SIZE2 = 3 attributes + 5 methods = 8
 * </pre>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#SIZE2
 */
public class KotlinNumberOfAttributesAndMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int attributes = 0;
        int methods = 0;

        // Count primary constructor and its properties
        KtPrimaryConstructor primaryConstructor = klass.getPrimaryConstructor();
        if (primaryConstructor != null) {
            methods++; // Primary constructor counts as a method
            
            // Count properties declared in primary constructor (val/var parameters)
            for (KtParameter parameter : primaryConstructor.getValueParameters()) {
                if (parameter.hasValOrVar()) {
                    attributes++;
                }
            }
        }

        // Process class body declarations
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration declaration : body.getDeclarations()) {
                if (declaration instanceof KtProperty) {
                    KtProperty property = (KtProperty) declaration;
                    
                    // Exclude properties in companion objects (static members)
                    if (!isInCompanionObject(property)) {
                        attributes++; // Count the property itself
                        
                        // Count custom accessors as methods
                        KtPropertyAccessor getter = property.getGetter();
                        if (getter != null && getter.hasBody()) {
                            methods++; // Custom getter with implementation
                        }
                        
                        KtPropertyAccessor setter = property.getSetter();
                        if (setter != null && setter.hasBody()) {
                            methods++; // Custom setter with implementation
                        }
                    }
                } else if (declaration instanceof KtNamedFunction) {
                    // Count all named functions (regular, operator, infix, etc.)
                    methods++;
                } else if (declaration instanceof KtSecondaryConstructor) {
                    // Count secondary constructors
                    methods++;
                }
                // Note: KtClassInitializer (init blocks) are NOT counted as methods
                // Note: Nested/inner classes are NOT counted here (processed separately)
            }
        }
        
        metric = Metric.of(SIZE2, (long) attributes + methods);
    }

    /**
     * Checks if a property is declared inside a companion object.
     * Companion object members are treated as static and not counted in SIZE2.
     * 
     * @param property the property to check
     * @return true if the property is inside a companion object, false otherwise
     */
    private boolean isInCompanionObject(@NotNull KtProperty property) {
        PsiElement parent = property.getParent();
        
        // Navigate up to find KtClassBody
        while (parent != null && !(parent instanceof KtClassBody)) {
            parent = parent.getParent();
        }
        
        if (parent instanceof KtClassBody) {
            PsiElement possibleCompanion = parent.getParent();
            
            // Check if the parent of class body is a companion object
            if (possibleCompanion instanceof KtObjectDeclaration) {
                return ((KtObjectDeclaration) possibleCompanion).isCompanion();
            }
        }
        
        return false;
    }
}
