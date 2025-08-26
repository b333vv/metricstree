package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNumberOfPublicAttributesVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        long numberOfPublicAttributes = n.getFields().stream().filter(f -> f.isPublic()).count();
        Metric metric = Metric.of(MetricType.NOPA, Value.of(numberOfPublicAttributes));
        collector.accept(metric);
    }
}
