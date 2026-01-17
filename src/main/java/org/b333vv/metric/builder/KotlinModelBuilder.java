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
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
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
 * Kotlin-specific model builder extracted to avoid hard dependency from core
 * classes at startup.
 */
public class KotlinModelBuilder extends ModelBuilder {

    private final Project project;

    public KotlinModelBuilder(Project project) {
        this.project = project;
    }

    /**
     * Reflective bridge for {@link #createKotlinFile(KtFile)} to avoid compile-time
     * dependency
     * from callers that only have a PsiFile instance. Public for reflective
     * invocation.
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
                if (ktClass instanceof KtClassOrObject) {
                    if (isMetricEnabled(project, WMC)) {
                        KotlinWeightedMethodCountVisitor wmc = new KotlinWeightedMethodCountVisitor();
                        wmc.computeFor(ktClass);
                        if (wmc.getMetric() != null)
                            klass.addMetric(wmc.getMetric());
                    }
                    if (isMetricEnabled(project, ATFD)) {
                        KotlinAccessToForeignDataVisitor atfd = new KotlinAccessToForeignDataVisitor();
                        atfd.computeFor(ktClass);
                        if (atfd.getMetric() != null)
                            klass.addMetric(atfd.getMetric());
                    }
                    if (isMetricEnabled(project, DAC)) {
                        KotlinDataAbstractionCouplingVisitor dac = new KotlinDataAbstractionCouplingVisitor();
                        dac.computeFor(ktClass);
                        if (dac.getMetric() != null)
                            klass.addMetric(dac.getMetric());
                    }
                    if (isMetricEnabled(project, NOM)) {
                        KotlinNumberOfMethodsVisitor nom = new KotlinNumberOfMethodsVisitor();
                        nom.computeFor(ktClass);
                        if (nom.getMetric() != null)
                            klass.addMetric(nom.getMetric());
                    }
                    if (isMetricEnabled(project, NOA)) {
                        KotlinNumberOfAttributesVisitor noa = new KotlinNumberOfAttributesVisitor();
                        noa.computeFor(ktClass);
                        if (noa.getMetric() != null)
                            klass.addMetric(noa.getMetric());
                    }
                    if (isMetricEnabled(project, NCSS)) {
                        KotlinNonCommentingSourceStatementsVisitor ncss = new KotlinNonCommentingSourceStatementsVisitor();
                        ncss.computeFor(ktClass);
                        if (ncss.getMetric() != null)
                            klass.addMetric(ncss.getMetric());
                    }
                    if (isMetricEnabled(project, RFC)) {
                        KotlinResponseForClassVisitor rfc = new KotlinResponseForClassVisitor();
                        rfc.computeFor(ktClass);
                        if (rfc.getMetric() != null)
                            klass.addMetric(rfc.getMetric());
                    }
                    if (isMetricEnabled(project, CBO)) {
                        KotlinCouplingBetweenObjectsVisitor cbo = new KotlinCouplingBetweenObjectsVisitor();
                        cbo.computeFor(ktClass);
                        if (cbo.getMetric() != null)
                            klass.addMetric(cbo.getMetric());
                    }
                    if (isMetricEnabled(project, MPC)) {
                        KotlinMessagePassingCouplingVisitor mpc = new KotlinMessagePassingCouplingVisitor();
                        mpc.computeFor(ktClass);
                        if (mpc.getMetric() != null)
                            klass.addMetric(mpc.getMetric());
                    }
                    if (isMetricEnabled(project, LCOM)) {
                        KotlinLackOfCohesionOfMethodsVisitor lcom = new KotlinLackOfCohesionOfMethodsVisitor();
                        lcom.computeFor(ktClass);
                        if (lcom.getMetric() != null)
                            klass.addMetric(lcom.getMetric());
                    }
                    if (isMetricEnabled(project, DIT)) {
                        KotlinDepthOfInheritanceTreeVisitor dit = new KotlinDepthOfInheritanceTreeVisitor();
                        dit.computeFor(ktClass);
                        if (dit.getMetric() != null)
                            klass.addMetric(dit.getMetric());
                    }
                    if (isMetricEnabled(project, NOC)) {
                        KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
                        noc.computeFor(ktClass);
                        if (noc.getMetric() != null)
                            klass.addMetric(noc.getMetric());
                    }
                    if (isMetricEnabled(project, TCC)) {
                        KotlinTightClassCohesionVisitor tcc = new KotlinTightClassCohesionVisitor();
                        tcc.computeFor(ktClass);
                        if (tcc.getMetric() != null)
                            klass.addMetric(tcc.getMetric());
                    }
                    if (isMetricEnabled(project, NOAC)) {
                        KotlinNumberOfAccessorMethodsVisitor noac = new KotlinNumberOfAccessorMethodsVisitor();
                        noac.computeFor(ktClass);
                        if (noac.getMetric() != null)
                            klass.addMetric(noac.getMetric());
                    }
                    if (isMetricEnabled(project, NOAM)) {
                        KotlinNumberOfAddedMethodsVisitor noam = new KotlinNumberOfAddedMethodsVisitor();
                        noam.computeFor(ktClass);
                        if (noam.getMetric() != null)
                            klass.addMetric(noam.getMetric());
                    }
                    if (isMetricEnabled(project, NOO)) {
                        KotlinNumberOfOperationsVisitor noo = new KotlinNumberOfOperationsVisitor();
                        noo.computeFor(ktClass);
                        if (noo.getMetric() != null)
                            klass.addMetric(noo.getMetric());
                    }
                    if (isMetricEnabled(project, NOOM)) {
                        KotlinNumberOfOverriddenMethodsVisitor noom = new KotlinNumberOfOverriddenMethodsVisitor();
                        noom.computeFor(ktClass);
                        if (noom.getMetric() != null)
                            klass.addMetric(noom.getMetric());
                    }
                    if (isMetricEnabled(project, NOPA)) {
                        KotlinNumberOfPublicAttributesVisitor nopa = new KotlinNumberOfPublicAttributesVisitor();
                        nopa.computeFor(ktClass);
                        if (nopa.getMetric() != null)
                            klass.addMetric(nopa.getMetric());
                    }
                    if (isMetricEnabled(project, SIZE2)) {
                        KotlinNumberOfAttributesAndMethodsVisitor size2 = new KotlinNumberOfAttributesAndMethodsVisitor();
                        size2.computeFor(ktClass);
                        if (size2.getMetric() != null)
                            klass.addMetric(size2.getMetric());
                    }
                    if (isMetricEnabled(project, WOC)) {
                        KotlinWeightOfAClassVisitor woc = new KotlinWeightOfAClassVisitor();
                        woc.computeFor(ktClass);
                        if (woc.getMetric() != null)
                            klass.addMetric(woc.getMetric());
                    }

                    // Always compute Kotlin Halstead class metrics (mirrors Java pipeline behavior)
                    KotlinHalsteadClassVisitor kh = new KotlinHalsteadClassVisitor();
                    kh.computeFor(ktClass);
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
        // If no classes/objects declared, create a synthetic class to host top-level
        // functions
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
                                .printInfo(
                                        "[KotlinModelBuilder] Skipped method visitors for top-level function due to: "
                                                + t.getClass().getSimpleName());
                    }
                    addMaintainabilityIndexForMethod(m);
                }
            }
            addMaintainabilityIndexForClass(synthetic);
            addLinesOfCodeIndexForClass(synthetic);
            addCognitiveComplexityForClass(synthetic);

            // Apply class-level metrics to the synthetic class (file level)
            if (isMetricEnabled(project, WMC)) {
                KotlinWeightedMethodCountVisitor wmc = new KotlinWeightedMethodCountVisitor();
                wmc.computeFor(ktFile);
                if (wmc.getMetric() != null)
                    synthetic.addMetric(wmc.getMetric());
            }
            if (isMetricEnabled(project, NOM)) {
                KotlinNumberOfMethodsVisitor nom = new KotlinNumberOfMethodsVisitor();
                nom.computeFor(ktFile);
                if (nom.getMetric() != null)
                    synthetic.addMetric(nom.getMetric());
            }
            if (isMetricEnabled(project, NOA)) {
                KotlinNumberOfAttributesVisitor noa = new KotlinNumberOfAttributesVisitor();
                noa.computeFor(ktFile);
                if (noa.getMetric() != null)
                    synthetic.addMetric(noa.getMetric());
            }
            if (isMetricEnabled(project, NCSS)) {
                KotlinNonCommentingSourceStatementsVisitor ncss = new KotlinNonCommentingSourceStatementsVisitor();
                ncss.computeFor(ktFile);
                if (ncss.getMetric() != null)
                    synthetic.addMetric(ncss.getMetric());
            }
            if (isMetricEnabled(project, SIZE2)) {
                KotlinNumberOfAttributesAndMethodsVisitor size2 = new KotlinNumberOfAttributesAndMethodsVisitor();
                size2.computeFor(ktFile);
                if (size2.getMetric() != null)
                    synthetic.addMetric(size2.getMetric());
            }
            if (isMetricEnabled(project, ATFD)) {
                KotlinAccessToForeignDataVisitor atfd = new KotlinAccessToForeignDataVisitor();
                atfd.computeFor(ktFile);
                if (atfd.getMetric() != null)
                    synthetic.addMetric(atfd.getMetric());
            }
            if (isMetricEnabled(project, CBO)) {
                KotlinCouplingBetweenObjectsVisitor cbo = new KotlinCouplingBetweenObjectsVisitor();
                cbo.computeFor(ktFile);
                if (cbo.getMetric() != null)
                    synthetic.addMetric(cbo.getMetric());
            }
            if (isMetricEnabled(project, DAC)) {
                KotlinDataAbstractionCouplingVisitor dac = new KotlinDataAbstractionCouplingVisitor();
                dac.computeFor(ktFile);
                if (dac.getMetric() != null)
                    synthetic.addMetric(dac.getMetric());
            }
            if (isMetricEnabled(project, MPC)) {
                KotlinMessagePassingCouplingVisitor mpc = new KotlinMessagePassingCouplingVisitor();
                mpc.computeFor(ktFile);
                if (mpc.getMetric() != null)
                    synthetic.addMetric(mpc.getMetric());
            }
            if (isMetricEnabled(project, RFC)) {
                KotlinResponseForClassVisitor rfc = new KotlinResponseForClassVisitor();
                rfc.computeFor(ktFile);
                if (rfc.getMetric() != null)
                    synthetic.addMetric(rfc.getMetric());
            }
            if (isMetricEnabled(project, TCC)) {
                KotlinTightClassCohesionVisitor tcc = new KotlinTightClassCohesionVisitor();
                tcc.computeFor(ktFile);
                if (tcc.getMetric() != null)
                    synthetic.addMetric(tcc.getMetric());
            }
            if (isMetricEnabled(project, WOC)) {
                KotlinWeightOfAClassVisitor woc = new KotlinWeightOfAClassVisitor();
                woc.computeFor(ktFile);
                if (woc.getMetric() != null)
                    synthetic.addMetric(woc.getMetric());
            }
            if (isMetricEnabled(project, DIT)) {
                KotlinDepthOfInheritanceTreeVisitor dit = new KotlinDepthOfInheritanceTreeVisitor();
                dit.computeFor(ktFile);
                if (dit.getMetric() != null)
                    synthetic.addMetric(dit.getMetric());
            }
            if (isMetricEnabled(project, NOC)) {
                KotlinNumberOfChildrenVisitor noc = new KotlinNumberOfChildrenVisitor();
                noc.computeFor(ktFile);
                if (noc.getMetric() != null)
                    synthetic.addMetric(noc.getMetric());
            }
            if (isMetricEnabled(project, NOAC)) {
                KotlinNumberOfAccessorMethodsVisitor noac = new KotlinNumberOfAccessorMethodsVisitor();
                noac.computeFor(ktFile);
                if (noac.getMetric() != null)
                    synthetic.addMetric(noac.getMetric());
            }
            if (isMetricEnabled(project, NOAM)) {
                KotlinNumberOfAddedMethodsVisitor noam = new KotlinNumberOfAddedMethodsVisitor();
                noam.computeFor(ktFile);
                if (noam.getMetric() != null)
                    synthetic.addMetric(noam.getMetric());
            }
            if (isMetricEnabled(project, NOO)) {
                KotlinNumberOfOperationsVisitor noo = new KotlinNumberOfOperationsVisitor();
                noo.computeFor(ktFile);
                if (noo.getMetric() != null)
                    synthetic.addMetric(noo.getMetric());
            }
            if (isMetricEnabled(project, NOOM)) {
                KotlinNumberOfOverriddenMethodsVisitor noom = new KotlinNumberOfOverriddenMethodsVisitor();
                noom.computeFor(ktFile);
                if (noom.getMetric() != null)
                    synthetic.addMetric(noom.getMetric());
            }
            if (isMetricEnabled(project, LCOM)) {
                KotlinLackOfCohesionOfMethodsVisitor lcom = new KotlinLackOfCohesionOfMethodsVisitor();
                lcom.computeFor(ktFile);
                if (lcom.getMetric() != null)
                    synthetic.addMetric(lcom.getMetric());
            }
            if (isMetricEnabled(project, NOPA)) {
                KotlinNumberOfPublicAttributesVisitor nopa = new KotlinNumberOfPublicAttributesVisitor();
                nopa.computeFor(ktFile);
                if (nopa.getMetric() != null)
                    synthetic.addMetric(nopa.getMetric());
            }

            // Always compute Kotlin Halstead metrics for synthetic class
            KotlinHalsteadClassVisitor kh = new KotlinHalsteadClassVisitor();
            kh.computeFor(ktFile);
            for (org.b333vv.metric.model.metric.Metric m : kh.buildMetrics()) {
                synthetic.addMetric(m);
            }
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
                            .printInfo("[KotlinModelBuilder] Skipped visitors for primary ctor due to: "
                                    + t.getClass().getSimpleName());
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
                            .printInfo("[KotlinModelBuilder] Skipped visitors for secondary ctor due to: "
                                    + t.getClass().getSimpleName());
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
                            .printInfo("[KotlinModelBuilder] Skipped visitors for function due to: "
                                    + t.getClass().getSimpleName());
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
        applyAllKotlinMethodVisitors(method, function);
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtPrimaryConstructor ctor) {
        applyAllKotlinMethodVisitors(method, ctor);

        // Aggregate metrics from anonymous initializers in the same class
        KtClassOrObject parentClass = ctor.getContainingClassOrObject();
        if (parentClass != null) {
            for (KtAnonymousInitializer initializer : parentClass.getAnonymousInitializers()) {
                applyMetricsFromInitializer(method, initializer);
            }
        }
    }

    protected void applyKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtSecondaryConstructor ctor) {
        applyAllKotlinMethodVisitors(method, ctor);
    }

    private void applyAllKotlinMethodVisitors(@NotNull MethodElement method, @NotNull KtElement element) {
        Project project = element.getProject();
        if (isMetricEnabled(project, LOC)) {
            KotlinLinesOfCodeVisitor visitor = new KotlinLinesOfCodeVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, MetricType.CC)) {
            KotlinMcCabeCyclomaticComplexityVisitor visitor = new KotlinMcCabeCyclomaticComplexityVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, CND)) {
            KotlinConditionNestingDepthVisitor visitor = new KotlinConditionNestingDepthVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, LND)) {
            KotlinLoopNestingDepthVisitor visitor = new KotlinLoopNestingDepthVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, CCM)) {
            KotlinCognitiveComplexityVisitor visitor = new KotlinCognitiveComplexityVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, MND)) {
            KotlinMaximumNestingDepthVisitor visitor = new KotlinMaximumNestingDepthVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, NOPM)) {
            KotlinNumberOfParametersVisitor visitor = new KotlinNumberOfParametersVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, NOL)) {
            KotlinNumberOfLoopsVisitor visitor = new KotlinNumberOfLoopsVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, LAA)) {
            KotlinLocalityOfAttributeAccessesVisitor visitor = new KotlinLocalityOfAttributeAccessesVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, FDP)) {
            KotlinForeignDataProvidersVisitor visitor = new KotlinForeignDataProvidersVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, CINT)) {
            KotlinCouplingIntensityVisitor visitor = new KotlinCouplingIntensityVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, CDISP)) {
            KotlinCouplingDispersionVisitor visitor = new KotlinCouplingDispersionVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }
        if (isMetricEnabled(project, NOAV)) {
            KotlinNumberOfAccessedVariablesVisitor visitor = new KotlinNumberOfAccessedVariablesVisitor();
            computeFor(visitor, element);
            if (visitor.getMetric() != null)
                method.addMetric(visitor.getMetric());
        }

        KotlinHalsteadMethodVisitor hal = new KotlinHalsteadMethodVisitor();
        computeFor(hal, element);
        for (org.b333vv.metric.model.metric.Metric m : hal.buildMetrics()) {
            method.addMetric(m);
        }
    }

    private void applyMetricsFromInitializer(@NotNull MethodElement method,
            @NotNull KtAnonymousInitializer initializer) {
        KotlinLinesOfCodeVisitor loc = new KotlinLinesOfCodeVisitor();
        loc.computeFor(initializer);
        if (loc.getMetric() != null) {
            Metric existing = method.metric(LOC);
            Value newValue = existing != null ? existing.getPsiValue().plus(loc.getMetric().getPsiValue())
                    : loc.getMetric().getPsiValue();
            method.addMetric(Metric.of(LOC, newValue));
        }

        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.computeFor(initializer);
        if (cc.getMetric() != null) {
            Metric existing = method.metric(MetricType.CC);
            Value newValue = existing != null
                    ? existing.getPsiValue().plus(cc.getMetric().getPsiValue().minus(Value.ONE))
                    : cc.getMetric().getPsiValue().minus(Value.ONE);
            method.addMetric(Metric.of(MetricType.CC, newValue));
        }

        KotlinCognitiveComplexityVisitor ccm = new KotlinCognitiveComplexityVisitor();
        ccm.computeFor(initializer);
        if (ccm.getMetric() != null) {
            Metric existing = method.metric(CCM);
            Value newValue = existing != null ? existing.getPsiValue().plus(ccm.getMetric().getPsiValue())
                    : ccm.getMetric().getPsiValue();
            method.addMetric(Metric.of(CCM, newValue));
        }

        KotlinNumberOfLoopsVisitor nol = new KotlinNumberOfLoopsVisitor();
        nol.computeFor(initializer);
        if (nol.getMetric() != null) {
            Metric existing = method.metric(NOL);
            Value newValue = existing != null ? existing.getPsiValue().plus(nol.getMetric().getPsiValue())
                    : nol.getMetric().getPsiValue();
            method.addMetric(Metric.of(NOL, newValue));
        }

        KotlinConditionNestingDepthVisitor cnd = new KotlinConditionNestingDepthVisitor();
        cnd.computeFor(initializer);
        if (cnd.getMetric() != null) {
            Metric existing = method.metric(CND);
            int max = Math.max(existing != null ? existing.getPsiValue().intValue() : 0,
                    cnd.getMetric().getPsiValue().intValue());
            method.addMetric(Metric.of(CND, max));
        }

        KotlinLoopNestingDepthVisitor lnd = new KotlinLoopNestingDepthVisitor();
        lnd.computeFor(initializer);
        if (lnd.getMetric() != null) {
            Metric existing = method.metric(LND);
            int max = Math.max(existing != null ? existing.getPsiValue().intValue() : 0,
                    lnd.getMetric().getPsiValue().intValue());
            method.addMetric(Metric.of(LND, max));
        }

        KotlinMaximumNestingDepthVisitor mnd = new KotlinMaximumNestingDepthVisitor();
        mnd.computeFor(initializer);
        if (mnd.getMetric() != null) {
            Metric existing = method.metric(MND);
            int max = Math.max(existing != null ? existing.getPsiValue().intValue() : 0,
                    mnd.getMetric().getPsiValue().intValue());
            method.addMetric(Metric.of(MND, max));
        }
    }

    private void computeFor(KotlinMethodVisitor visitor, KtElement element) {
        if (element instanceof KtNamedFunction)
            visitor.computeFor((KtNamedFunction) element);
        else if (element instanceof KtPrimaryConstructor)
            visitor.computeFor((KtPrimaryConstructor) element);
        else if (element instanceof KtSecondaryConstructor)
            visitor.computeFor((KtSecondaryConstructor) element);
        else if (element instanceof KtAnonymousInitializer)
            visitor.computeFor((KtAnonymousInitializer) element);
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
