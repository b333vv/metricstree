package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNumberOfParametersVisitor extends JavaParserMethodVisitor {

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        long numberOfParameters = n.getParameters().size();
        Metric metric = Metric.of(MetricType.NOPM, Value.of(numberOfParameters));
        collector.accept(metric);
    }
}
