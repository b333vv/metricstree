package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserCouplingIntensityVisitor extends JavaParserMethodVisitor {
    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        Set<String> calledMethods = new HashSet<>();
        n.walk(MethodCallExpr.class, mce -> {
            try {
                calledMethods.add(mce.resolve().getQualifiedSignature());
            } catch (Exception e) {
                // ignore
            }
        });
        Metric metric = Metric.of(MetricType.CINT, Value.of(calledMethods.size()));
        collector.accept(metric);
    }
}
