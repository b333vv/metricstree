package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JavaParserMessagePassingCouplingVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        List<MethodCallExpr> mpc = new ArrayList<>();
        try {
            String currentClassName = n.resolve().getQualifiedName();
            n.walk(MethodCallExpr.class, mce -> {
                try {
                    String declaringClassName = mce.resolve().declaringType().getQualifiedName();
                    if (!declaringClassName.equals(currentClassName)) {
                        mpc.add(mce);
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
        } catch (Exception e) {
            // ignore
        }
        Metric metric = Metric.of(MetricType.MPC, Value.of(mpc.size()));
        collector.accept(metric);
    }
}
