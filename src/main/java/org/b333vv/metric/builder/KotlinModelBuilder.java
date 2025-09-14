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

    /**
     * Reflective bridge for {@link #createKotlinFile(KtFile)} to avoid compile-time dependency
     * from callers that only have a PsiFile instance. Public for reflective invocation.
     */
    public org.b333vv.metric.model.code.FileElement createKotlinFileBridge(com.intellij.psi.PsiFile psiFile) {
        if (psiFile instanceof KtFile) {
            return createKotlinFile((KtFile) psiFile);
        }
        return null;
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
                    if (isMetricEnabled(project, ATFD)) {
                        KotlinAccessToForeignDataVisitor atfd = new KotlinAccessToForeignDataVisitor();
                        atfd.computeFor((KtClass) ktClass);
                        if (atfd.getMetric() != null) klass.addMetric(atfd.getMetric());
                    }
                    if (isMetricEnabled(project, DAC)) {
                        KotlinDataAbstractionCouplingVisitor dac = new KotlinDataAbstractionCouplingVisitor();
                        dac.computeFor((KtClass) ktClass);
                        if (dac.getMetric() != null) klass.addMetric(dac.getMetric());
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
                    if (isMetricEnabled(project, MPC)) {
                        KotlinMessagePassingCouplingVisitor mpc = new KotlinMessagePassingCouplingVisitor();
                        mpc.computeFor((KtClass) ktClass);
                        if (mpc.getMetric() != null) klass.addMetric(mpc.getMetric());
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
                    if (isMetricEnabled(project, NOAC)) {
                        KotlinNumberOfAccessorMethodsVisitor noac = new KotlinNumberOfAccessorMethodsVisitor();
                        noac.computeFor((KtClass) ktClass);
                        if (noac.getMetric() != null) klass.addMetric(noac.getMetric());
                    }
                    if (isMetricEnabled(project, NOAM)) {
                        KotlinNumberOfAddedMethodsVisitor noam = new KotlinNumberOfAddedMethodsVisitor();
                        noam.computeFor((KtClass) ktClass);
                        if (noam.getMetric() != null) klass.addMetric(noam.getMetric());
                    }
                    if (isMetricEnabled(project, NOO)) {
                        KotlinNumberOfOperationsVisitor noo = new KotlinNumberOfOperationsVisitor();
                        noo.computeFor((KtClass) ktClass);
                        if (noo.getMetric() != null) klass.addMetric(noo.getMetric());
                    }
                    if (isMetricEnabled(project, NOOM)) {
                        KotlinNumberOfOverriddenMethodsVisitor noom = new KotlinNumberOfOverriddenMethodsVisitor();
                        noom.computeFor((KtClass) ktClass);
                        if (noom.getMetric() != null) klass.addMetric(noom.getMetric());
                    }
                    if (isMetricEnabled(project, NOPA)) {
                        KotlinNumberOfPublicAttributesVisitor nopa = new KotlinNumberOfPublicAttributesVisitor();
                        nopa.computeFor((KtClass) ktClass);
                        if (nopa.getMetric() != null) klass.addMetric(nopa.getMetric());
                    }
                    if (isMetricEnabled(project, SIZE2)) {
                        KotlinNumberOfAttributesAndMethodsVisitor size2 = new KotlinNumberOfAttributesAndMethodsVisitor();
                        size2.computeFor((KtClass) ktClass);
                        if (size2.getMetric() != null) klass.addMetric(size2.getMetric());
                    }
                    if (isMetricEnabled(project, WOC)) {
                        KotlinWeightOfAClassVisitor woc = new KotlinWeightOfAClassVisitor();
                        woc.computeFor((KtClass) ktClass);
                        if (woc.getMetric() != null) klass.addMetric(woc.getMetric());
                    }

                    // Always compute Kotlin Halstead class metrics (mirrors Java pipeline behavior)
                    KotlinHalsteadClassVisitor kh = new KotlinHalsteadClassVisitor();
                    kh.computeFor((KtClass) ktClass);
                    for (org.b333vv.metric.model.metric.Metric m : kh.buildMetrics()) {
                        klass.addMetric(m);
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
        if (isMetricEnabled(project, CCM)) {
            KotlinCognitiveComplexityVisitor ccm = new KotlinCognitiveComplexityVisitor();
            ccm.computeFor(function); if (ccm.getMetric()!=null) method.addMetric(ccm.getMetric());
        }
        if (isMetricEnabled(project, MND)) {
            KotlinMaximumNestingDepthVisitor mnd = new KotlinMaximumNestingDepthVisitor();
            mnd.computeFor(function); if (mnd.getMetric()!=null) method.addMetric(mnd.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor nopm = new KotlinNumberOfParametersVisitor();
            nopm.computeFor(function); if (nopm.getMetric()!=null) method.addMetric(nopm.getMetric());
        }
        if (isMetricEnabled(project, NOL)) {
            KotlinNumberOfLoopsVisitor nol = new KotlinNumberOfLoopsVisitor();
            nol.computeFor(function); if (nol.getMetric()!=null) method.addMetric(nol.getMetric());
        }
        if (isMetricEnabled(project, LAA)) {
            KotlinLocalityOfAttributeAccessesVisitor laa = new KotlinLocalityOfAttributeAccessesVisitor();
            laa.computeFor(function); if (laa.getMetric()!=null) method.addMetric(laa.getMetric());
        }
        if (isMetricEnabled(project, FDP)) {
            KotlinForeignDataProvidersVisitor fdp = new KotlinForeignDataProvidersVisitor();
            fdp.computeFor(function); if (fdp.getMetric()!=null) method.addMetric(fdp.getMetric());
        }
        if (isMetricEnabled(project, CINT)) {
            KotlinCouplingIntensityVisitor cint = new KotlinCouplingIntensityVisitor();
            cint.computeFor(function); if (cint.getMetric()!=null) method.addMetric(cint.getMetric());
        }
        if (isMetricEnabled(project, CDISP)) {
            KotlinCouplingDispersionVisitor cdisp = new KotlinCouplingDispersionVisitor();
            cdisp.computeFor(function); if (cdisp.getMetric()!=null) method.addMetric(cdisp.getMetric());
        }
        if (isMetricEnabled(project, NOAV)) {
            KotlinNumberOfAccessedVariablesVisitor noav = new KotlinNumberOfAccessedVariablesVisitor();
            noav.computeFor(function); if (noav.getMetric()!=null) method.addMetric(noav.getMetric());
        }
        // Halstead metrics for methods: always compute full suite similar to Java pipeline
        KotlinHalsteadMethodVisitor hal = new KotlinHalsteadMethodVisitor();
        hal.computeFor(function);
        for (org.b333vv.metric.model.metric.Metric m : hal.buildMetrics()) { method.addMetric(m); }
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtPrimaryConstructor ctor) {
        Project project = ctor.getProject();
        if (isMetricEnabled(project, MetricType.CC)) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(ctor); if (cc.getMetric()!=null) method.addMetric(cc.getMetric());
        }
        if (isMetricEnabled(project, CCM)) {
            KotlinCognitiveComplexityVisitor ccm = new KotlinCognitiveComplexityVisitor();
            ccm.computeFor(ctor); if (ccm.getMetric()!=null) method.addMetric(ccm.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor nopm = new KotlinNumberOfParametersVisitor();
            nopm.computeFor(ctor); if (nopm.getMetric()!=null) method.addMetric(nopm.getMetric());
        }
        if (isMetricEnabled(project, NOL)) {
            KotlinNumberOfLoopsVisitor nol = new KotlinNumberOfLoopsVisitor();
            nol.computeFor(ctor); if (nol.getMetric()!=null) method.addMetric(nol.getMetric());
        }
        if (isMetricEnabled(project, MND)) {
            KotlinMaximumNestingDepthVisitor mnd = new KotlinMaximumNestingDepthVisitor();
            mnd.computeFor(ctor); if (mnd.getMetric()!=null) method.addMetric(mnd.getMetric());
        }
        // Halstead metrics for constructors (likely zero or minimal)
        KotlinHalsteadMethodVisitor hal = new KotlinHalsteadMethodVisitor();
        hal.computeFor(ctor);
        for (org.b333vv.metric.model.metric.Metric m : hal.buildMetrics()) { method.addMetric(m); }
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
        // Halstead metrics for constructors
        KotlinHalsteadMethodVisitor hal = new KotlinHalsteadMethodVisitor();
        hal.computeFor(ctor);
        for (org.b333vv.metric.model.metric.Metric m : hal.buildMetrics()) { method.addMetric(m); }
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
