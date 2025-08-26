package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNumberOfLoopsVisitor extends JavaParserMethodVisitor {
    private long loops = 0;

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        loops = 0;
        super.visit(n, collector);
        Metric metric = Metric.of(MetricType.NOL, Value.of(loops));
        collector.accept(metric);
    }

    @Override
    public void visit(ForStmt n, Consumer<Metric> collector) {
        loops++;
        super.visit(n, collector);
    }

    @Override
    public void visit(WhileStmt n, Consumer<Metric> collector) {
        loops++;
        super.visit(n, collector);
    }

    @Override
    public void visit(DoStmt n, Consumer<Metric> collector) {
        loops++;
        super.visit(n, collector);
    }

    @Override
    public void visit(ForEachStmt n, Consumer<Metric> collector) {
        loops++;
        super.visit(n, collector);
    }
}
