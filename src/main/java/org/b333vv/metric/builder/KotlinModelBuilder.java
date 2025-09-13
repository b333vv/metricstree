/*
 * Copyright 2025 b333vv
 */
package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.model.visitor.kotlin.method.*;
import org.b333vv.metric.model.visitor.kotlin.type.*;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

/**
 * Kotlin-specific model builder extracted to avoid hard dependency from core classes at startup.
 */
public class KotlinModelBuilder extends ModelBuilder {

    private final Project project;

    public KotlinModelBuilder(Project project) {
        this.project = project;
    }

    protected FileElement createKotlinFile(@NotNull KtFile ktFile) {
        FileElement kotlinFile = new FileElement(ktFile.getName());
        Project project = ktFile.getProject();
        boolean anyClasses = false;
        for (KtDeclaration decl : ktFile.getDeclarations()) {
            if (decl instanceof KtClassOrObject) {
                KtClassOrObject ktClass = (KtClassOrObject) decl;
                ClassElement klass = new ClassElement(ktClass);

                // Apply Kotlin class-level visitors where applicable
                if (ktClass instanceof KtClass) {
                    if (isMetricEnabled(project, WMC)) {
                        KotlinWeightedMethodCountVisitor wmc = new KotlinWeightedMethodCountVisitor();
                        wmc.computeFor((KtClass) ktClass);
                        if (wmc.getMetric() != null) klass.addMetric(wmc.getMetric());
                    }
                    if (isMetricEnabled(project, NOM)) {
                        KotlinNumberOfMethodsVisitor nom = new KotlinNumberOfMethodsVisitor();
                        nom.computeFor((KtClass) ktClass);
                        if (nom.getMetric() != null) klass.addMetric(nom.getMetric());
                    }
                    if (isMetricEnabled(project, NOA)) {
                        KotlinNumberOfAttributesVisitor noa = new KotlinNumberOfAttributesVisitor();
                        noa.computeFor((KtClass) ktClass);
                        if (noa.getMetric() != null) klass.addMetric(noa.getMetric());
                    }
                    if (isMetricEnabled(project, NCSS)) {
                        KotlinNonCommentingSourceStatementsVisitor ncss = new KotlinNonCommentingSourceStatementsVisitor();
                        ncss.computeFor((KtClass) ktClass);
                        if (ncss.getMetric() != null) klass.addMetric(ncss.getMetric());
                    }
                    if (isMetricEnabled(project, RFC)) {
                        KotlinResponseForClassVisitor rfc = new KotlinResponseForClassVisitor();
                        rfc.computeFor((KtClass) ktClass);
                        if (rfc.getMetric() != null) klass.addMetric(rfc.getMetric());
                    }
                    if (isMetricEnabled(project, CBO)) {
                        KotlinCouplingBetweenObjectsVisitor cbo = new KotlinCouplingBetweenObjectsVisitor();
                        cbo.computeFor((KtClass) ktClass);
                        if (cbo.getMetric() != null) klass.addMetric(cbo.getMetric());
                    }
                    if (isMetricEnabled(project, LCOM)) {
                        KotlinLackOfCohesionOfMethodsVisitor lcom = new KotlinLackOfCohesionOfMethodsVisitor();
                        lcom.computeFor((KtClass) ktClass);
                        if (lcom.getMetric() != null) klass.addMetric(lcom.getMetric());
                    }
                    if (isMetricEnabled(project, DIT)) {
                        KotlinDepthOfInheritanceTreeVisitor dit = new KotlinDepthOfInheritanceTreeVisitor();
                        dit.computeFor((KtClass) ktClass);
                        if (dit.getMetric() != null) klass.addMetric(dit.getMetric());
                    }
                    if (isMetricEnabled(project, NOC)) {
                        KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
                        noc.computeFor((KtClass) ktClass);
                        if (noc.getMetric() != null) klass.addMetric(noc.getMetric());
                    }
                    if (isMetricEnabled(project, TCC)) {
                        KotlinTightClassCohesionVisitor tcc = new KotlinTightClassCohesionVisitor();
                        tcc.computeFor((KtClass) ktClass);
                        if (tcc.getMetric() != null) klass.addMetric(tcc.getMetric());
                    }
                }

                kotlinFile.addClass(klass);

                buildKotlinConstructors(klass, ktClass);
                buildKotlinFunctions(klass, ktClass);

                addMaintainabilityIndexForClass(klass);
                addLinesOfCodeIndexForClass(klass);
                addCognitiveComplexityForClass(klass);
                addToAllClasses(klass);
                anyClasses = true;
            }
        }
        // If no classes/objects declared, create a synthetic class to host top-level functions
        if (!anyClasses) {
            String syntheticName = ktFile.getName();
            ClassElement synthetic = new ClassElement(syntheticName);
            kotlinFile.addClass(synthetic);
            // top-level functions
            for (KtDeclaration d : ktFile.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    MethodElement m = new MethodElement(f, synthetic);
                    synthetic.addMethod(m);
                    try {
                        applyKotlinMethodVisitors(m, f);
                    } catch (Throwable t) {
                        project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                                .printInfo("[KotlinModelBuilder] Skipped method visitors for top-level function due to: " + t.getClass().getSimpleName());
                    }
                    addMaintainabilityIndexForMethod(m);
                }
            }
            addMaintainabilityIndexForClass(synthetic);
            addLinesOfCodeIndexForClass(synthetic);
            addCognitiveComplexityForClass(synthetic);
            addToAllClasses(synthetic);
        }
        return kotlinFile;
    }

    protected void buildKotlinConstructors(@NotNull ClassElement klass, @NotNull KtClassOrObject ktClass) {
        Project project = (klass.getPsiClass() != null) ? klass.getPsiClass().getProject() : ktClass.getProject();
        if (ktClass instanceof KtClass) {
            KtPrimaryConstructor primary = ((KtClass) ktClass).getPrimaryConstructor();
            if (primary != null) {
                MethodElement ctorEl = new MethodElement(primary, klass);
                klass.addMethod(ctorEl);
                try {
                    applyKotlinMethodVisitors(ctorEl, primary);
                } catch (Throwable t) {
                    project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                            .printInfo("[KotlinModelBuilder] Skipped visitors for primary ctor due to: " + t.getClass().getSimpleName());
                }
                addMaintainabilityIndexForMethod(ctorEl);
            }
            for (KtSecondaryConstructor s : ((KtClass) ktClass).getSecondaryConstructors()) {
                MethodElement ctorEl = new MethodElement(s, klass);
                klass.addMethod(ctorEl);
                try {
                    applyKotlinMethodVisitors(ctorEl, s);
                } catch (Throwable t) {
                    project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                            .printInfo("[KotlinModelBuilder] Skipped visitors for secondary ctor due to: " + t.getClass().getSimpleName());
                }
                addMaintainabilityIndexForMethod(ctorEl);
            }
        }
    }

    protected void buildKotlinFunctions(@NotNull ClassElement klass, @NotNull KtClassOrObject ktClass) {
        for (KtDeclaration d : ktClass.getDeclarations()) {
            if (d instanceof KtNamedFunction) {
                KtNamedFunction f = (KtNamedFunction) d;
                MethodElement m = new MethodElement(f, klass);
                klass.addMethod(m);
                try {
                    applyKotlinMethodVisitors(m, f);
                } catch (Throwable t) {
                    project.getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                            .printInfo("[KotlinModelBuilder] Skipped visitors for function due to: " + t.getClass().getSimpleName());
                }
                addMaintainabilityIndexForMethod(m);
            }
            if (d instanceof KtClassOrObject) {
                buildKotlinConstructors(klass, (KtClassOrObject) d);
                buildKotlinFunctions(klass, (KtClassOrObject) d);
            }
        }
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtNamedFunction function) {
        Project project = function.getProject();
        if (isMetricEnabled(project, LOC)) {
            KotlinLinesOfCodeVisitor loc = new KotlinLinesOfCodeVisitor();
            loc.computeFor(function); if (loc.getMetric()!=null) method.addMetric(loc.getMetric());
        }
        if (isMetricEnabled(project, MetricType.CC)) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(function); if (cc.getMetric()!=null) method.addMetric(cc.getMetric());
        }
        if (isMetricEnabled(project, CND)) {
            KotlinConditionNestingDepthVisitor cnd = new KotlinConditionNestingDepthVisitor();
            cnd.computeFor(function); if (cnd.getMetric()!=null) method.addMetric(cnd.getMetric());
        }
        if (isMetricEnabled(project, LND)) {
            KotlinLoopNestingDepthVisitor lnd = new KotlinLoopNestingDepthVisitor();
            lnd.computeFor(function); if (lnd.getMetric()!=null) method.addMetric(lnd.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor nopm = new KotlinNumberOfParametersVisitor();
            nopm.computeFor(function); if (nopm.getMetric()!=null) method.addMetric(nopm.getMetric());
        }
        if (isMetricEnabled(project, LAA)) {
            KotlinLocalityOfAttributeAccessesVisitor laa = new KotlinLocalityOfAttributeAccessesVisitor();
            laa.computeFor(function); if (laa.getMetric()!=null) method.addMetric(laa.getMetric());
        }
        if (isMetricEnabled(project, FDP)) {
            KotlinForeignDataProvidersVisitor fdp = new KotlinForeignDataProvidersVisitor();
            fdp.computeFor(function); if (fdp.getMetric()!=null) method.addMetric(fdp.getMetric());
        }
        if (isMetricEnabled(project, NOAV)) {
            KotlinNumberOfAccessedVariablesVisitor noav = new KotlinNumberOfAccessedVariablesVisitor();
            noav.computeFor(function); if (noav.getMetric()!=null) method.addMetric(noav.getMetric());
        }
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtPrimaryConstructor ctor) {
        Project project = ctor.getProject();
        if (isMetricEnabled(project, MetricType.CC)) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(ctor); if (cc.getMetric()!=null) method.addMetric(cc.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor nopm = new KotlinNumberOfParametersVisitor();
            nopm.computeFor(ctor); if (nopm.getMetric()!=null) method.addMetric(nopm.getMetric());
        }
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtSecondaryConstructor ctor) {
        Project project = ctor.getProject();
        if (isMetricEnabled(project, MetricType.CC)) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(ctor); if (cc.getMetric()!=null) method.addMetric(cc.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor nopm = new KotlinNumberOfParametersVisitor();
            nopm.computeFor(ctor); if (nopm.getMetric()!=null) method.addMetric(nopm.getMetric());
        }
    }

    @Override
    protected void addToAllClasses(ClassElement javaClass) {
        // no-op in Class Metrics panel context
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
        return project.getService(SettingsService.class)
                .getClassMetricsTreeSettings()
                .getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    @Deprecated
    @Override
    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {
        return project.getService(SettingsService.class)
                .getClassMetricsTreeSettings()
                .getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);
    }
}
