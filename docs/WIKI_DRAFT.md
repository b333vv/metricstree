# Welcome to MetricsTree

**MetricsTree** is an IntelliJ IDEA extension designed to evaluate quantitative properties of Java and Kotlin code. It visualizes metrics through trees and treemaps, helping developers identify code quality issues, antipatterns, and areas for improvement.

## 🚀 Key Features

- **Visualization**: View metrics calculation results as trees and treemaps.
- **Scope Control**: Build trees for the currently open class or the entire project.
- **Fitness Functions**: 
  - *Package Level*: Detect coupling and cohesion problems.
  - *Class Level*: Recognize antipatterns like 'God Class' or 'Feature Envy'.
- **Charts & Trends**: Display distributions, correlations, and metric evolution based on `git log`.
- **Editor Integration**: In-code indicators (inlay hints) for metrics exceeding reference intervals.

## 📊 Supported Metrics

MetricsTree calculates metrics at four different levels.

### 1. Project Level
* **General**: Non-Commenting Source Statements, LOC, Number of Classes (Concrete, Abstract, Static), Interfaces.
* **MOOD (Object-Oriented Design)**: Method Hiding Factor (MHF), Attribute Hiding Factor (AHF), Method Inheritance Factor (MIF), Attribute Inheritance Factor (AIF), Polymorphism Factor (PF), Coupling Factor (CF).
* **QMOOD Quality Attributes**: Reusability, Flexibility, Understandability, Functionality, Extendibility, Effectiveness.
* **Halstead Metrics**: Volume, Difficulty, Length, Effort, Vocabulary, Errors.
* **Maintainability Index**

### 2. Package Level
* **General**: NCSS, LOC, Classes/Interfaces counts.
* **Robert C. Martin Metrics**: Efferent/Afferent Coupling (Ce, Ca), Instability (I), Abstractness (A), Normalized Distance (D).
* **Halstead Metrics** & **Maintainability Index**.

### 3. Class Level
* **Chidamber-Kemerer**: WMC, DIT, NOC, CBO, RFC, LCOM.
* **Lorenz-Kidd**: Number of Attributes/Operations, Added/Overridden Methods.
* **Li-Henry**: SIZE2, MPC, DAC, NOM.
* **Lanza-Marinescu**: Access To Foreign Data, Public Attributes, Accessor Methods, Weight Of A Class.
* **Bieman-Kang**: Tight Class Cohesion (TCC).
* **Cognitive Complexity**.

### 4. Method Level
* **Complexity**: McCabe Cyclomatic Complexity (CC), Cognitive Complexity.
* **Nesting**: Maximum, Loop, Condition nesting depths.
* **Coupling**: Coupling Intensity (CINT), Coupling Dispersion (CDISP), Foreign Data Providers (FDP).
* **Locality**: Locality Of Attribute Accesses (LAA).

## 📥 Installation

Install MetricsTree directly from your IDE:
1. Go to **Preferences | Plugins | Marketplace**.
2. Search for `MetricsTree`.
3. Click **Install**.

## ⚙️ Configuration

Access settings via the properties button or **Settings -> Metrics Tree Code Vision**.
- **Reference Intervals**: Define acceptable ranges for metrics.
- **Visuals**: Toggle Inlay Hints in the editor.

## 📚 References
* [1] Brito e Abreu F. and Carapuça R. *Object-Oriented Software Engineering* (1994)
* [2] Bansiya J., Davis C.G. *A hierarchical model for object-oriented design quality assessment* (2002)
* [3] Martin, R. C. *OO design quality metrics* (1994)
* [5] Chidamber S.R., Kemerer C.F. *A Metrics Suite for Object Oriented Design* (1994)
* [10] Halstead, M.H. *Elements of Software Science*
* [12] Campbell, G.A. *Cognitive Complexity* (SonarSource, 2023)

---
*Generated for MetricsTree Wiki*