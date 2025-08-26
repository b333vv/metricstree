package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.Optional;
import java.util.function.Consumer;

public class JavaParserLinesOfCodeVisitor extends JavaParserMethodVisitor {

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        long linesOfCode = 0;
        Optional<BlockStmt> body = n.getBody();
        if (body.isPresent()) {
            if(body.get().getEnd().isPresent() && body.get().getBegin().isPresent()){
                linesOfCode = body.get().getEnd().get().line - body.get().getBegin().get().line + 1;
            }
        } else {
            if(n.getEnd().isPresent() && n.getBegin().isPresent()){
                linesOfCode = n.getEnd().get().line - n.getBegin().get().line + 1;
            }
        }

        Metric metric = Metric.of(MetricType.LOC, Value.of(linesOfCode));
        collector.accept(metric);
    }
}
