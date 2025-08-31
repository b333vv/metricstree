package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserCouplingBetweenObjectsVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> coupledClasses = new HashSet<>();
        
        // Enhanced CBO calculation with comprehensive method call analysis
        
        // 0. Import dependencies from the compilation unit
        Optional<CompilationUnit> cu = n.findCompilationUnit();
        if (cu.isPresent()) {
            for (ImportDeclaration importDecl : cu.get().getImports()) {
                try {
                    String importName = importDecl.getNameAsString();
                    // Add import as a dependency if it's a class import (not package import)
                    if (!importDecl.isAsterisk() && !importName.endsWith(".*")) {
                        coupledClasses.add(importName);
                    }
                } catch (Exception e) {
                    // Skip failed imports
                }
            }
        }
        
        // Find all types that this class depends on (outgoing dependencies)
        // 1. Type references from declarations
        n.walk(ClassOrInterfaceType.class, t -> {
            try {
                String resolvedName = t.resolve().asReferenceType().getQualifiedName();
                coupledClasses.add(resolvedName);

            } catch (Exception e) {
                // Unsolved symbol - skip
            }
        });
        
        // 2. Method call dependencies (enhanced for PSI alignment)
        n.walk(MethodCallExpr.class, methodCall -> {
            try {
                ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
                // Get the class that contains this method
                if (resolvedMethod.declaringType() instanceof ResolvedReferenceTypeDeclaration) {
                    ResolvedReferenceTypeDeclaration declaringType = 
                        (ResolvedReferenceTypeDeclaration) resolvedMethod.declaringType();
                    String qualifiedName = declaringType.getQualifiedName();
                    coupledClasses.add(qualifiedName);

                }
            } catch (Exception e) {
                // If method resolution fails, try to infer from scope
                try {
                    if (methodCall.getScope().isPresent()) {
                        // Handle cases like Objects.requireNonNull(), Comparator.comparing()
                        String scopeText = methodCall.getScope().get().toString();
                        String inferredType = inferTypeFromStaticCall(scopeText);
                        if (inferredType != null) {
                            coupledClasses.add(inferredType);

                        }
                    }
                } catch (Exception ignored) {
                    // Fallback failed, skip this method call
                }
            }
        });
        
        // 3. Object creation expressions
        n.walk(ObjectCreationExpr.class, objectCreation -> {
            try {
                String resolvedName = objectCreation.getType().resolve().asReferenceType().getQualifiedName();
                coupledClasses.add(resolvedName);

            } catch (Exception e) {
                // Unsolved symbol - skip
            }
        });

        // Remove the current class itself from the coupled classes set
        try {
            String currentClassName = n.resolve().getQualifiedName();
            coupledClasses.remove(currentClassName);
        } catch (Exception e) {
            // If we can't resolve the current class name, that's fine
            // We'll just rely on it not being in the set anyway
        }
        

        
        Metric metric = Metric.of(MetricType.CBO, Value.of(coupledClasses.size()));
        collector.accept(metric);
    }
    
    /**
     * Infer qualified type name from static method call patterns.
     * Handles common cases like Objects.method(), Comparator.method(), etc.
     */
    private String inferTypeFromStaticCall(String scopeText) {
        switch (scopeText) {
            case "Objects":
                return "java.util.Objects";
            case "Comparator":
                return "java.util.Comparator";
            case "Collections":
                return "java.util.Collections";
            case "Arrays":
                return "java.util.Arrays";
            case "String":
                return "java.lang.String";
            case "Math":
                return "java.lang.Math";
            case "System":
                return "java.lang.System";
            case "Optional":
                return "java.util.Optional";
            case "Stream":
                return "java.util.stream.Stream";
            default:
                return null;
        }
    }
}
