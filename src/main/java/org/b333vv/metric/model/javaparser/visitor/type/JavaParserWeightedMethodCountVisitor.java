package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.javaparser.visitor.method.JavaParserMcCabeCyclomaticComplexityVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JavaParserWeightedMethodCountVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long wmc = 0;
        for (MethodDeclaration method : n.getMethods()) {
            JavaParserMcCabeCyclomaticComplexityVisitor visitor = new JavaParserMcCabeCyclomaticComplexityVisitor();
            List<Metric> metrics = new ArrayList<>();
            visitor.visit(method, metrics::add);
            if (!metrics.isEmpty()) {
                wmc += metrics.get(0).getValue().longValue();
            }
        }
        for (ConstructorDeclaration constructor : n.getConstructors()) {
            JavaParserMcCabeCyclomaticComplexityVisitor visitor = new JavaParserMcCabeCyclomaticComplexityVisitor();
            List<Metric> metrics = new ArrayList<>();
            visitor.visit(constructor, metrics::add);
            if (!metrics.isEmpty()) {
                wmc += metrics.get(0).getValue().longValue();
            }
        }
        Metric metric = Metric.of(MetricType.WMC, Value.of(wmc));
        collector.accept(metric);
    }
}
