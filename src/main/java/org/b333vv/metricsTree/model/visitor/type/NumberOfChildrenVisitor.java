package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.b333vv.metricsTree.model.metric.value.Value;
import org.b333vv.metricsTree.model.metric.Metric;

public class NumberOfChildrenVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("NOC", "Number Of Children",
                "/html/NumberOfChildren.html", Value.UNDEFINED);
        if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                psiClass.isInterface() ||
                psiClass.isEnum()
        )) {
            metric = Metric.of("NOC", "Number Of Children", "/html/NumberOfChildren.html",
                    ClassInheritorsSearch.search(psiClass, false).findAll().size());
        }
    }
}