package org.b333vv.metric.model.metric;

import java.util.Set;

public class Sets {
    private Sets(){}
    //    Chidamber-Kemerer metrics set includes:
    //    WMC: Weighted methods per class
    //    DIT: Depth of Inheritance Tree
    //    NOC: Number of Children
    //    CBO: Coupling between object classes
    //    RFC: Response for a Class
    //    LCOM: Lack of cohesion in methods
    private static final Set<String> chidamberKemererMetricsSet = Set.of("WMC", "DIT", "NOC", "CBO", "RFC", "LCOM");
    public static boolean inChidamberKemererMetricsSet(String metricName) {
        return chidamberKemererMetricsSet.contains(metricName);
    }

    //    Lorenz-Kidd metrics set includes:
    //    NOA: Number of Attributes
    //    NOO: Number of Operations
    //    NOAM: Number of Added Methods
    //    NOOM: Number of Overridden Methods
    //    SIZE2: Number of Attributes and Methods
    private static final Set<String> lorenzKiddMetricsSet = Set.of("NOA", "NOO", "NOAM", "NOOM");
    public static boolean inLorenzKiddMetricsSet(String metricName) {
        return lorenzKiddMetricsSet.contains(metricName);
    }
    //    Li-Henry metrics set includes:
    //    SIZE2: Number of Attributes and Methods
    //    MPC (Message Passing Coupling) - MPC(C) is defined as the number of send statements defined in class C 
    //    DAC (Data Abstraction Coupling) - DAC(C) is defined as the number of ADTs defined in a class C
    //    NOM (Number of Methods) - NOM(C) is defined as the number of local methods in a class C
    private static final Set<String> liHenryMetricsSet = Set.of("SIZE2", "NOM", "DAC", "MPC");
    public static boolean inLiHenryMetricsSet(String metricName) {
        return liHenryMetricsSet.contains(metricName);
    }

    //    Robert C. Martin metrics set includes:
    //    Ce: Efferent Coupling
    //    Ca: Afferent Coupling
    //    I: Instability
    //    A: Abstractness
    //    D: Normalized Distance from Main Sequence
    private static final Set<String> robertMartinMetricsSet = Set.of("Ce", "Ca", "I", "A", "D");
    public static boolean inRobertMartinMetricsSet(String metricName) {
        return robertMartinMetricsSet.contains(metricName);
    }

    //    MOOD metrics set includes:
    //    MHF: Method Hiding Factor
    //    AHF: Attribute Hiding Factor
    //    MIF: Method Inheritance Factor
    //    AIF: Attribute Inheritance Factor
    //    PF: Polymorphism Factor
    //    CF: Coupling Factor
    private static final Set<String> moodMetricsSet = Set.of("MHF", "AHF", "MIF", "AIF", "PF", "CF");
    public static boolean inMoodMetricsSet(String metricName) {
        return moodMetricsSet.contains(metricName);
    }
}
