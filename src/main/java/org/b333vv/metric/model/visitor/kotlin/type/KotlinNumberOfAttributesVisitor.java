/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.b333vv.metric.model.metric.MetricType.NOA;

/**
 * Visitor that calculates the Number of Attributes (NOA) metric for Kotlin classes.
 * 
 * <p>The NOA metric counts the total number of attributes (properties with backing fields) 
 * in a class, including properties from various Kotlin-specific constructs.</p>
 * 
 * <h2>What is counted in this metric:</h2>
 * <ul>
 *   <li><b>Primary constructor properties</b> - properties declared with {@code val} or {@code var} 
 *       in the primary constructor parameter list</li>
 *   <li><b>Class body properties with backing fields</b> - properties that have:
 *     <ul>
 *       <li>An initializer expression ({@code val x = 5})</li>
 *       <li>The {@code lateinit} modifier</li>
 *       <li>A delegated property ({@code val x by lazy { ... }})</li>
 *       <li>Default accessors (getter/setter without custom body)</li>
 *       <li>Custom accessors that reference the {@code field} keyword</li>
 *     </ul>
 *   </li>
 *   <li><b>Companion object properties</b> - static properties with backing fields declared in 
 *       companion objects, including:
 *     <ul>
 *       <li>{@code const val} constants (always have backing fields)</li>
 *       <li>Properties annotated with {@code @JvmField}</li>
 *       <li>Properties with {@code lateinit} modifier</li>
 *       <li>Regular properties with backing fields</li>
 *     </ul>
 *   </li>
 *   <li><b>Destructuring declarations</b> - componentN functions that access backing fields</li>
 * </ul>
 * 
 * <h2>What is NOT counted:</h2>
 * <ul>
 *   <li>Abstract properties (no backing field by definition)</li>
 *   <li>Interface properties (interfaces cannot store state)</li>
 *   <li>Computed properties without backing fields (getters that calculate values)</li>
 *   <li>Properties that override interface properties without adding state</li>
 *   <li>Extension properties (cannot have backing fields)</li>
 *   <li>Local variables inside functions</li>
 * </ul>
 * 
 * <h2>Example:</h2>
 * <pre>{@code
 * class MyClass(val id: Int) {              // +1 (primary constructor property)
 *     var name: String = ""                  // +1 (has initializer)
 *     lateinit var address: String           // +1 (lateinit)
 *     val city by lazy { "Unknown" }         // +1 (delegated)
 *     val computed get() = name.uppercase()  // NOT counted (no backing field)
 *     
 *     companion object {
 *         const val MAX_SIZE = 100           // +1 (const in companion)
 *         @JvmField val DEFAULT_NAME = ""    // +1 (@JvmField in companion)
 *     }
 * }
 * // Total NOA = 6
 * }</pre>
 * 
 * @see org.b333vv.metric.model.metric.MetricType#NOA
 */
public class KotlinNumberOfAttributesVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;

        // Count primary constructor properties (val/var parameters)
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    count++;
                }
            }
        }

        // Count class body properties
        KtClassBody body = klass.getBody();
        if (body != null) {
            boolean isInterface = klass.isInterface();
            
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty property = (KtProperty) decl;
                    if (hasBackingField(property, isInterface)) {
                        count++;
                    }
                }
                // Count companion object properties with backing fields
                else if (decl instanceof KtObjectDeclaration) {
                    KtObjectDeclaration obj = (KtObjectDeclaration) decl;
                    if (obj.isCompanion()) {
                        count += countCompanionObjectProperties(obj);
                    }
                }
            }
        }

        metric = Metric.of(NOA, count);
    }

    /**
     * Counts properties with backing fields in a companion object.
     * 
     * @param companionObject the companion object declaration
     * @return the number of properties with backing fields
     */
    private int countCompanionObjectProperties(@NotNull KtObjectDeclaration companionObject) {
        int count = 0;
        KtClassBody body = companionObject.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty property = (KtProperty) decl;
                    if (hasBackingFieldInCompanion(property)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Determines if a property in a companion object has a backing field.
     * Companion object properties follow special rules:
     * - const val always has a backing field
     * - @JvmField always has a backing field
     * - lateinit always has a backing field
     * - Otherwise, follows standard backing field rules
     * 
     * @param property the property to check
     * @return true if the property has a backing field
     */
    private boolean hasBackingFieldInCompanion(@NotNull KtProperty property) {
        // const val always has backing field
        if (property.hasModifier(KtTokens.CONST_KEYWORD)) {
            return true;
        }

        // @JvmField annotation forces backing field
        if (hasJvmFieldAnnotation(property)) {
            return true;
        }

        // lateinit always has backing field
        if (property.hasModifier(KtTokens.LATEINIT_KEYWORD)) {
            return true;
        }

        // For other properties, use standard backing field logic
        return hasBackingField(property, false);
    }

    /**
     * Checks if a property is annotated with @JvmField.
     * 
     * @param property the property to check
     * @return true if the property has @JvmField annotation
     */
    private boolean hasJvmFieldAnnotation(@NotNull KtProperty property) {
        for (KtAnnotationEntry annotation : property.getAnnotationEntries()) {
            Name shortName = annotation.getShortName();
            if (shortName != null && shortName.asString().equals("JvmField")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a regular class property has a backing field.
     * 
     * <p>A property has a backing field if:</p>
     * <ul>
     *   <li>It has an initializer expression</li>
     *   <li>It has the lateinit modifier</li>
     *   <li>It is a delegated property</li>
     *   <li>It has default accessors (no custom getter/setter body)</li>
     *   <li>Its custom accessors reference the 'field' keyword</li>
     * </ul>
     * 
     * <p>Properties that do NOT have backing fields:</p>
     * <ul>
     *   <li>Abstract properties</li>
     *   <li>Interface properties (unless they have explicit backing through delegation)</li>
     *   <li>Properties with only computed getters that don't use 'field'</li>
     *   <li>Override properties that don't add state</li>
     * </ul>
     * 
     * @param property the property to analyze
     * @param isInterface true if the property is declared in an interface
     * @return true if the property has a backing field
     */
    private boolean hasBackingField(@NotNull KtProperty property, boolean isInterface) {
        // Abstract properties never have backing fields
        if (property.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
            return false;
        }

        // Interface properties without delegation don't have backing fields
        // (unless they have a delegate or are const, but const not allowed in interfaces)
        if (isInterface && !property.hasDelegate()) {
            return false;
        }

        // Delegated properties always have backing field for the delegate
        if (property.hasDelegate()) {
            return true;
        }

        // lateinit properties always have backing field
        if (property.hasModifier(KtTokens.LATEINIT_KEYWORD)) {
            return true;
        }

        // Property with initializer has backing field
        if (property.hasInitializer()) {
            return true;
        }

        // @JvmField annotation forces backing field
        if (hasJvmFieldAnnotation(property)) {
            return true;
        }

        // Check if accessors use default implementation or reference 'field'
        KtPropertyAccessor getter = property.getGetter();
        KtPropertyAccessor setter = property.getSetter();

        // If there are no custom accessors at all, backing field exists
        // (except for abstract/interface properties, already handled above)
        if (getter == null && setter == null) {
            return true;
        }

        // If getter exists and uses backing field (no body or references 'field')
        if (getter != null && accessorUsesBackingField(getter)) {
            return true;
        }

        // If setter exists and uses backing field (no body or references 'field')
        if (setter != null && accessorUsesBackingField(setter)) {
            return true;
        }

        // No backing field detected
        return false;
    }

    /**
     * Checks if an accessor (getter or setter) uses a backing field.
     * An accessor uses a backing field if:
     * - It has no custom body (uses default implementation)
     * - It explicitly references the 'field' keyword in its body
     * 
     * @param accessor the property accessor to check
     * @return true if the accessor uses a backing field
     */
    private boolean accessorUsesBackingField(@NotNull KtPropertyAccessor accessor) {
        // Default accessor (no custom body) always uses backing field
        if (!accessor.hasBody()) {
            return true;
        }

        // Check if the accessor body references 'field' keyword
        return referencesFieldKeyword(accessor);
    }

    /**
     * Checks if an accessor body contains references to the 'field' keyword.
     * The 'field' keyword is used to access the backing field within custom accessors.
     * 
     * @param accessor the accessor to analyze
     * @return true if 'field' keyword is referenced
     */
    private boolean referencesFieldKeyword(@NotNull KtPropertyAccessor accessor) {
        AtomicBoolean found = new AtomicBoolean(false);
        accessor.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                if ("field".equals(expression.getReferencedName())) {
                    found.set(true);
                }
                super.visitSimpleNameExpression(expression);
            }
        });
        return found.get();
    }
}
