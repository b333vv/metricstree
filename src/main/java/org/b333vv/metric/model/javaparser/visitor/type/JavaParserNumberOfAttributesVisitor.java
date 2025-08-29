package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserNumberOfAttributesVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        
        // Count both declared and inherited fields to match PSI's getAllFields() behavior
        long numberOfAttributes = countAllFields(n);
        Metric metric = Metric.of(MetricType.NOA, Value.of(numberOfAttributes));
        collector.accept(metric);
    }
    
    /**
     * Counts all fields (declared + inherited) to match PSI's getAllFields() semantic.
     * This aligns with PSI implementation which includes inherited fields.
     */
    private long countAllFields(ClassOrInterfaceDeclaration n) {
        Set<String> fieldNames = new HashSet<>();
        
        // Count declared fields
        List<FieldDeclaration> declaredFields = n.getFields();
        declaredFields.forEach(field -> 
            field.getVariables().forEach(var -> fieldNames.add(var.getNameAsString()))
        );
        
        try {
            // Attempt to resolve and count inherited fields
            if (n.resolve().getAllFields() != null) {
                for (ResolvedFieldDeclaration resolvedField : n.resolve().getAllFields()) {
                    fieldNames.add(resolvedField.getName());
                }
            }
        } catch (Exception e) {
            // If resolution fails, fallback to declared fields only
            // This maintains functionality when symbol resolution is unavailable
        }
        
        return fieldNames.size();
    }
}
