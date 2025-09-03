package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.MethodUsage;

public class JavaParserNumberOfAttributesAndMethodsVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long size2;
        
        try {
            ResolvedReferenceTypeDeclaration resolvedClass = n.resolve();

            // Count all non-static fields including inherited ones
            Set<String> countedFields = new HashSet<>();
            long attributes = 0;
            
            // Add fields from current class
            for (ResolvedFieldDeclaration f : resolvedClass.getDeclaredFields()) {
                if (!f.isStatic() && !countedFields.contains(f.getName())) {
                    countedFields.add(f.getName());
                    attributes++;
                }
            }
            
            // Add fields from ancestors (but not Object class)
            for (ResolvedReferenceType ancestor : resolvedClass.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (opt.isPresent()) {
                    ResolvedReferenceTypeDeclaration ad = opt.get();
                    // Skip Object class
                    if (!"java.lang.Object".equals(ad.getQualifiedName())) {
                        for (ResolvedFieldDeclaration f : ad.getDeclaredFields()) {
                            if (!f.isStatic() && !countedFields.contains(f.getName())) {
                                countedFields.add(f.getName());
                                attributes++;
                            }
                        }
                    }
                }
            }
            
            // Count all non-static methods including inherited ones and constructors
            Set<String> countedMethods = new HashSet<>();
            long methods = 0;
            
            // Add constructors from current class
            methods += resolvedClass.getConstructors().size();
            
            // Add constructors from ancestors (but not Object class)
            for (ResolvedReferenceType ancestor : resolvedClass.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (opt.isPresent()) {
                    ResolvedReferenceTypeDeclaration ad = opt.get();
                    // Skip Object class
                    if (!"java.lang.Object".equals(ad.getQualifiedName())) {
                        methods += ad.getConstructors().size();
                    }
                }
            }
            
            // Add methods using getAllMethods to get inherited methods, but exclude Object methods and constructors
            for (MethodUsage m : resolvedClass.getAllMethods()) {
                // Skip methods from Object class
                if ("java.lang.Object".equals(m.getDeclaration().declaringType().getQualifiedName())) {
                    continue;
                }
                
                // Skip constructors (they are handled separately)
                if (m.getName().equals("<init>")) {
                    continue;
                }
                
                // Only count methods that are not static and not already counted
                String methodSignature = m.getDeclaration().getQualifiedSignature();
                if (!m.getDeclaration().isStatic() && !countedMethods.contains(methodSignature)) {
                    countedMethods.add(methodSignature);
                    methods++;
                }
            }

            size2 = attributes + methods;
        } catch (Throwable e) {
            // Fallback: count declared elements only
            long attributes = n.getFields().stream()
                    .filter(f -> !f.isStatic())
                    .mapToLong(f -> f.getVariables().size())
                    .sum();
            
            long methods = n.getMethods().stream()
                    .filter(m -> !m.isStatic())
                    .count();
            
            long constructors = n.getConstructors().size();
            
            size2 = attributes + methods + constructors;
        }

        Metric metric = Metric.of(MetricType.SIZE2, Value.of(size2));
        collector.accept(metric);
    }
}