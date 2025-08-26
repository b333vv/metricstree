package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNumberOfOverriddenMethodsVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        long noom = 0;
        if (!n.isInterface()) {
            noom = n.getMethods().stream()
                    .filter(m -> m.getAnnotationByName("Override").isPresent())
                    .count();
        }
        Metric metric = Metric.of(MetricType.NOOM, Value.of(noom));
        collector.accept(metric);
    }
}
