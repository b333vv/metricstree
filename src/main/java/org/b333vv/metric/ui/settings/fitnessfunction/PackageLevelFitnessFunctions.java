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

package org.b333vv.metric.ui.settings.fitnessfunction;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Long.MAX_VALUE;
import static org.b333vv.metric.model.metric.MetricType.*;

@State(
        name = "PackageLevelFitnessFunctions",
        storages = {@Storage("package-level-fitness-functions.xml")}
)
public final class PackageLevelFitnessFunctions implements PersistentStateComponent<PackageLevelFitnessFunctions> {

    private final Map<String, List<FitnessFunctionItem>> profiles = new TreeMap<>();
    private final Map<String, String> profilesDescription = new HashMap<>();

    public PackageLevelFitnessFunctions() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        //1. Minimize package complexity to make it easier to maintain
        List<FitnessFunctionItem> controlledPackageComplexity = new ArrayList<>();
        addItem(controlledPackageComplexity, PLOC.name(), 2000, MAX_VALUE);
        addItem(controlledPackageComplexity, PAHVL.name(), 200000, MAX_VALUE);
        addItem(controlledPackageComplexity, PAMI.name(), 0, 65);
        profiles.put("Controlled Package Complexity", controlledPackageComplexity);
        profilesDescription.put("Controlled Package Complexity", "Minimize package complexity to make it easier to maintain");

        //2. Ensure minimal dependency on other packets and maximize cohesion within the packet
        List<FitnessFunctionItem> reducedCouplingAndImprovedCohesion = new ArrayList<>();
        addItem(reducedCouplingAndImprovedCohesion, Ce.name(), 20, MAX_VALUE);
        addItem(reducedCouplingAndImprovedCohesion, Ca.name(), 20, MAX_VALUE);
        addItem(reducedCouplingAndImprovedCohesion, I.name(), 0.5, MAX_VALUE);
        profiles.put("Reduced Coupling and Improved Cohesion", reducedCouplingAndImprovedCohesion);
        profilesDescription.put("Reduced Coupling and Improved Cohesion", "Ensure minimal dependency on other packets and maximize cohesion within the packet");

        //3. The package must be abstract enough to be extensible and stable enough to be usable
        List<FitnessFunctionItem>  balanceOfAbstractionAndStability = new ArrayList<>();
        addItem(balanceOfAbstractionAndStability, A.name(), 0.0, 0.3);
        addItem(balanceOfAbstractionAndStability, D.name(), 0.5, MAX_VALUE);
        addItem(balanceOfAbstractionAndStability, I.name(), 0.5, MAX_VALUE);
        profiles.put("Balance of Abstraction and Stability", balanceOfAbstractionAndStability);
        profilesDescription.put("Balance of Abstraction and Stability", "The package must be abstract enough to be extensible and stable enough to be usable");

        //4. The package should be efficiently implemented in terms of algorithm complexity
        List<FitnessFunctionItem>  implementationEfficiency = new ArrayList<>();
        addItem(implementationEfficiency, PAHD.name(), 30, MAX_VALUE);
        addItem(implementationEfficiency, PACHEF.name(), 50000, MAX_VALUE);
        addItem(implementationEfficiency, PLOC.name(), 2000, MAX_VALUE);
        profiles.put("Implementation Efficiency", implementationEfficiency);
        profilesDescription.put("Implementation Efficiency", "The package should be efficiently implemented in terms of algorithm complexity");

        //5. The package should be easily maintainable
        List<FitnessFunctionItem>  packageMaintainability = new ArrayList<>();
        addItem(packageMaintainability, PAMI.name(), 0, 75);
        addItem(packageMaintainability, PACHER.name(), 0.1, MAX_VALUE);
        addItem(packageMaintainability, PNCSS.name(), 1800, MAX_VALUE);
        profiles.put("Package Maintainability", packageMaintainability);
        profilesDescription.put("Package Maintainability", "The package should be easily maintainable");

        //6. Provide a manageable amount of code in the package
        List<FitnessFunctionItem>  packageSizeControl = new ArrayList<>();
        addItem(packageSizeControl, PNOCC.name(), 10, MAX_VALUE);
        addItem(packageSizeControl, PNOAC.name(), 5, MAX_VALUE);
        addItem(packageSizeControl, PNOSC.name(), 3, MAX_VALUE);
        addItem(packageSizeControl, PNOI.name(), 5, MAX_VALUE);
        addItem(packageSizeControl, PLOC.name(), 2000, MAX_VALUE);
        profiles.put("Package Size Control", packageSizeControl);
        profilesDescription.put("Package Size Control", "Provide a manageable amount of code in the package");

        //7. Reduce the impact of changes in the package on the rest of the system
        List<FitnessFunctionItem>  changeResistance = new ArrayList<>();
        addItem(changeResistance, Ce.name(), 15, MAX_VALUE);
        addItem(changeResistance, I.name(), 0.3, MAX_VALUE);
        addItem(changeResistance, D.name(), 0.4, MAX_VALUE);
        profiles.put("Change Resistance", changeResistance);
        profilesDescription.put("Change Resistance", "Reduce the impact of changes in the package on the rest of the system");

        //8. Reduce the complexity of packet interactions
        List<FitnessFunctionItem>  interactionEfficiency = new ArrayList<>();
        addItem(interactionEfficiency, Ce.name(), 10, MAX_VALUE);
        addItem(interactionEfficiency, A.name(), 0.0, 0.5);
        addItem(interactionEfficiency, PACHEF.name(), 1400, MAX_VALUE);
        profiles.put("Interaction Efficiency", interactionEfficiency);
        profilesDescription.put("Interaction Efficiency", "Reduce the complexity of packet interactions");

        //9. Provide simple and clear architecture of interfaces
        List<FitnessFunctionItem>  minimizedInterfaceComplexity = new ArrayList<>();
        addItem(minimizedInterfaceComplexity, PNOI.name(), 3, MAX_VALUE);
        addItem(minimizedInterfaceComplexity, PACHVC.name(), 120, MAX_VALUE);
        addItem(minimizedInterfaceComplexity, PAHD.name(), 25, MAX_VALUE);
        profiles.put("Minimized Interface Complexity", minimizedInterfaceComplexity);
        profilesDescription.put("Minimized Interface Complexity", "Provide simple and clear architecture of interfaces");

        //10. Ensure that the package architecture remains stable and predictable
        List<FitnessFunctionItem>  architecturalStability = new ArrayList<>();
        addItem(architecturalStability, I.name(), 0.3, MAX_VALUE);
        addItem(architecturalStability, D.name(), 0.3, MAX_VALUE);
        addItem(architecturalStability, PAMI.name(), 0, 70);
        profiles.put("Architectural Stability", architecturalStability);
        profilesDescription.put("Architectural Stability", "Ensure that the package architecture remains stable and predictable");

        //11. Make it easier to read and understand the code in the package
        List<FitnessFunctionItem>  transparencyAndSimplicity = new ArrayList<>();
        addItem(transparencyAndSimplicity, PNCSS.name(), 1500, MAX_VALUE);
        addItem(transparencyAndSimplicity, PACHVC.name(), 120, MAX_VALUE);
        addItem(transparencyAndSimplicity, PAMI.name(), 0, 75);
        profiles.put("Transparency and Simplicity", transparencyAndSimplicity);
        profilesDescription.put("Transparency and Simplicity", "Make it easier to read and understand the code in the package");

        //12. Reduce code redundancy and duplication
        List<FitnessFunctionItem>  codeDuplicationMinimization = new ArrayList<>();
        addItem(codeDuplicationMinimization, PACHL.name(), 10000, MAX_VALUE);
        addItem(codeDuplicationMinimization, PLOC.name(), 1800, MAX_VALUE);
        addItem(codeDuplicationMinimization, PAMI.name(), 0, 70);
        profiles.put("Code Duplication Minimization", codeDuplicationMinimization);
        profilesDescription.put("Code Duplication Minimization", "Reduce code redundancy and duplication");

        //13. Ensure that responsibilities are evenly distributed among the classes within the package
        List<FitnessFunctionItem>  overloadResistance = new ArrayList<>();
        addItem(overloadResistance, PNOCC.name(), 12, MAX_VALUE);
        addItem(overloadResistance, PNOAC.name(), 4, MAX_VALUE);
        addItem(overloadResistance, PAHD.name(), 25, MAX_VALUE);
        profiles.put("Overload Resistance", overloadResistance);
        profilesDescription.put("Overload Resistance", "Ensure that responsibilities are evenly distributed among the classes within the package");

        //14. Reduce package maintenance costs
        List<FitnessFunctionItem>  easeOfMaintenance = new ArrayList<>();
        addItem(easeOfMaintenance, PAMI.name(), 0, 70);
        addItem(easeOfMaintenance, PNCSS.name(), 1500, MAX_VALUE);
        addItem(easeOfMaintenance, PACHEF.name(), 40000, MAX_VALUE);
        profiles.put("Ease of Maintenance", easeOfMaintenance);
        profilesDescription.put("Ease of Maintenance", "Reduce package maintenance costs");

        //15. Minimize the number of interfaces while maintaining functionality
        List<FitnessFunctionItem>  interfaceOptimization = new ArrayList<>();
        addItem(interfaceOptimization, PNOI.name(), 3, MAX_VALUE);
        addItem(interfaceOptimization, A.name(), 0.0, 0.6);
        addItem(interfaceOptimization, Ce.name(), 15, MAX_VALUE);
        profiles.put("Interface Optimization", interfaceOptimization);
        profilesDescription.put("Interface Optimization", "Minimize the number of interfaces while maintaining functionality");

        //16. Reduce computational complexity within the package
        List<FitnessFunctionItem>  computationalResourceBalance = new ArrayList<>();
        addItem(computationalResourceBalance, PACHEF.name(), 35000, MAX_VALUE);
        addItem(computationalResourceBalance, PAHVL.name(), 15000, MAX_VALUE);
        addItem(computationalResourceBalance, PAMI.name(), 0, 70);
        profiles.put("Computational Resource Balance", computationalResourceBalance);
        profilesDescription.put("Computational Resource Balance", "Reduce computational complexity within the package");

        //17. Increase cohesion between classes within a package
        List<FitnessFunctionItem>  classCohesionControl = new ArrayList<>();
        addItem(classCohesionControl, Ca.name(), 15, MAX_VALUE);
        addItem(classCohesionControl, D.name(), 0.4, MAX_VALUE);
        addItem(classCohesionControl, PNOCC.name(), 10, MAX_VALUE);
        profiles.put("Class Cohesion Control", classCohesionControl);
        profilesDescription.put("Class Cohesion Control", "Increase cohesion between classes within a package");

        //18. Reduce the structural complexity of the package to increase its simplicity
        List<FitnessFunctionItem>  structuralComplexityOptimization = new ArrayList<>();
        addItem(structuralComplexityOptimization, Ce.name(), 15, MAX_VALUE);
        addItem(structuralComplexityOptimization, PAHVL.name(), 12000, MAX_VALUE);
        addItem(structuralComplexityOptimization, PLOC.name(), 1500, MAX_VALUE);
        profiles.put("Structural Complexity Optimization", structuralComplexityOptimization);
        profilesDescription.put("Structural Complexity Optimization", "Reduce the structural complexity of the package to increase its simplicity");

        //19. Preserve the modularity of the package, ensuring its independence and stability
        List<FitnessFunctionItem>  stabilityAndModularity = new ArrayList<>();
        addItem(stabilityAndModularity, I.name(), 0.4, MAX_VALUE);
        addItem(stabilityAndModularity, Ce.name(), 10, MAX_VALUE);
        addItem(stabilityAndModularity, A.name(), 0.0, 0.4);
        profiles.put("Stability and Modularity", stabilityAndModularity);
        profilesDescription.put("Stability and Modularity", "Preserve the modularity of the package, ensuring its independence and stability");

        //20. Simplify the logical structure of the code in the package
        List<FitnessFunctionItem>  logicComplexityMinimization = new ArrayList<>();
        addItem(logicComplexityMinimization, PAHD.name(), 20, MAX_VALUE);
        addItem(logicComplexityMinimization, PACHEF.name(), 30000, MAX_VALUE);
        addItem(logicComplexityMinimization, D.name(), 0.5, MAX_VALUE);
        profiles.put("Logic Complexity Minimization", logicComplexityMinimization);
        profilesDescription.put("Logic Complexity Minimization", "Simplify the logical structure of the code in the package");

        //21. Increase the testability of the package
        List<FitnessFunctionItem>  testabilitySupport = new ArrayList<>();
        addItem(testabilitySupport, PNOI.name(), 5, MAX_VALUE);
        addItem(testabilitySupport, I.name(), 0.3, MAX_VALUE);
        addItem(testabilitySupport, PAMI.name(), 0, 75);
        profiles.put("Testability Support", testabilitySupport);
        profilesDescription.put("Testability Support", "Increase the testability of the package");

        //21. Ensure that the load on packets is distributed evenly
        List<FitnessFunctionItem>  functionalLoadOptimization = new ArrayList<>();
        addItem(functionalLoadOptimization, PNCSS.name(), 1200, MAX_VALUE);
        addItem(functionalLoadOptimization, PNOCC.name(), 8, MAX_VALUE);
        addItem(functionalLoadOptimization, PACHVC.name(), 100, MAX_VALUE);
        profiles.put("Functional Load Optimization", functionalLoadOptimization);
        profilesDescription.put("Functional Load Optimization", "Ensure that the load on packets is distributed evenly");

        //22. Hidden Technical Debt
        List<FitnessFunctionItem>  contextDependencyMinimization = new ArrayList<>();
        addItem(contextDependencyMinimization, Ce.name(), 10, MAX_VALUE);
        addItem(contextDependencyMinimization, PAHD.name(), 20, MAX_VALUE);
        addItem(contextDependencyMinimization, PAMI.name(), 65, 85);
        profiles.put("Hidden Technical Debt", contextDependencyMinimization);
        profilesDescription.put("Hidden Technical Debt", "Moderate MI masks high complexity and coupling, indicating accumulating debt");

        //23. Reduce the complexity of the functionality provided to the end user
        List<FitnessFunctionItem>  endUserPackageComplexity = new ArrayList<>();
        addItem(endUserPackageComplexity, A.name(), 0.0, 0.6);
        addItem(endUserPackageComplexity, PACHEF.name(), 25000, MAX_VALUE);
        addItem(endUserPackageComplexity, PNCSS.name(), 1000, MAX_VALUE);
        profiles.put("End-User Package Complexity", endUserPackageComplexity);
        profilesDescription.put("End-User Package Complexity", "Reduce the complexity of the functionality provided to the end user");

        //24. A package with many dependents (high Ca) but low abstraction (A) and stability (I) becomes a rigid "core" – changes here risk cascading failures.
        List<FitnessFunctionItem>  rigidCorePackage = new ArrayList<>();
        addItem(rigidCorePackage, A.name(), 0.0, 0.2);
        addItem(rigidCorePackage, Ca.name(), 20, MAX_VALUE);
        addItem(rigidCorePackage, I.name(), 0.0, 0.3);
        profiles.put("Rigid Core Package", rigidCorePackage);
        profilesDescription.put("Rigid Core Package", "A package with many dependents (high Ca) but low abstraction (A) and stability (I) becomes a rigid \"core\" – changes here risk cascading failures");

        //25. A package with many dependents (high Ca) but low abstraction (A) and stability (I) becomes a rigid "core" – changes here risk cascading failures.
        List<FitnessFunctionItem>  concreteOverload = new ArrayList<>();
        addItem(concreteOverload, A.name(), 0.0, 0.2);
        addItem(concreteOverload, Ca.name(), 20, MAX_VALUE);
        addItem(concreteOverload, I.name(), 0.0, 0.3);
        profiles.put("Concrete Overload", concreteOverload);
        profilesDescription.put("Concrete Overload", "Excessive concrete classes with high outgoing dependencies (Ce) create rigidity");

        //26. Static utility classes with high instability are prone to ripple effects.
        List<FitnessFunctionItem>  unstableUtilityPackage = new ArrayList<>();
        addItem(unstableUtilityPackage, PNOSC.name(), 5, MAX_VALUE);
        addItem(unstableUtilityPackage, PAMI.name(), 70, MAX_VALUE);
        addItem(unstableUtilityPackage, I.name(), 0.7, MAX_VALUE);
        profiles.put("Unstable Utility Package", unstableUtilityPackage);
        profilesDescription.put("Unstable Utility Package", "Static utility classes with high instability are prone to ripple effects");

        //27. High coupling without interfaces leads to brittle dependencies.
        List<FitnessFunctionItem>  interfaceDeficiency = new ArrayList<>();
        addItem(interfaceDeficiency, PNOI.name(), 0, 2);
        addItem(interfaceDeficiency, Ce.name(), 15, MAX_VALUE);
        addItem(interfaceDeficiency, D.name(), 0.3, MAX_VALUE);
        profiles.put("Interface Deficiency", interfaceDeficiency);
        profilesDescription.put("Interface Deficiency", "High coupling without interfaces leads to brittle dependencies");

        //28. Oversized packages with low MI are hard to understand and modify.
        List<FitnessFunctionItem>  largeBlobPackage = new ArrayList<>();
        addItem(largeBlobPackage, PLOC.name(), 2000, MAX_VALUE);
        addItem(largeBlobPackage, PNCSS.name(), 1500, MAX_VALUE);
        addItem(largeBlobPackage, PAMI.name(), 0, 60);
        profiles.put("Large Blob Package", largeBlobPackage);
        profilesDescription.put("Large Blob Package", "Oversized packages with low MI are hard to understand and modify");

        //29. Over-abstraction in unstable packages leads to unnecessary indirection.
        List<FitnessFunctionItem>  abstractnessMismatch = new ArrayList<>();
        addItem(abstractnessMismatch, PNOAC.name(), 5, MAX_VALUE);
        addItem(abstractnessMismatch, A.name(), 0.8, MAX_VALUE);
        addItem(abstractnessMismatch, I.name(), 0.7, MAX_VALUE);
        profiles.put("Abstractness Mismatch", abstractnessMismatch);
        profilesDescription.put("Abstractness Mismatch", "Over-abstraction in unstable packages leads to unnecessary indirection");

        //30. High vocabulary/errors suggest error-prone, dense logic.
        List<FitnessFunctionItem>  errorProneLogic = new ArrayList<>();
        addItem(errorProneLogic, PRCHER.name(), 5, MAX_VALUE);
        addItem(errorProneLogic, PRCHVC.name(), 100, MAX_VALUE);
        addItem(errorProneLogic, PLOC.name(), 500, MAX_VALUE);
        profiles.put("Error-Prone Logic", errorProneLogic);
        profilesDescription.put("Error-Prone Logic", "High vocabulary/errors suggest error-prone, dense logic");
    }

    private void addItem(List<FitnessFunctionItem> list, String name, long minValue, long maxValue) {
        list.add(new FitnessFunctionItem(name, true, minValue, maxValue, 0.0, 0.0));
    }

    private void addItem(List<FitnessFunctionItem> list, String name, double minValue, double maxValue) {
        list.add(new FitnessFunctionItem(name, false, 0, 0, minValue, maxValue));
    }

    public Map<String, List<FitnessFunctionItem>> getProfiles() {
        return new TreeMap<>(profiles);
    }

    public Map<String, String> getProfilesDescription() {
        return new HashMap<>(profilesDescription);
    }

    public void setProfiles(Map<String, List<FitnessFunctionItem>> profiles) {
        this.profiles.clear();
        this.profiles.putAll(profiles);
    }

    public void setProfilesDescription(Map<String, String> profilesDescription) {
        this.profilesDescription.clear();
        this.profilesDescription.putAll(profilesDescription);
    }

    @Override
    public synchronized PackageLevelFitnessFunctions getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull PackageLevelFitnessFunctions state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}