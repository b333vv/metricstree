package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserNumberOfAccessedVariablesVisitor extends JavaParserMethodVisitor {
    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        Set<String> accessedVariables = new HashSet<>();
        n.walk(NameExpr.class, ne -> {
            try {
                ResolvedValueDeclaration resolved = ne.resolve();
                if (resolved.isParameter() || resolved.isField() || resolved.isVariable()) {
                    accessedVariables.add(resolved.getName());
                }
            } catch (Exception e) {
                // ignore
            }
        });
        Metric metric = Metric.of(MetricType.NOAV, Value.of(accessedVariables.size()));
        collector.accept(metric);
    }
}
