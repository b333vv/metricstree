package org.b333vv.metric.model.javaparser.visitor;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.b333vv.metric.model.metric.Metric;
import java.util.function.Consumer;

public abstract class JavaParserClassVisitor extends VoidVisitorAdapter<Consumer<Metric>> {
}
