package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserDataAbstractionCouplingVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> adt = new HashSet<>();
        for (FieldDeclaration field : n.getFields()) {
            field.getVariables().forEach(v -> {
                try {
                    ResolvedType resolvedType = v.getType().resolve();
                    if (resolvedType.isReferenceType()) {
                        adt.add(resolvedType.asReferenceType().getQualifiedName());
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
        }
        Metric metric = Metric.of(MetricType.DAC, Value.of(adt.size()));
        collector.accept(metric);
    }
}
