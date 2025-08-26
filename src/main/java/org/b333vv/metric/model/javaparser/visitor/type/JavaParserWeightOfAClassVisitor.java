package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserWeightOfAClassVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long publicMethods = n.getMethods().stream().filter(MethodDeclaration::isPublic).count();
        long accessorMethods = n.getMethods().stream()
                .filter(MethodDeclaration::isPublic)
                .filter(this::isAccessor)
                .count();
        long publicFields = n.getFields().stream().filter(FieldDeclaration::isPublic).count();

        double woc = 0;
        if (publicMethods + publicFields > 0) {
            woc = (double) (publicMethods - accessorMethods) / (publicMethods + publicFields);
        }

        Metric metric = Metric.of(MetricType.WOC, Value.of(woc));
        collector.accept(metric);
    }

    private boolean isAccessor(MethodDeclaration m) {
        return isGetter(m) || isSetter(m);
    }

    private boolean isGetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("get")
                && m.getParameters().isEmpty()
                && !m.getType().isVoidType();
    }

    private boolean isSetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("set")
                && m.getParameters().size() == 1
                && m.getType().isVoidType();
    }
}
