package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.MethodUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaParserNumberOfAttributesAndMethodsVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long size2;
        try {
            ResolvedReferenceTypeDeclaration r = n.resolve();

            long operations = 0;
            for (MethodUsage m : r.getAllMethods()) {
                boolean declaredHere = m.declaringType().getQualifiedName().equals(r.getQualifiedName());
                if (declaredHere || !m.getDeclaration().isStatic()) {
                    operations++;
                }
            }

            long attributes = 0;
            // Fields declared in this class (only instance fields)
            for (ResolvedFieldDeclaration f : r.getDeclaredFields()) {
                if (!f.isStatic()) {
                    attributes++;
                }
            }
            
            // Fields declared in ancestors (only instance fields)
            for (ResolvedReferenceType ancestor : r.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (!opt.isPresent()) continue;
                ResolvedReferenceTypeDeclaration ad = opt.get();
                // Only count instance fields from ancestors
                for (ResolvedFieldDeclaration f : ad.getDeclaredFields()) {
                    if (!f.isStatic()) {
                        attributes++;
                    }
                }
            }

            size2 = operations + attributes;
        } catch (Throwable e) {
            // Fallback: only count instance fields and methods
            size2 = 0;
            // Count instance fields
            size2 += n.getFields().stream().filter(f -> !f.isStatic()).count();
            // Count instance methods
            size2 += n.getMethods().stream().filter(m -> !m.isStatic()).count();
        }

        Metric metric = Metric.of(MetricType.SIZE2, Value.of(size2));
        collector.accept(metric);
    }
}
