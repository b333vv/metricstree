package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;
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

            // Count all non-static fields including inherited ones (mirror PSI getAllFields)
            long attributes = 0;
            // Fields declared in the current class
            for (ResolvedFieldDeclaration f : resolvedClass.getDeclaredFields()) {
                if (!f.isStatic()) {
                    attributes++;
                }
            }
            // Fields declared in all ancestors (including Object if any; Object has none)
            for (ResolvedReferenceType ancestor : resolvedClass.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (opt.isPresent()) {
                    ResolvedReferenceTypeDeclaration ad = opt.get();
                    for (ResolvedFieldDeclaration f : ad.getDeclaredFields()) {
                        if (!f.isStatic()) {
                            attributes++;
                        }
                    }
                }
            }
            
            // Count all non-static methods including inherited ones (mirror PSI getAllMethods). Do NOT count constructors.
            long methods = 0;
            for (MethodUsage m : resolvedClass.getAllMethods()) {
                if (!m.getDeclaration().isStatic()) {
                    methods++;
                }
            }

            size2 = attributes + methods;
        } catch (Throwable e) {
            // Fallback: count declared elements only (resolution failed)
            long attributes = n.getFields().stream()
                    .filter(f -> !f.isStatic())
                    .mapToLong(f -> f.getVariables().size())
                    .sum();
            
            long methods = n.getMethods().stream()
                    .filter(m -> !m.isStatic())
                    .count();
            
            size2 = attributes + methods;
        }

        Metric metric = Metric.of(MetricType.SIZE2, Value.of(size2));
        collector.accept(metric);
    }
}