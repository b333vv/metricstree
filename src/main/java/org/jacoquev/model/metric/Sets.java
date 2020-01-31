package org.jacoquev.model.metric;

import java.util.Set;

public class Sets {
//    Chidamber-Kemerer metrics set includes:
//    WMC: Weighted methods per class
//    DIT: Depth of Inheritance Tree
//    NOC: Number of Children
//    CBO: Coupling between object classes
//    RFC: Response for a Class
//    LCOM: Lack of cohesion in methods
    private Set<String> chidamberKemererMetricSet = Set.of("WMC", "DIT", "NOC", "CBO", "RFC", "LCOM");

//    Robert C. Martin metrics set includes:
//    Ce: Efferent Coupling
//    Ca: Afferent Coupling
//    I: Instability
//    A: Abstractness
//    D: Normalized Distance from Main Sequence
    private Set<String> robertMartinMetricSet = Set.of("Ce", "Ca", "I", "A", "D");

//    MOOD metrics set includes:
//    MHF: Method Hiding Factor
//    AHF: Attribute Hiding Factor
//    MIF: Method Inheritance Factor
//    AIF: Attribute Inheritance Factor
//    PF: Polymorphism Factor
//    CF: Coupling Factor
    private Set<String> moodMetricSet = Set.of("MHF", "AHF", "MIF", "AIF", "PF", "CF");

//    Lorenz-Kidd metrics set includes:
//    NOA: Number of Attributes
//    NOO: Number of Operations
//    NOAM: Number of Added Methods
//    NOOM: Number of Overridden Methods
    private Set<String> lorenzKiddMetricSet = Set.of("NOA", "NOO", "NOAM", "NOOM");

//    Wei Li metrics set includes:
//    NAC: Number of Ancestor Classes
//    NLM: Number of Local Methods
//    CMC: Class Method Complexity
//    NDC: Number of Descendent Classes
//    CTA: Coupling Through Abstract Data Type
//    CTM: Coupling Through Message Passing
    private Set<String> weiLiMetricSet = Set.of("NAC", "NLM", "CMC", "NDC", "CTA", "CTM");
}
