package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.List;
import java.util.function.Consumer;

public class JavaParserNumberOfAttributesVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        
        // Count both declared and inherited fields to match PSI's PsiClass#getAllFields() behavior
        long numberOfAttributes = countAllFields(n);
        Metric metric = Metric.of(MetricType.NOA, Value.of(numberOfAttributes));
        collector.accept(metric);
    }
    
    /**
     * Counts all fields (declared + inherited) to match PSI's PsiClass#getAllFields() semantic.
     * No deduplication by name.
     */
    private long countAllFields(ClassOrInterfaceDeclaration n) {
        try {
            // Prefer resolved model: includes declared + inherited fields
            long resolvedCount = n.resolve().getAllFields().size();
            long declared = 0;
            for (FieldDeclaration fd : n.getFields()) {
                declared += fd.getVariables().size();
            }
            if (resolvedCount <= declared) {
                // Likely unresolved ancestors; try reflection and take the larger count
                try {
                    String fqn = n.getFullyQualifiedName().orElseGet(() -> {
                        String pkg = n.findCompilationUnit()
                                .flatMap(cu -> cu.getPackageDeclaration().map(pd -> pd.getNameAsString()))
                                .orElse("");
                        String name = n.getNameAsString();
                        String prefix = pkg.isEmpty() ? "" : pkg + ".";
                        return prefix + name;
                    });
                    if (fqn != null) {
                        Class<?> cls = null;
                        try {
                            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                            if (tccl != null) {
                                cls = Class.forName(fqn, false, tccl);
                            }
                        } catch (Throwable ignored2) { }
                        if (cls == null) {
                            ClassLoader cl = this.getClass().getClassLoader();
                            cls = Class.forName(fqn, false, cl);
                        }
                        long reflectCount = countFieldsByReflection(cls);
                        return Math.max(resolvedCount, reflectCount);
                    }
                } catch (Throwable ignored3) { }
            }
            return resolvedCount;
        } catch (Exception e) {
            // Try reflection-based fallback using FQN to include external library ancestors
            try {
                String fqn = n.getFullyQualifiedName().orElseGet(() -> {
                    // Build FQN from CU package + nested type names if unavailable
                    String pkg = n.findCompilationUnit()
                            .flatMap(cu -> cu.getPackageDeclaration().map(pd -> pd.getNameAsString()))
                            .orElse("");
                    String name = n.getNameAsString();
                    String prefix = pkg.isEmpty() ? "" : pkg + ".";
                    return prefix + name;
                });
                if (fqn != null) {
                    // Try TCCL first, then fall back to plugin classloader
                    Class<?> cls = null;
                    try {
                        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                        if (tccl != null) {
                            cls = Class.forName(fqn, false, tccl);
                        }
                    } catch (Throwable ignored2) { }
                    if (cls == null) {
                        ClassLoader cl = this.getClass().getClassLoader();
                        cls = Class.forName(fqn, false, cl);
                    }
                    return countFieldsByReflection(cls);
                }
            } catch (Throwable ignored) {
                // ignore and fallback to declared-only below
            }
            // Fallback: count only declared variables when resolution is unavailable
            long declared = 0;
            for (FieldDeclaration fd : n.getFields()) {
                declared += fd.getVariables().size();
            }
            return declared;
        }
    }

    private long countFieldsByReflection(Class<?> cls) {
        long count = 0;
        // Traverse class hierarchy
        Class<?> current = cls;
        while (current != null) {
            try {
                count += current.getDeclaredFields().length;
            } catch (Throwable ignored) { }
            // include fields from directly implemented interfaces at each level
            for (Class<?> itf : safeGetInterfaces(current)) {
                try {
                    count += itf.getDeclaredFields().length;
                } catch (Throwable ignored) { }
                count += countInterfaceHierarchyFields(itf);
            }
            current = current.getSuperclass();
        }
        return count;
    }

    private long countInterfaceHierarchyFields(Class<?> itf) {
        long count = 0;
        for (Class<?> superItf : safeGetInterfaces(itf)) {
            try {
                count += superItf.getDeclaredFields().length;
            } catch (Throwable ignored) { }
            count += countInterfaceHierarchyFields(superItf);
        }
        return count;
    }

    private Class<?>[] safeGetInterfaces(Class<?> c) {
        try {
            return c.getInterfaces();
        } catch (Throwable e) {
            return new Class<?>[0];
        }
    }
}
