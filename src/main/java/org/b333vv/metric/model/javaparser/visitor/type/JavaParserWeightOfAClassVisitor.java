package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.type.VoidType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.type.CohesionUtils;

import java.util.function.Consumer;
import java.util.Set;

public class JavaParserWeightOfAClassVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        // Denominator: all declared methods (constructors are separate nodes, so all MethodDeclaration count)
        long totalMethods = n.getMethods().size();

        // Numerator: functional methods only
        long functionalMethods = n.getMethods().stream()
                .filter(this::isFunctional)
                .count();

        double woc = 0.0;
        if (totalMethods > 0) {
            woc = (double) functionalMethods / (double) totalMethods;
        }

        Metric metric = Metric.of(MetricType.WOC, Value.of(woc));
        collector.accept(metric);
    }

    private boolean isFunctional(MethodDeclaration m) {
        if (isAccessor(m)) return false;
        if (isBoilerplate(m)) return false;
        if (isTrivial(m)) return false;
        return true;
    }

    private boolean isAccessor(MethodDeclaration m) {
        return isGetter(m) || isBooleanGetter(m) || isSetter(m);
    }

    private boolean isSetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("set")
                && m.getParameters().size() == 1
                && m.getType() instanceof VoidType;
    }

    private boolean isGetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("get")
                && m.getParameters().isEmpty()
                && !(m.getType() instanceof VoidType)
                && returnsFieldDirectly(m);
    }

    private boolean isBooleanGetter(MethodDeclaration m) {
        return m.getNameAsString().startsWith("is")
                && m.getParameters().isEmpty()
                && m.getType().isPrimitiveType() && m.getType().asPrimitiveType().toString().equals("boolean")
                && returnsFieldDirectly(m);
    }

    private boolean returnsFieldDirectly(MethodDeclaration m) {
        return m.getBody()
                .flatMap(this::singleStatement)
                .filter(Statement::isReturnStmt)
                .map(Statement::asReturnStmt)
                .flatMap(ReturnStmt::getExpression)
                .map(Expression::isNameExpr)
                .orElse(false);
    }

    private boolean isBoilerplate(MethodDeclaration m) {
        Set<String> boiler = CohesionUtils.getBoilerplateMethods();
        return boiler.contains(m.getNameAsString());
    }

    private boolean isTrivial(MethodDeclaration m) {
        // No body or empty -> trivial
        if (m.getBody().isEmpty()) return true;
        BlockStmt body = m.getBody().get();
        if (body.getStatements().isEmpty()) return true;
        if (body.getStatements().size() > 1) return false;

        Statement s = body.getStatement(0);
        if (s.isReturnStmt()) {
            return s.asReturnStmt().getExpression()
                    .map(expr -> expr.isNameExpr() || expr.isMethodCallExpr())
                    .orElse(true);
        }
        if (s.isExpressionStmt()) {
            Expression e = s.asExpressionStmt().getExpression();
            if (e instanceof MethodCallExpr) return true; // delegate
            if (e instanceof AssignExpr) {
                AssignExpr ae = (AssignExpr) e;
                return ae.getTarget() instanceof NameExpr && ae.getValue() instanceof NameExpr; // x = y;
            }
        }
        return false;
    }

    private java.util.Optional<Statement> singleStatement(BlockStmt block) {
        if (block.getStatements().size() == 1) {
            return java.util.Optional.of(block.getStatement(0));
        }
        return java.util.Optional.empty();
    }
}
