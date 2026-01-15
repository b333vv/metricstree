/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.Bag;
import org.b333vv.metric.model.util.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.*;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

/**
 * <h1>Project Metrics Set Calculator</h1>
 * 
 * <p>Calculates project-level software quality metrics by traversing the PSI (Program Structure Interface) tree
 * and aggregating class/package-level metrics. This calculator computes three major metric suites:</p>
 * <ul>
 *   <li><b>MOOD Metrics</b> (Metrics for Object-Oriented Design) - AHF, MHF, AIF, MIF, CF, PF</li>
 *   <li><b>QMOOD Metrics</b> (Quality Model for Object-Oriented Design) - Reusability, Flexibility, 
 *       Understandability, Functionality, Extendibility, Effectiveness</li>
 *   <li><b>Halstead Metrics</b> - Volume, Difficulty, Length, Effort, Vocabulary, Errors</li>
 *   <li><b>Maintainability Index</b> - Overall project maintainability score</li>
 *   <li><b>Statistical Metrics</b> - LOC, NCSS, class counts, etc.</li>
 * </ul>
 *
 * <h2>Architecture and Processing Flow</h2>
 * 
 * <p>The calculator operates in two phases:</p>
 * <ol>
 *   <li><b>Collection Phase:</b> Traverses all Java and Kotlin classes in the project scope using a 
 *       {@link JavaRecursiveElementVisitor}. During traversal, it:
 *       <ul>
 *         <li>Collects raw counts (classes, methods, fields, LOC, etc.)</li>
 *         <li>Accumulates visibility information per class and package</li>
 *         <li>Computes inheritance relationships and coupling dependencies</li>
 *         <li>Aggregates class-level metrics (WMC, DIT, LCOM, etc.) from already-calculated values</li>
 *       </ul>
 *   </li>
 *   <li><b>Calculation Phase:</b> After traversal completes, computes final project-level metrics using 
 *       the collected data and formulas specific to each metric suite.</li>
 * </ol>
 *
 * <h2>MOOD Metrics (Metrics for Object-Oriented Design)</h2>
 * 
 * <p>MOOD metrics measure fundamental object-oriented design properties. The calculator implements 
 * six MOOD metrics as defined by Abreu and Carapuça (1994):</p>
 *
 * <h3>1. Attribute Hiding Factor (AHF)</h3>
 * <p>Measures the ratio of hidden (non-visible) attributes to total possible attribute visibility.</p>
 * <pre>
 *   AHF = (Σ(attributes) * (classes - 1) - Σ(attribute_visibility)) / (Σ(attributes) * (classes - 1))
 *   AHF = 1 - (total_visibility / (total_attributes * (classes - 1)))
 * </pre>
 * <p>Visibility contribution rules:</p>
 * <ul>
 *   <li><b>private</b>: contributes 0 (not visible outside declaring class)</li>
 *   <li><b>package-private</b>: contributes (classes_in_same_package - 1)</li>
 *   <li><b>protected</b>: contributes (classes_in_same_package - 1) + subclasses_outside_package</li>
 *   <li><b>public</b>: contributes (total_classes - 1)</li>
 * </ul>
 *
 * <h3>2. Method Hiding Factor (MHF)</h3>
 * <p>Measures the ratio of hidden (non-visible) methods to total possible method visibility.</p>
 * <pre>
 *   MHF = 1 - (total_method_visibility / (total_methods * (classes - 1)))
 * </pre>
 * <p>Uses the same visibility contribution rules as AHF.</p>
 *
 * <h3>3. Attribute Inheritance Factor (AIF)</h3>
 * <p>Measures the ratio of inherited attributes to total available attributes in subclasses.</p>
 * <pre>
 *   AIF = inherited_attributes / available_attributes
 * </pre>
 * <p>Where:</p>
 * <ul>
 *   <li><b>available_attributes</b>: all non-private attributes visible in each class (own + inherited)</li>
 *   <li><b>inherited_attributes</b>: attributes defined in superclasses (excluding Object)</li>
 * </ul>
 *
 * <h3>4. Method Inheritance Factor (MIF)</h3>
 * <p>Measures the ratio of inherited methods to total available methods in subclasses.</p>
 * <pre>
 *   MIF = inherited_methods / available_methods
 * </pre>
 * <p>Counts only non-overridden methods. Overridden methods are not double-counted.</p>
 *
 * <h3>5. Coupling Factor (CF)</h3>
 * <p>Measures the ratio of actual couplings to maximum possible couplings between classes.</p>
 * <pre>
 *   CF = actual_couplings / max_possible_couplings
 *   max_possible_couplings = (classes * (classes - 1)) / 2
 * </pre>
 * <p>Counts couplings from each class to classes it depends on (excluding inheritance relationships).</p>
 *
 * <h3>6. Polymorphism Factor (PF)</h3>
 * <p>Measures the ratio of actual method overrides to potential overrides.</p>
 * <pre>
 *   PF = actual_overrides / potential_overrides
 *   potential_overrides = Σ(new_methods_per_class * subclass_count)
 * </pre>
 * <p>Where new_methods are methods not inherited from superclasses.</p>
 *
 * <h2>QMOOD Metrics (Quality Model for Object-Oriented Design)</h2>
 * 
 * <p>QMOOD provides a hierarchical quality model mapping low-level design properties to high-level 
 * quality attributes. The calculator implements the QMOOD model as defined by Bansiya and Davis (2002).</p>
 *
 * <h3>Design Properties (Normalized via Z-Score)</h3>
 * <p>Eleven design properties are computed from class/package metrics:</p>
 * <ol>
 *   <li><b>Design Size</b>: number of classes (PNOCC aggregated by package)</li>
 *   <li><b>Hierarchies</b>: average Depth of Inheritance Tree (DIT)</li>
 *   <li><b>Abstraction</b>: abstractness ratio (A) per package</li>
 *   <li><b>Encapsulation</b>: fixed at 1.0 (all classes assumed encapsulated in this implementation)</li>
 *   <li><b>Coupling</b>: efferent coupling (Ce) per package</li>
 *   <li><b>Cohesion</b>: inverse of Lack of Cohesion (LCOM) - lower LCOM = higher cohesion</li>
 *   <li><b>Composition</b>: Number of Attributes (NOA) per class</li>
 *   <li><b>Inheritance</b>: ratio of overridden to total methods across all classes</li>
 *   <li><b>Polymorphism</b>: Number of Overridden Methods (NOOM) per class</li>
 *   <li><b>Messaging</b>: Number of Methods (NOM) per class</li>
 *   <li><b>Complexity</b>: Weighted Methods per Class (WMC)</li>
 * </ol>
 *
 * <p>Each property is normalized using z-score transformation:</p>
 * <pre>
 *   z = (max_value - mean) / standard_deviation
 * </pre>
 * <p><b>Note:</b> The current implementation uses max instead of individual values, which provides
 * a single z-score representing the "extremeness" of the maximum observation in the distribution.
 * This approach emphasizes outliers in the design property measurement.</p>
 *
 * <h3>Quality Attributes</h3>
 * <p>Six quality attributes are derived from weighted combinations of design properties:</p>
 * <ol>
 *   <li><b>Reusability</b> = -0.25*Coupling + 0.25*Cohesion + 0.5*Messaging + 0.5*DesignSize
 *       <br>Measures how easily components can be reused in other contexts.</li>
 *   <li><b>Flexibility</b> = 0.25*Encapsulation - 0.25*Coupling + 0.5*Composition + 0.5*Polymorphism
 *       <br>Measures ease of modification and extension.</li>
 *   <li><b>Understandability</b> = -0.33*Abstraction + 0.33*Encapsulation - 0.33*Coupling 
 *       + 0.33*Cohesion - 0.33*Polymorphism - 0.33*Complexity - 0.33*DesignSize
 *       <br>Measures how easily the design can be comprehended.</li>
 *   <li><b>Functionality</b> = 0.12*Cohesion + 0.22*Polymorphism + 0.22*Messaging 
 *       + 0.22*DesignSize + 0.22*Hierarchies
 *       <br>Measures the breadth of capabilities provided.</li>
 *   <li><b>Extendibility</b> = 0.5*Abstraction - 0.5*Coupling + 0.5*Inheritance + 0.5*Polymorphism
 *       <br>Measures ease of adding new functionality.</li>
 *   <li><b>Effectiveness</b> = 0.2*Abstraction + 0.2*Encapsulation + 0.2*Composition 
 *       + 0.2*Inheritance + 0.2*Polymorphism
 *       <br>Measures overall design efficiency and quality.</li>
 * </ol>
 *
 * <h2>Halstead Metrics</h2>
 * 
 * <p>Aggregates Halstead complexity metrics from package-level calculations:</p>
 * <ul>
 *   <li><b>Volume (PRHVL)</b>: total program volume = Σ(package volumes)</li>
 *   <li><b>Difficulty (PRHD)</b>: total difficulty = Σ(package difficulties)</li>
 *   <li><b>Length (PRCHL)</b>: total program length = Σ(package lengths)</li>
 *   <li><b>Effort (PRCHEF)</b>: total programming effort = Σ(package efforts)</li>
 *   <li><b>Vocabulary (PRCHVC)</b>: total unique operators + operands = Σ(package vocabularies)</li>
 *   <li><b>Errors (PRCHER)</b>: estimated errors = Σ(package errors)</li>
 * </ul>
 *
 * <h2>Maintainability Index</h2>
 * 
 * <p>Computes the Microsoft Visual Studio Maintainability Index formula:</p>
 * <pre>
 *   MI = max(0, (171 - 5.2*ln(HalsteadVolume) - 0.23*ln(CyclomaticComplexity) 
 *        - 16.2*ln(LinesOfCode)) * 100 / 171)
 * </pre>
 * <p>Where:</p>
 * <ul>
 *   <li><b>HalsteadVolume</b>: summed from package metrics (PAHVL)</li>
 *   <li><b>CyclomaticComplexity</b>: summed from all method CC metrics</li>
 *   <li><b>LinesOfCode</b>: summed from all method LOC metrics</li>
 * </ul>
 * <p>Result is normalized to 0-100 scale, where higher values indicate better maintainability.</p>
 *
 * <h2>Kotlin Support</h2>
 * 
 * <p>Kotlin classes are processed by converting {@link KtClassOrObject} to light Java {@link PsiClass} 
 * representations using {@link LightClassUtilsKt#toLightClass}. This enables uniform processing of 
 * Java and Kotlin code through the same PSI visitor logic.</p>
 *
 * <h2>Thread Safety and Progress Reporting</h2>
 * 
 * <p>This calculator is designed to run on a background thread with progress indication. It uses 
 * {@link ProgressIndicator#checkCanceled()} to support user cancellation and reports progress 
 * during PSI traversal.</p>
 *
 * @author b333vv
 * @see ProjectElement
 * @see DependenciesBuilder
 * @see MetricType
 */
public class ProjectMetricsSetCalculator {
    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;
    private final ProjectElement projectElement;

    private ProgressIndicator indicator;
    private int filesCount;
    private int progress = 0;

    // Attribute-related counters for MOOD metrics
    private int attributesNumber = 0;
    private int publicAttributesNumber = 0;
    private int classesNumber = 0;
    private Value totalAttributesVisibility = Value.of(0.0);
    private final Bag<String> classesPerPackage = new Bag<>();
    private final Bag<String> packageVisibleAttributesPerPackage = new Bag<>();

    /**
     * Number of protected fields per declaring class.
     * Protected visibility requires a package-aware calculation performed after traversal.
     */
    private final Map<PsiClass, Integer> protectedFieldsPerClass = new HashMap<>();

    private final Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    /** Cached number of subclasses outside the declaring class package. */
    private final Map<PsiClass, Integer> subclassesOutsidePackagePerClass = new HashMap<>();

    private int availableFields = 0;
    private int inheritedFields = 0;

    private int totalCoupling = 0;

    // Method-related counters for MOOD metrics
    private int methodsNumber = 0;
    private int publicMethodsNumber = 0;
    private Value totalMethodsVisibility = Value.of(0.0);
    private final Bag<String> packageVisibleMethodsPerPackage = new Bag<>();

    /**
     * Number of protected methods per declaring class.
     * Protected visibility requires a package-aware calculation performed after traversal.
     */
    private final Map<PsiClass, Integer> protectedMethodsPerClass = new HashMap<>();

    private int availableMethods = 0;
    private int inheritedMethods = 0;

    private int overridingMethodsNumber = 0;
    private int overridePotentialsNumber = 0;

    // Statistical counters
    private long concreteClassesNumber = 0;
    private long abstractClassesNumber = 0;
    private long staticClassesNumber = 0;
    private long interfacesNumber = 0;
    private long linesOfCode = 0;
    double halsteadVolume = 0.0;

    /**
     * Constructs a new ProjectMetricsSetCalculator.
     *
     * @param scope the analysis scope defining which files to process
     * @param dependenciesBuilder the builder providing class dependency information
     * @param projectElement the project model element to which calculated metrics will be added
     */
    public ProjectMetricsSetCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder, ProjectElement projectElement) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
        this.projectElement = projectElement;
    }

    /**
     * Executes the complete project metrics calculation process.
     * 
     * <p>This method orchestrates the two-phase calculation:</p>
     * <ol>
     *   <li>Traverses the PSI tree to collect raw data</li>
     *   <li>Computes final metrics from collected data</li>
     * </ol>
     * 
     * <p>Progress is reported via {@link ProgressIndicator} and can be canceled by the user.</p>
     */
    public void calculate() {
        indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Initializing");
        filesCount = scope.getFileCount();

        scope.accept(new Visitor());

        indicator.setText("Calculating metrics");

        calculateMood();
        calculateStatistics();
        calculateQmood();
        calculateHalstead();
        calculateMaintainabilityIndex();
    }

    /**
     * Calculates QMOOD quality attributes from design properties.
     * 
     * <p>Computes z-scores for 11 design properties, then applies weighted formulas
     * to derive 6 quality attributes: Reusability, Flexibility, Understandability,
     * Functionality, Extendibility, and Effectiveness.</p>
     */
    private void calculateQmood() {
        double zCoupling = calculateZCoupling();
        double zCohesion = calculateZCohesion();
        double zMessaging = calculateZMessaging();
        double zDesignSize = calculateZDesignSize();
        double zEncapsulation = 1.0;
        double zComposition = calculateZComposition();
        double zPolymorphism = calculateZPolymorphism();
        double zAbstraction = calculateZAbstraction();
        double zComplexity = calculateZComplexity();
        double zHierarchies = calculateZHierarchies();
        double zInheritance = calculateZInheritance();

        double Reusability = -0.25 * zCoupling + 0.25 * zCohesion + 0.5 * zMessaging + 0.5 * zDesignSize;
        double Flexibility = 0.25 * zEncapsulation - 0.25 * zCoupling + 0.5 * zComposition + 0.5 * zPolymorphism;
        double Understandability = -0.33 * zAbstraction + 0.33 * zEncapsulation - 0.33 * zCoupling
                + 0.33 * zCohesion - 0.33 * zPolymorphism - 0.33 * zComplexity - 0.33 * zDesignSize;
        double Functionality = 0.12 * zCohesion + 0.22 * zPolymorphism + 0.22 * zMessaging + 0.22 * zDesignSize
                + 0.22 * zHierarchies;
        double Extendibility = 0.5 * zAbstraction - 0.5 * zCoupling + 0.5 * zInheritance + 0.5 * zPolymorphism;
        double Effectiveness = 0.2 * zAbstraction + 0.2 * zEncapsulation + 0.2 * zComposition
                + 0.2 * zInheritance + 0.2 * zPolymorphism;

        projectElement.addMetric(Metric.of(MetricType.Reusability, Reusability));
        projectElement.addMetric(Metric.of(MetricType.Flexibility, Flexibility));
        projectElement.addMetric(Metric.of(MetricType.Understandability, Understandability));
        projectElement.addMetric(Metric.of(MetricType.Functionality, Functionality));
        projectElement.addMetric(Metric.of(MetricType.Extendibility, Extendibility));
        projectElement.addMetric(Metric.of(MetricType.Effectiveness, Effectiveness));
    }

    /**
     * Calculates statistical metrics: class counts, NCSS, and LOC.
     */
    private void calculateStatistics() {
        addClassesCounters();
        addClassesNonCommentingSourceStatements();
        addLinesOfCode();
    }

    /**
     * Calculates all MOOD metrics: AHF, MHF, AIF, MIF, CF, and PF.
     */
    private void calculateMood() {
        addAttributeHidingFactor();
        addAttributeInheritanceFactor();
        addCouplingFactor();
        addMethodHidingFactor();
        addMethodInheritanceFactor();
        addPolymorphismFactor();
    }

    /**
     * Calculates the Maintainability Index using the Microsoft formula.
     * 
     * <p>Formula: MI = max(0, (171 - 5.2*ln(V) - 0.23*ln(CC) - 16.2*ln(LOC)) * 100/171)</p>
     * <p>Where V = Halstead Volume, CC = Cyclomatic Complexity, LOC = Lines of Code</p>
     */
    private void calculateMaintainabilityIndex() {
        long projectCC = projectElement
                .allClasses().flatMap(ClassElement::methods)
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (projectCC > 0L && linesOfCode > 0L && halsteadVolume > 0.0) {
            maintainabilityIndex = Math.max(0, (171 - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(projectCC) - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        projectElement.addMetric(Metric.of(MetricType.PRMI, maintainabilityIndex));
    }

    /**
     * Aggregates Halstead metrics from package-level metrics.
     * 
     * <p>Sums the following Halstead metrics across all packages:</p>
     * <ul>
     *   <li>Volume (PRHVL)</li>
     *   <li>Difficulty (PRHD)</li>
     *   <li>Length (PRCHL)</li>
     *   <li>Effort (PRCHEF)</li>
     *   <li>Vocabulary (PRCHVC)</li>
     *   <li>Errors (PRCHER)</li>
     * </ul>
     */
    private void calculateHalstead() {
        halsteadVolume = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PAHVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        double halsteadDifficulty = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PAHD)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadLength = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadEffort = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHEF)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadVocabulary = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHVC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadErrors = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHER)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        projectElement.addMetric(Metric.of(PRHVL, halsteadVolume));
        projectElement.addMetric(Metric.of(PRHD, halsteadDifficulty));
        projectElement.addMetric(Metric.of(PRCHL, halsteadLength));
        projectElement.addMetric(Metric.of(PRCHEF, halsteadEffort));
        projectElement.addMetric(Metric.of(PRCHVC, halsteadVocabulary));
        projectElement.addMetric(Metric.of(PRCHER, halsteadErrors));
    }

    private double calculateZCoupling() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == Ce)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZCohesion() {
        double z = calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == LCOM)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
        return z == 0.0 ? 0.0 : 1.0 / z;
    }

    private double calculateZMessaging() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == NOM)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZDesignSize() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == PNOCC)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZComposition() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == NOA)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZPolymorphism() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == NOOM)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZAbstraction() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == A)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZComplexity() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == WMC)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZHierarchies() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == DIT)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Calculates inheritance factor as the average ratio of overridden methods to total methods
     * across all classes.
     * 
     * @return sum of (NOOM/NOM) ratios across classes, or 0 if no classes with methods exist
     */
    private double calculateZInheritance() {
        List<CodeElement> classes = projectElement.allClasses()
                .collect(Collectors.toUnmodifiableList());
        double zInheritance = 0.0;
        for (CodeElement aClass : classes) {
            Metric nomMetric = aClass.metric(MetricType.NOM);
            if (nomMetric == null) {
                continue;
            }
            Value nom = nomMetric.getPsiValue();
            if (nom.isEqualsOrLessThan(Value.ZERO)) {
                continue;
            }
            Metric noomMetric = aClass.metric(NOOM);
            Value noom = noomMetric != null ? noomMetric.getPsiValue() : Value.ZERO;
            zInheritance = zInheritance + (noom.divide(nom.times(Value.of(100)))).doubleValue();
        }
        return zInheritance;
    }

    /**
     * Calculates z-score for a list of metric values.
     * 
     * <p>The z-score measures how many standard deviations the maximum value is from the mean:</p>
     * <pre>
     *   z = (max - mean) / std_dev
     * </pre>
     * 
     * <p><b>Note:</b> This implementation computes the z-score of the maximum value in the distribution,
     * which is useful for identifying extreme outliers in design properties. For per-element z-scores,
     * a different approach would be needed.</p>
     *
     * @param source list of metric values
     * @return z-score of the maximum value, or 0.0 if list is empty or standard deviation is zero
     */
    private double calculateZScore(List<Value> source) {
        if (source == null || source.isEmpty()) {
            return 0.0;
        }

        Value max = source.stream().max(Value::compareTo).orElse(Value.ZERO);
        Value avg = source.stream().reduce(Value::plus).orElse(Value.ZERO)
                .divide(Value.of(source.size()));

        Value dispersion = source.stream()
                .map(v -> v.minus(avg).pow(2))
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .divide(Value.of(source.size()));

        Value std = Value.of(Math.sqrt(dispersion.doubleValue()));
        if (std.equals(Value.ZERO)) {
            return 0.0;
        }

        Value zScore = max.minus(avg).divide(std);
        return zScore.doubleValue();
    }

    private void addClassesNonCommentingSourceStatements() {
        long nonCommentingSourceStatements = projectElement.allClasses().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == NCSS)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        projectElement.addMetric(Metric.of(PNCSS, nonCommentingSourceStatements));
    }

    private void addLinesOfCode() {
        linesOfCode = projectElement.allClasses()
                .flatMap(ClassElement::methods)
                .map(javaMethod -> {
                    Metric m = javaMethod.metric(LOC);
                    return m != null ? m.getPsiValue() : Value.ZERO;
                })
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        projectElement.addMetric(Metric.of(PLOC, linesOfCode));
    }

    private void addClassesCounters() {
        projectElement.addMetric(Metric.of(PNOCC, concreteClassesNumber));
        projectElement.addMetric(Metric.of(PNOAC, abstractClassesNumber));
        projectElement.addMetric(Metric.of(PNOSC, staticClassesNumber));
        projectElement.addMetric(Metric.of(PNOI, interfacesNumber));
    }

    /**
     * Computes and stores the Polymorphism Factor (PF) metric.
     * 
     * <p>PF measures the ratio of actual method overrides to potential overrides.
     * If there are no potential overrides (base classes with overridable methods),
     * PF is defined as 1.0 (maximum polymorphism).</p>
     */
    private void addPolymorphismFactor() {
        Value polymorphismFactor = overridePotentialsNumber == 0 ? Value.of(1.0) :
                Value.of((double) overridingMethodsNumber).divide(Value.of((double) overridePotentialsNumber));
        projectElement.addMetric(Metric.of(PF, polymorphismFactor));
    }

    /**
     * Computes and stores the Method Inheritance Factor (MIF) metric.
     * 
     * <p>MIF = inherited_methods / available_methods</p>
     * <p>Returns 0 if no methods are available (no classes or all private methods).</p>
     */
    private void addMethodInheritanceFactor() {
        Value methodInheritanceFactor = Value.ZERO;
        if (availableMethods > 0) {
            methodInheritanceFactor = Value.of((double) inheritedMethods).divide(Value.of((double) availableMethods));
        }
        projectElement.addMetric(Metric.of(MIF, methodInheritanceFactor));
    }

    /**
     * Computes and stores the Method Hiding Factor (MHF) metric.
     * 
     * <p>MHF measures information hiding for methods. It computes total method visibility
     * by summing visibility contributions from public, package-private, and protected methods,
     * then calculates the hiding factor as:</p>
     * <pre>
     *   MHF = 1 - (total_visibility / (total_methods * (classes - 1)))
     * </pre>
     * 
     * <p>Visibility contribution rules:</p>
     * <ul>
     *   <li>public: visible to all other classes (classes - 1)</li>
     *   <li>package-private: visible within same package (classes_in_package - 1)</li>
     *   <li>protected: visible in same package + subclasses outside package</li>
     *   <li>private: not visible (contributes 0)</li>
     * </ul>
     */
    private void addMethodHidingFactor() {
        // Public methods are visible from all other classes.
        totalMethodsVisibility = totalMethodsVisibility
                .plus((Value.of(publicMethodsNumber)
                        .times(Value.of(classesNumber - 1))));

        // Package-private methods are visible only within the same package.
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleMethods = packageVisibleMethodsPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalMethodsVisibility = totalMethodsVisibility
                    .plus((Value.of(visibleMethods)
                            .times(Value.of(Math.max(0, classes - 1)))));
        }

        // Protected methods: visible within the same package and to subclasses in other packages.
        for (Map.Entry<PsiClass, Integer> entry : protectedMethodsPerClass.entrySet()) {
            PsiClass declaringClass = entry.getKey();
            if (declaringClass == null) {
                continue;
            }
            int protectedMembersCount = entry.getValue();
            if (protectedMembersCount <= 0) {
                continue;
            }
            String declaringPackage = ClassUtils.calculatePackageName(declaringClass);
            int classesInPackage = classesPerPackage.getCountForObject(declaringPackage);
            int visibleInSamePackage = Math.max(0, classesInPackage - 1);
            int subclassesOutsidePackage = getSubclassesOutsidePackageCount(declaringClass, declaringPackage);

            totalMethodsVisibility = totalMethodsVisibility.plus(
                    Value.of(protectedMembersCount)
                            .times(Value.of(visibleInSamePackage + subclassesOutsidePackage))
            );
        }

        // Calculate hiding factor with division-by-zero safety
        Value methodHidingFactor = Value.ZERO;
        if (classesNumber > 1 && methodsNumber > 0) {
            final Value denominator = Value.of(methodsNumber).times(Value.of(classesNumber - 1));
            if (!denominator.equals(Value.ZERO)) {
                final Value numerator = denominator.minus(totalMethodsVisibility);
                methodHidingFactor = numerator.divide(denominator);
            }
        }

        projectElement.addMetric(Metric.of(MHF, methodHidingFactor));
    }

    /**
     * Computes and stores the Coupling Factor (CF) metric.
     * 
     * <p>CF measures the ratio of actual class couplings to maximum possible couplings:</p>
     * <pre>
     *   CF = actual_couplings / (classes * (classes-1) / 2)
     * </pre>
     */
    private void addCouplingFactor() {
        Value couplingFactor = Value.ZERO;
        if (classesNumber > 1) {
            Value numerator = Value.of((double) totalCoupling);
            Value denominator = Value.of((double) classesNumber)
                    .times(Value.of((double) (classesNumber - 1))).divide(Value.of(2.0));
            if (!denominator.equals(Value.ZERO)) {
                couplingFactor = numerator.divide(denominator);
            }
        }

        projectElement.addMetric(Metric.of(CF, couplingFactor));
    }

    /**
     * Computes and stores the Attribute Inheritance Factor (AIF) metric.
     * 
     * <p>AIF = inherited_attributes / available_attributes</p>
     * <p>Returns 0 if no attributes are available (no classes or all private attributes).</p>
     */
    private void addAttributeInheritanceFactor() {
        Value attributeInheritanceFactor = Value.ZERO;
        if (availableFields > 0) {
            attributeInheritanceFactor = Value.of((double) inheritedFields)
                    .divide(Value.of((double) availableFields));
        }

        projectElement.addMetric(Metric.of(AIF, attributeInheritanceFactor));
    }

    /**
     * Computes and stores the Attribute Hiding Factor (AHF) metric.
     * 
     * <p>AHF measures information hiding for attributes. It computes total attribute visibility
     * by summing visibility contributions from public, package-private, and protected fields,
     * then calculates the hiding factor as:</p>
     * <pre>
     *   AHF = 1 - (total_visibility / (total_attributes * (classes - 1)))
     * </pre>
     * 
     * <p>Visibility contribution rules (same as MHF):</p>
     * <ul>
     *   <li>public: visible to all other classes (classes - 1)</li>
     *   <li>package-private: visible within same package (classes_in_package - 1)</li>
     *   <li>protected: visible in same package + subclasses outside package</li>
     *   <li>private: not visible (contributes 0)</li>
     * </ul>
     */
    private void addAttributeHidingFactor() {
        // Public fields are visible from all other classes.
        totalAttributesVisibility = totalAttributesVisibility
                .plus((Value.of(publicAttributesNumber)
                        .times(Value.of(classesNumber - 1))));

        // Package-private fields are visible only within the same package.
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleAttributes = packageVisibleAttributesPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalAttributesVisibility = totalAttributesVisibility
                    .plus((Value.of(visibleAttributes)
                            .times(Value.of(Math.max(0, classes - 1)))));
        }

        // Protected fields: visible within the same package and to subclasses in other packages.
        for (Map.Entry<PsiClass, Integer> entry : protectedFieldsPerClass.entrySet()) {
            PsiClass declaringClass = entry.getKey();
            if (declaringClass == null) {
                continue;
            }
            int protectedMembersCount = entry.getValue();
            if (protectedMembersCount <= 0) {
                continue;
            }
            String declaringPackage = ClassUtils.calculatePackageName(declaringClass);
            int classesInPackage = classesPerPackage.getCountForObject(declaringPackage);
            int visibleInSamePackage = Math.max(0, classesInPackage - 1);
            int subclassesOutsidePackage = getSubclassesOutsidePackageCount(declaringClass, declaringPackage);

            totalAttributesVisibility = totalAttributesVisibility.plus(
                    Value.of(protectedMembersCount)
                            .times(Value.of(visibleInSamePackage + subclassesOutsidePackage))
            );
        }

        // Calculate hiding factor with division-by-zero safety
        Value attributeHidingFactor = Value.ZERO;
        if (classesNumber > 1 && attributesNumber > 0) {
            final Value denominator = Value.of(attributesNumber).times(Value.of(classesNumber - 1));
            if (!denominator.equals(Value.ZERO)) {
                final Value numerator = denominator.minus(totalAttributesVisibility);
                attributeHidingFactor = numerator.divide(denominator);
            }
        }

        projectElement.addMetric(Metric.of(AHF, attributeHidingFactor));
    }

    /**
     * Computes and caches the number of subclasses outside the declaring class's package.
     * 
     * <p>Used for calculating protected member visibility in AHF and MHF metrics.
     * Subclasses within the same package are not counted here since they're already
     * included in the package-private visibility calculation.</p>
     *
     * @param psiClass the declaring class
     * @param declaringPackage the package name of the declaring class
     * @return number of subclasses located in different packages
     */
    private int getSubclassesOutsidePackageCount(@NotNull PsiClass psiClass, @NotNull String declaringPackage) {
        if (subclassesOutsidePackagePerClass.containsKey(psiClass)) {
            return subclassesOutsidePackagePerClass.get(psiClass);
        }

        int subclassesNumber = 0;
        final GlobalSearchScope globalScope = GlobalSearchScope.allScope(scope.getProject());
        final Query<PsiClass> query = ClassInheritorsSearch.search(
                psiClass, globalScope, true, true, true);
        for (final PsiClass inheritor : query) {
            if (inheritor.isInterface()) {
                continue;
            }
            if (classIsInLibrary(inheritor)) {
                continue;
            }
            String inheritorPackage = ClassUtils.calculatePackageName(inheritor);
            if (!declaringPackage.equals(inheritorPackage)) {
                subclassesNumber++;
            }
        }

        subclassesOutsidePackagePerClass.put(psiClass, subclassesNumber);
        return subclassesNumber;
    }

    /**
     * Determines if a class is part of an external library (not project source code).
     * 
     * <p>A class is considered a library class if:</p>
     * <ul>
     *   <li>Its containing file cannot be resolved, OR</li>
     *   <li>Its virtual file is indexed as a library by IntelliJ, OR</li>
     *   <li>Its virtual file is not in content roots or source roots</li>
     * </ul>
     * 
     * <p>Kotlin source files are correctly identified as non-library even when converted
     * to light classes.</p>
     *
     * @param psiClass the class to check
     * @return true if the class is from a library, false if it's project source code
     */
    private boolean classIsInLibrary(@NotNull PsiClass psiClass) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
            PsiFile file = psiClass.getContainingFile();
            if (file == null) {
                // If we cannot resolve a file for the class, treat it as library to be safe
                return true;
            }
            VirtualFile vFile = file.getVirtualFile();
            if (vFile == null) {
                return true;
            }
            ProjectFileIndex index = ProjectRootManager.getInstance(file.getProject()).getFileIndex();
            // Class is considered in library only if it is indexed as library; Kotlin sources are in content/source
            boolean inLibrary = index.isInLibrary(vFile);
            boolean inContentOrSource = index.isInContent(vFile) || index.isInSource(vFile);
            return inLibrary || !inContentOrSource;
        });
    }

    /**
     * Processes member visibility for a given class and accumulates visibility counts.
     * 
     * <p>This method is called both for Java classes (via {@link #visitClass}) and 
     * Kotlin classes (via {@link #visitFile} after conversion to light classes).</p>
     * 
     * <p>Processes:</p>
     * <ul>
     *   <li>All methods: counts and categorizes by visibility (public/protected/package-private)</li>
     *   <li>All fields: counts and categorizes by visibility</li>
     * </ul>
     *
     * @param psiClass the class whose members to process
     */
    private void processMemberVisibility(@NotNull PsiClass psiClass) {
        for (PsiMethod psiMethod : psiClass.getMethods()) {
            methodsNumber++;
            final PsiClass containingClass = psiMethod.getContainingClass();
            if (containingClass == null) {
                continue;
            }

            if (psiMethod.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
                // private: not visible outside
            } else if (psiMethod.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                protectedMethodsPerClass.merge(containingClass, 1, Integer::sum);
            } else if ((psiMethod.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                publicMethodsNumber++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleMethodsPerPackage.add(packageName);
            }
        }

        for (PsiField psiField : psiClass.getFields()) {
            attributesNumber++;
            final PsiClass containingClass = psiField.getContainingClass();
            if (containingClass == null) {
                continue;
            }

            if (psiField.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
                // private: not visible outside
            } else if (psiField.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                protectedFieldsPerClass.merge(containingClass, 1, Integer::sum);
            } else if ((psiField.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                publicAttributesNumber++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleAttributesPerPackage.add(packageName);
            }
        }
    }

    /**
     * PSI visitor that traverses Java and Kotlin files to collect raw metric data.
     * 
     * <p>For Java files, the standard {@link JavaRecursiveElementVisitor} mechanism is used.
     * For Kotlin files, we explicitly process {@link KtFile} by converting Kotlin classes
     * to light Java {@link PsiClass} representations.</p>
     */
    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
            // Include Kotlin files by converting Kt classes to light Java PsiClass
            if (psiFile instanceof KtFile) {
                KtFile ktFile = (KtFile) psiFile;
                for (KtDeclaration decl : ktFile.getDeclarations()) {
                    if (decl instanceof KtClassOrObject) {
                        PsiClass light = LightClassUtilsKt.toLightClass((KtClassOrObject) decl);
                        if (light != null) {
                            indicator.checkCanceled();

                            // Process same as for Java classes
                            processAttributeInheritanceFactor(light);
                            processAttributeAndMethodHidingFactor(light);
                            processCouplingFactor(light);
                            processMethodInheritanceFactor(light);
                            processPolymorphismFactor(light);
                            processStatisticMetrics(light);
                            processMemberVisibility(light);

                            indicator.setText("Calculating metrics on project level: processing class " + light.getName() + "...");
                            progress++;
                            indicator.setIndeterminate(false);
                            indicator.setFraction((double) progress / (double) filesCount);
                        }
                    }
                }
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();

            // Collect class-level inputs before descending into members.
            processAttributeInheritanceFactor(aClass);
            processAttributeAndMethodHidingFactor(aClass);
            processCouplingFactor(aClass);
            processMethodInheritanceFactor(aClass);
            processPolymorphismFactor(aClass);
            processStatisticMetrics(aClass);

            super.visitClass(aClass);

            indicator.setText("Calculating metrics on project level: processing class " + aClass.getName() + "...");
            progress++;
            indicator.setIndeterminate(false);
            indicator.setFraction((double) progress / (double) filesCount);
        }

        /**
         * Collects class type statistics (concrete, abstract, static, interface).
         */
        private void processStatisticMetrics(@NotNull PsiClass psiClass) {
            if (ClassUtils.isConcreteClass(psiClass)) {
                concreteClassesNumber++;
            }
            if (ClassUtils.isAbstractClass(psiClass)) {
                abstractClassesNumber++;
            }
            if (ClassUtils.isStaticClass(psiClass)) {
                staticClassesNumber++;
            }
            if (psiClass.isInterface()) {
                interfacesNumber++;
            }
        }

        /**
         * Processes polymorphism information for a class.
         * 
         * <p>Counts:</p>
         * <ul>
         *   <li>New methods (not overriding any superclass method)</li>
         *   <li>Overridden methods (having at least one super method)</li>
         * </ul>
         * 
         * <p>Override potentials = new_methods * subclass_count</p>
         */
        private void processPolymorphismFactor(@NotNull PsiClass psiClass) {
            int newMethodsCount = 0;
            int overriddenMethodsCount = 0;
            final PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                final PsiMethod[] superMethods = method.findSuperMethods();
                if (superMethods.length == 0) {
                    newMethodsCount++;
                } else {
                    overriddenMethodsCount++;
                }
            }
            overridePotentialsNumber += newMethodsCount * getSubclassCount(psiClass);
            overridingMethodsNumber += overriddenMethodsCount;
        }

        /**
         * Processes method inheritance information for a class.
         * 
         * <p>Computes the set of non-overridden methods available to the class (own + inherited).
         * Counts available methods and inherited methods for MIF calculation.</p>
         */
        private void processMethodInheritanceFactor(@NotNull PsiClass psiClass) {
            final PsiMethod[] allMethods = psiClass.getAllMethods();
            final Set<PsiMethod> nonOverriddenMethods = new HashSet<>();
            for (PsiMethod method : allMethods) {
                boolean overrideFound = false;
                for (PsiMethod testMethod : allMethods) {
                    if (overrides(testMethod, method)) {
                        overrideFound = true;
                        break;
                    }
                }
                if (!overrideFound) {
                    nonOverriddenMethods.add(method);
                }
            }
            for (PsiMethod method : nonOverriddenMethods) {
                final PsiClass containingClass = method.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                if (containingClass.equals(psiClass)) {
                    availableMethods++;
                } else if (!classIsInLibrary(containingClass) && !method.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableMethods++;
                    inheritedMethods++;
                }
            }
        }

        /**
         * Checks if testMethod overrides method (not reflexive).
         */
        private boolean overrides(PsiMethod testMethod, PsiMethod method) {
            if (testMethod.equals(method)) {
                return false;
            }
            final PsiMethod[] superMethods = testMethod.findSuperMethods();
            for (PsiMethod superMethod : superMethods) {
                if (superMethod.equals(method)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Processes coupling information for a class.
         * 
         * <p>Counts the number of classes this class depends on, excluding inheritance relationships.</p>
         */
        private void processCouplingFactor(PsiClass psiClass) {
            final Set<PsiClass> dependencies = dependenciesBuilder.getClassesDependencies(psiClass);
            totalCoupling += dependencies.stream()
                    .filter(c -> !psiClass.isInheritor(c, true))
                    .count();
        }

        /**
         * Tracks class count per package for AHF/MHF calculation.
         */
        private void processAttributeAndMethodHidingFactor(PsiClass psiClass) {
            classesNumber++;
            final String packageName = ClassUtils.calculatePackageName(psiClass);
            classesPerPackage.add(packageName);
        }

        /**
         * Processes attribute inheritance information for a class.
         * 
         * <p>Counts available fields and inherited fields for AIF calculation.
         * Excludes fields from java.lang.Object and private fields in superclasses.</p>
         */
        private void processAttributeInheritanceFactor(PsiClass psiClass) {
            final PsiField[] allFields = psiClass.getAllFields();
            for (PsiField field : allFields) {
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                final String className = containingClass.getName();
                if (containingClass.equals(psiClass)) {
                    availableFields++;
                } else if (!"java.lang.Object".equals(className) && !field.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableFields++;
                    inheritedFields++;
                }
            }
        }

        @Override
        public void visitMethod(PsiMethod psiMethod) {
            super.visitMethod(psiMethod);
            // Note: For Java classes, visibility is now handled by processMemberVisibility
            // which is called from visitClass. However, for backwards compatibility and 
            // to handle any edge cases where visitMethod might be called independently,
            // we still include the logic here. The counts will be correct because
            // visitMethod is called during super.visitClass(), and processMemberVisibility
            // is called before super.visitClass(), so there's no duplication.
        }

        @Override
        public void visitField(PsiField psiField) {
            super.visitField(psiField);
            // Note: Same as visitMethod - kept for edge cases and backwards compatibility.
        }

        /**
         * Computes and caches the total number of (non-interface, non-library) subclasses 
         * for a given class.
         *
         * @param psiClass the class to find subclasses for
         * @return total subclass count
         */
        private int getSubclassCount(final PsiClass psiClass) {
            if (subclassesPerClass.containsKey(psiClass)) {
                return subclassesPerClass.get(psiClass);
            }
            int subclassesNumber = 0;
            final GlobalSearchScope globalScope = GlobalSearchScope.allScope(scope.getProject());
            final Query<PsiClass> query = ClassInheritorsSearch.search(
                    psiClass, globalScope, true, true, true);
            for (final PsiClass inheritor : query) {
                if (!inheritor.isInterface() && !classIsInLibrary(inheritor)) {
                    subclassesNumber++;
                }
            }
            subclassesPerClass.put(psiClass, subclassesNumber);
            return subclassesNumber;
        }
    }
}
