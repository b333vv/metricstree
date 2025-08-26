package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserDepthOfInheritanceTreeVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        int depth = getDepth(n);
        Metric metric = Metric.of(MetricType.DIT, Value.of(depth));
        collector.accept(metric);
    }

    private int getDepth(ClassOrInterfaceDeclaration n) {
        if (n.getExtendedTypes().isEmpty()) {
            return 1; // Extends Object
        }
        try {
            ResolvedReferenceTypeDeclaration resolved = n.getExtendedTypes(0).resolve().asReferenceType().getTypeDeclaration().get();
            if (resolved instanceof JavaParserClassDeclaration) {
                return 1 + getDepth(((JavaParserClassDeclaration) resolved).getWrappedNode());
            } else {
                return 2;
            }
        } catch (Exception e) {
            return 1;
        }
    }
}
