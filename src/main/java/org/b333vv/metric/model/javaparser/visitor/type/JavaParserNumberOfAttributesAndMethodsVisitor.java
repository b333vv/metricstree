package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.MethodUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaParserNumberOfAttributesAndMethodsVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long size2;
        try {
            ResolvedReferenceTypeDeclaration r = n.resolve();

            long operations = 0;
            // Single-pass count over all visible methods with PSI rule; do NOT dedupe
            Set<String> seenDeclTypes = new HashSet<>();
            for (MethodUsage mu : r.getAllMethods()) {
                String declQN = mu.declaringType().getQualifiedName();
                boolean declaredHere = declQN.equals(r.getQualifiedName());
                boolean nonStatic = !mu.getDeclaration().isStatic();
                if (declaredHere || nonStatic) {
                    operations++;
                }
                if (!declaredHere && nonStatic) {
                    seenDeclTypes.add(declQN);
                }
            }

            long attributes = 0;
            // Fields declared in this class
            for (ResolvedFieldDeclaration f : r.getDeclaredFields()) {
                attributes++; // count regardless of static (declared here)
            }
            // Fields declared in ancestors (count only non-static ones)
            for (ResolvedReferenceType ancestor : r.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (!opt.isPresent()) continue;
                ResolvedReferenceTypeDeclaration ad = opt.get();
                for (ResolvedFieldDeclaration f : ad.getDeclaredFields()) {
                    if (!f.isStatic()) {
                        attributes++;
                    }
                }
            }

            // Supplement for unresolved ancestors via reflection
            for (ResolvedReferenceType ancestor : r.getAncestors(true)) {
                Optional<ResolvedReferenceTypeDeclaration> opt = ancestor.getTypeDeclaration();
                if (opt.isPresent()) continue; // already handled
                String qn;
                try {
                    qn = ancestor.getQualifiedName();
                } catch (Throwable t) {
                    qn = ancestor.describe();
                }
                if (qn == null || qn.isEmpty()) continue;
                try {
                    Class<?> cls = Class.forName(qn);
                    // Methods: count non-static as inherited if not already seen from symbol solver
                    if (!seenDeclTypes.contains(qn)) {
                        for (Method m : cls.getDeclaredMethods()) {
                            if (!Modifier.isStatic(m.getModifiers())) {
                                operations++;
                            }
                        }
                    }
                    // Fields: count declared non-static fields
                    for (Field f : cls.getDeclaredFields()) {
                        if (!Modifier.isStatic(f.getModifiers())) {
                            attributes++;
                        }
                    }
                } catch (Throwable ignore) {
                    // best-effort; ignore if class not loadable
                }
            }

            size2 = operations + attributes;
        } catch (Throwable e) {
            // Fallback: local members only (previous behavior)
            size2 = n.getFields().size() + n.getMethods().size();
        }

        Metric metric = Metric.of(MetricType.SIZE2, Value.of(size2));
        collector.accept(metric);
    }
}
