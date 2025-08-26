package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserCouplingDispersionVisitor extends JavaParserMethodVisitor {
    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        Set<Integer> depths = new HashSet<>();
        n.walk(MethodCallExpr.class, mce -> {
            try {
                ResolvedReferenceTypeDeclaration declaringType = mce.resolve().declaringType();
                if (declaringType.isClass()) {
                    depths.add(getDepth(declaringType));
                }
            } catch (Exception e) {
                // ignore
            }
        });
        Metric metric = Metric.of(MetricType.CDISP, Value.of(depths.size()));
        collector.accept(metric);
    }

    private int getDepth(ResolvedReferenceTypeDeclaration type) {
        int depth = 1;
        List<ResolvedReferenceType> ancestors = type.getAllAncestors();
        for (ResolvedReferenceType ancestor : ancestors) {
            if (ancestor.getTypeDeclaration().isPresent() && ancestor.getTypeDeclaration().get().isClass() && !ancestor.getQualifiedName().equals("java.lang.Object")) {
                depth++;
            }
        }
        return depth;
    }
}
