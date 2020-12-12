# MetricsTree IntelliJ IDEA plugin [![Build Status](https://travis-ci.org/b333vv/metricstree.svg?branch=master)](https://travis-ci.org/b333vv)

MetricsTree is an IDE extension that helps you evaluate quantitative properties of java code. 
MetricsTree calculates metrics on project, package, class and method levels. 
It supports the most common metric sets.
# Metrics
1. Project level:
    - Non-Commenting Source Statements
    - Lines Of Code
    - Number Of Concrete Classes
    - Number Of Abstract Classes
    - Number Of Static Classes
    - Number Of Interfaces
    - MOOD metrics set [1]:  
        - MHF: Method Hiding Factor
        - AHF: Attribute Hiding Factor
        - MIF: Method Inheritance Factor
        - AIF: Attribute Inheritance Factor
        - PF: Polymorphism Factor
        - CF: Coupling Factor
2. Package level
    - Non-Commenting Source Statements
    - Lines Of Code
    - Number Of Concrete Classes
    - Number Of Abstract Classes
    - Number Of Static Classes
    - Number Of Interfaces
    - Robert C. Martin metrics set [2, 3]:
        - Ce: Efferent Coupling
        - Ca: Afferent Coupling
        - I: Instability
        - A: Abstractness
        - D: Normalized Distance from Main Sequence
3. Class level
    - Chidamber-Kemerer metrics set [4]:
        - WMC: Weighted methods per class
        - DIT: Depth of Inheritance Tree
        - NOC: Number of Children
        - CBO: Coupling between object classes
        - RFC: Response for a Class
        - LCOM: Lack of cohesion in methods
    - Lorenz-Kidd metrics set [5]:
        - NOA: Number of Attributes
        - NOO: Number of Operations
        - NOAM: Number of Added Methods
        - NOOM: Number of Overridden Methods
    - Li-Henry metrics set [6]:
        - SIZE2: Number of Attributes and Methods
        - MPC: Message Passing Coupling
        - DAC: Data Abstraction Coupling
        - NOM: Number of Methods
    - Lanza-Marinescu metrics set [7]:
        - ATFD: Access To Foreign Data
        - NOPA: Number Of Public Attributes
        - Number Of Accessor Methods
        - WOC: Weight Of A Class
    - Bieman-Kang metrics set [8]:
        - TCC: Tight Class Cohesion
    - Chr. Clemens Lee metrics set:
            - NCSS: Non-Commenting Source Statements
4. Method level:
    - LOC: Lines Of Code
    - CC: McCabe Cyclomatic Complexity
    - Maximum Nesting Depth
    - Loop Nesting Depth
    - Condition Nesting Depth
    - Number Of Loops
    - LAA: Locality Of Attribute Accesses
    - FDP: Foreign Data Providers
    - NOAV: NumberOfAccessedVariables
    - CINT: Coupling Intensity
    - CDISP: Coupling Dispersion
> 1. Brito e Abreu F. and Carapuça R. Object-Oriented Software Engineering: Measuring and controlling the development process, 4th Interntional Conference on Software Quality, Mc Lean, VA, USA, 1994.
> 2. Martin, R. C. OO design quality metrics. An analysis of dependencies. 28 October 1994.
> 3. Martin, R. C. Agile Software Development: Principles, Patterns, and Practices. Alant Apt Series. Prentice Hall, Upper Saddle River, NJ, USA 2002.
> 4. S. R. Chidamber and C. F. Kemerer. A Metrics Suite for Object Oriented Design. In IEEE Transactions on Software Engineering, volume 20 (6), pages 476-493, June 1994.
> 5. M. Lorenz, J. Kidd. Object Oriented Software Metrics, Prentice Hall, NJ, 1994.
> 6. W. Li and S. Henry. Object-oriented metrics that predict maintainability. Journal of Systems and Software, Volume 23, Issue 2, pages 111-122, November 1993.
> 7. M. Lanza, R. Marinescu. Object-Oriented Metrics in Practice. Using Software Metrics to Characterize, Evaluate, and Improve the Design of Object-Oriented Systems. Springer-Verlag Berlin Heidelberg, 2006.
> 8. J. M. Bieman and B. Kang, Cohesion and reuse in an object-oriented system, Proceedings of the 1995 Symposium on Software reusability, Seattle, Washington, United States, pp. 259-262, 1995.
# Featutes   
 - Represents metrics calculation results in forms of trees and treemaps
 - Builds trees with metrics for class open in the editor or for the entire project
 - Supports controlling calculated metrics values
 - Finds common anti-patterns such as 'god class', 'feature envy', 'brain method' etc and allows to define new ones
 - Displays various metrics properties (distributions, correlations) in charts
 - Shows class metrics values evolution based on 'git log'
 
![Image of MetricsTree](project_metrics_tree.png)
![Image of MetricsTree](metrics_treemap.png)
![Image of MetricsTree](xy_chart.png)
![Image of MetricsTree](pie_chart.png)
![Image of MetricsTree](metric_profiles.png)
![Image of MetricsTree](profiles_metrics_correlation.png)
![Image of MetricsTree](profiles_correlation.png)

# Installation
The plugin can be installed from the JetBrains plugin repository within your IDE with 
Preferences | Plugins | Market Place and searching for MetricsTree. 
# Compatibility
The plugin tested for compatibility with IntelliJ IDEA version 2019.3+.
# Development
The plugin is written in Java using IntelliJ's plugin framework.
The code is hosted on GitHub and has a Travis-CI integration for automatic testing.
Compilation is done with Gradle (v 5.2.1+) using the IntelliJ Gradle plugin and should work out of the box.
# License     
The plugin is distributed under Apache License, version 2.0. For full license terms, see [LICENCE](../blob/master/LICENSE).