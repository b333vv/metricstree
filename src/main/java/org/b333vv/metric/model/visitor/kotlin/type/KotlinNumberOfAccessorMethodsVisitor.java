package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOAC;

/**
 * Visitor for calculating the Number Of Accessor Methods (NOAC) metric for Kotlin classes.
 * 
 * <p>The NOAC metric measures the total number of property accessor methods (getters and setters)
 * in a class. In Kotlin, this includes both explicit and implicit accessors that generate actual
 * bytecode methods.</p>
 * 
 * <h3>What is counted:</h3>
 * <ul>
 *   <li><b>Custom getter bodies:</b> Properties with explicitly defined getter implementation
 *       (e.g., {@code get() = field * 2})</li>
 *   <li><b>Custom setter bodies:</b> Properties with explicitly defined setter implementation
 *       (e.g., {@code set(value) { field = value.trim() }})</li>
 *   <li><b>Delegated properties:</b> Properties using delegation via the {@code by} keyword,
 *       which generate accessor methods delegating to the delegate object</li>
 *   <li><b>Accessors with custom visibility:</b> Getters or setters with explicit visibility
 *       modifiers (e.g., {@code private set}), as they generate separate methods</li>
 *   <li><b>Annotated accessors:</b> Getters or setters with annotations (e.g., {@code @JvmName}),
 *       which typically generate distinct accessor methods</li>
 *   <li><b>Lateinit properties:</b> {@code lateinit var} properties generate special accessor
 *       methods with initialization checks</li>
 *   <li><b>Companion object properties:</b> All properties declared in companion objects,
 *       as they generate static accessor methods</li>
 * </ul>
 * 
 * <h3>What is NOT counted:</h3>
 * <ul>
 *   <li>Simple properties with default (implicit) getters and setters without any customization</li>
 *   <li>Constructor parameters without property delegation</li>
 *   <li>Local variables within methods</li>
 * </ul>
 * 
 * <h3>Rationale:</h3>
 * <p>This metric helps identify classes with high accessor complexity. Kotlin's property syntax
 * can hide the fact that accessor methods are being generated. A high NOAC value may indicate:</p>
 * <ul>
 *   <li>Complex property logic that could be refactored into separate methods</li>
 *   <li>Overuse of property delegation</li>
 *   <li>Potential performance overhead from accessor method calls</li>
 *   <li>Testing complexity due to numerous entry points</li>
 * </ul>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#NOAC
 * @see KotlinClassVisitor
 */
public class KotlinNumberOfAccessorMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;
        
        // Process main class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            count += countAccessorsInDeclarations(body.getDeclarations());
        }
        
        // Process companion object
        KtObjectDeclaration companionObject = klass.getCompanionObjects().stream().findFirst().orElse(null);
        if (companionObject != null) {
            KtClassBody companionBody = companionObject.getBody();
            if (companionBody != null) {
                // All properties in companion objects generate accessor methods (static in JVM)
                count += countAccessorsInDeclarations(companionBody.getDeclarations());
            }
        }
        
        metric = Metric.of(NOAC, count);
    }
    
    /**
     * Counts accessor methods in a list of declarations.
     * 
     * @param declarations the list of class member declarations
     * @return the number of accessor methods
     */
    private int countAccessorsInDeclarations(java.util.List<KtDeclaration> declarations) {
        int count = 0;
        
        for (KtDeclaration d : declarations) {
            if (d instanceof KtProperty) {
                KtProperty property = (KtProperty) d;
                count += countPropertyAccessors(property);
            }
        }
        
        return count;
    }
    
    /**
     * Counts accessor methods for a single property.
     * 
     * @param property the property to analyze
     * @return the number of accessor methods for this property
     */
    private int countPropertyAccessors(KtProperty property) {
        int count = 0;
        
        // Check for delegated property (generates both getter and setter if var)
        if (property.getDelegateExpression() != null) {
            count++; // getter always present for delegated properties
            if (property.isVar()) {
                count++; // setter for mutable delegated properties
            }
            return count;
        }
        
        // Check for lateinit (generates special accessor with initialization check)
        if (property.hasModifier(KtTokens.LATEINIT_KEYWORD)) {
            count++; // getter with isInitialized check
            if (property.isVar()) {
                count++; // setter
            }
            return count;
        }
        
        // Check getter
        KtPropertyAccessor getter = property.getGetter();
        if (getter != null) {
            // Count if: has custom body, has custom visibility, or has annotations
            if (getter.getBodyExpression() != null 
                    || getter.getBodyBlockExpression() != null
                    || hasCustomVisibility(getter)
                    || hasAnnotations(getter)) {
                count++;
            }
        }
        
        // Check setter
        KtPropertyAccessor setter = property.getSetter();
        if (setter != null) {
            // Count if: has custom body, has custom visibility, or has annotations
            if (setter.getBodyExpression() != null 
                    || setter.getBodyBlockExpression() != null
                    || hasCustomVisibility(setter)
                    || hasAnnotations(setter)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Checks if an accessor has custom visibility modifier.
     * 
     * @param accessor the property accessor
     * @return true if accessor has explicit visibility modifier
     */
    private boolean hasCustomVisibility(KtPropertyAccessor accessor) {
        return accessor.hasModifier(KtTokens.PRIVATE_KEYWORD)
                || accessor.hasModifier(KtTokens.PROTECTED_KEYWORD)
                || accessor.hasModifier(KtTokens.INTERNAL_KEYWORD)
                || accessor.hasModifier(KtTokens.PUBLIC_KEYWORD);
    }
    
    /**
     * Checks if an accessor has annotations.
     * 
     * @param accessor the property accessor
     * @return true if accessor has any annotations
     */
    private boolean hasAnnotations(KtPropertyAccessor accessor) {
        return !accessor.getAnnotationEntries().isEmpty();
    }
}
