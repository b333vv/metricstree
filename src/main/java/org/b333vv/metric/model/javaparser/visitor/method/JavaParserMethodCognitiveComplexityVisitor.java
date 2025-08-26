package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JavaParserMethodCognitiveComplexityVisitor extends JavaParserMethodVisitor {
    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        JavaParserCognitiveComplexityVisitor visitor = new JavaParserCognitiveComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(n, metrics::add);
        if (!metrics.isEmpty()) {
            collector.accept(Metric.of(MetricType.CCM, metrics.get(0).getValue()));
        } else {
            collector.accept(Metric.of(MetricType.CCM, Value.UNDEFINED));
        }
    }
}
