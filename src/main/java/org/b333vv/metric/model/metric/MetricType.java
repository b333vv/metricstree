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

package org.b333vv.metric.model.metric;

import com.intellij.psi.JavaRecursiveElementVisitor;
import org.b333vv.metric.model.visitor.method.*;
import org.b333vv.metric.model.visitor.type.*;

import static org.b333vv.metric.model.metric.MetricLevel.*;
import static org.b333vv.metric.model.metric.MetricSet.*;

public enum MetricType {
    //Method level metrics
    CND("Condition Nesting Depth", UNDEFINED, METHOD, new ConditionNestingDepthVisitor()),
    LND("Loop Nesting Depth", UNDEFINED, METHOD, new LoopNestingDepthVisitor()),
    CC("McCabe Cyclomatic Complexity", UNDEFINED, METHOD, new McCabeCyclomaticComplexityVisitor()),
    NOL("Number Of Loops", UNDEFINED, METHOD, new NumberOfLoopsVisitor()),
    LOC("Lines Of Code", UNDEFINED, METHOD, new LinesOfCodeVisitor()),

    //Chidamber-Kemerer metrics set
    WMC("Weighted Methods Per Class", CHIDAMBER_KEMERER, CLASS, new WeightedMethodCountVisitor()),
    DIT("Depth Of Inheritance Tree", CHIDAMBER_KEMERER, CLASS, new DepthOfInheritanceTreeVisitor()),
    CBO("Coupling Between Object", CHIDAMBER_KEMERER, CLASS, new CouplingBetweenObjectsVisitor()),
    RFC("Response For A Class", CHIDAMBER_KEMERER, CLASS, new ResponseForClassVisitor()),
    LCOM("Lack Of Cohesion Of Methods", CHIDAMBER_KEMERER, CLASS, new LackOfCohesionOfMethodsVisitor()),
    NOC("Number Of Children", CHIDAMBER_KEMERER, CLASS, new NumberOfChildrenVisitor()),

    //Lorenz-Kidd metrics set
    NOA("Number Of Attributes", LORENZ_KIDD, CLASS, new NumberOfAttributesVisitor()),
    NOO("Number Of Operations", LORENZ_KIDD, CLASS, new NumberOfOperationsVisitor()),
    NOOM("Number Of Overridden Methods", LORENZ_KIDD, CLASS, new NumberOfOverriddenMethodsVisitor()),
    NOAM("Number Of Added Methods", LORENZ_KIDD, CLASS, new NumberOfAddedMethodsVisitor()),

    //Li-Henry metrics set
    SIZE2("Number Of Attributes And Methods", LI_HENRY, CLASS, new NumberOfAttributesAndMethodsVisitor()),
    NOM("Number Of Methods", LI_HENRY, CLASS, new NumberOfMethodsVisitor()),
    MPC("Message Passing Coupling", LI_HENRY, CLASS, new MessagePassingCouplingVisitor()),
    DAC("Data Abstraction Coupling", LI_HENRY, CLASS, new DataAbstractionCouplingVisitor()),

    //Robert C. Martin metrics set
    Ce("Efferent Coupling", R_MARTIN, PACKAGE, null),
    Ca("Afferent Coupling", R_MARTIN, PACKAGE, null),
    I("Instability", R_MARTIN, PACKAGE, null),
    A("Abstractness", R_MARTIN, PACKAGE, null),
    D("Normalized Distance From Main Sequence", R_MARTIN, PACKAGE, null),


    //MOOD metrics set
    MHF("Method Hiding Factor", MOOD, PROJECT, null),
    AHF("Attribute Hiding Factor", MOOD, PROJECT, null),
    MIF("Method Inheritance Factor", MOOD, PROJECT, null),
    AIF( "Attribute Inheritance Factor", MOOD, PROJECT, null),
    CF("Coupling Factor", MOOD, PROJECT, null),
    PF("Polymorphism Factor", MOOD, PROJECT, null);

    private final String description;
    private final String url;
    private final MetricSet set;
    private final MetricLevel level;
    private final JavaRecursiveElementVisitor visitor;

    MetricType(String description, MetricSet set, MetricLevel level, JavaRecursiveElementVisitor visitor) {
        this.description = description;
        this.url = "/html/" + name() + ".html";
        this.set = set;
        this.level = level;
        this.visitor = visitor;
    }

    public String description() {
        return description;
    }

    public String url() {
        return url;
    }

    public MetricSet set() {
        return set;
    }

    public MetricLevel level() {
        return level;
    }

    public JavaRecursiveElementVisitor visitor() {
        return visitor;
    }
}
