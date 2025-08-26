package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNumberOfAccessorMethodsVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        long numberOfAccessorMethods = n.getMethods().stream().filter(this::isAccessor).count();
        Metric metric = Metric.of(MetricType.NOAC, Value.of(numberOfAccessorMethods));
        collector.accept(metric);
    }

    private boolean isAccessor(MethodDeclaration m) {
        return isGetter(m) || isSetter(m);
    }

    private boolean isGetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("get") && m.getParameters().isEmpty() && !m.getType().isVoidType();
    }

    private boolean isSetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("set") && m.getParameters().size() == 1 && m.getType().isVoidType();
    }
}
