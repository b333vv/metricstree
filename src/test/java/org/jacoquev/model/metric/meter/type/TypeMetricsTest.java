package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsService;
import org.jacoquev.util.MetricsUtils;

public class TypeMetricsTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(this.getProject());
        MetricsService.init(this.getProject());
        myFixture.configureByFiles("Object.java", "JavaCode.java", "JavaClass.java", "JavaMethod.java",
                "JavaPackage.java", "JavaProject.java");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testDepthOfInheritanceTree() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        JavaClass javaClass = new JavaClass(psiClass);

        DepthOfInheritanceTree depthOfInheritanceTree = new DepthOfInheritanceTree();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("DIT", "Depth Of Inheritance Tree",
                        "/html/DepthOfInheritanceTree.html", 1));

        assertEquals(result, depthOfInheritanceTree.meter(javaClass));
    }

    public void testNumberOfChildren() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfChildren numberOfChildren = new NumberOfChildren();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("NOC", "Number Of Children",
                        "/html/NumberOfChildren.html", 4));

        assertEquals(result, numberOfChildren.meter(javaClass));
    }

    public void testResponseForClass() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        ResponseForClass responseForClass = new ResponseForClass();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("RFC", "Response For Class",
                        "/html/ResponseForClass.html", 9));

        assertEquals(result, responseForClass.meter(javaClass));
    }

    public void testLackOfCohesionOfMethods() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        LackOfCohesionOfMethods lackOfCohesionOfMethods = new LackOfCohesionOfMethods();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("LCOM", "Lack Of Cohesion Of Methods",
                        "/html/LackOfCohesionOfMethods.html",4));

        assertEquals(result, lackOfCohesionOfMethods.meter(javaClass));
    }

    public void testWeightedMethodCount() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightedMethodCount weightedMethodCount = new WeightedMethodCount();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("WMC", "Weighted Method Count",
                        "/html/WeightedMethodCount.html", 11));

        assertEquals(result, weightedMethodCount.meter(javaClass));
    }

    public void testNumberOfAddedMethods() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAddedMethods numberOfAddedMethods = new NumberOfAddedMethods();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("NOAM", "Number Of Added Methods",
                        "/html/NumberOfAddedMethods.html",  7));

        assertEquals(result, numberOfAddedMethods.meter(javaClass));
    }

    public void testNumberOfAttributes() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfAttributes numberOfAttributes = new NumberOfAttributes();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("NOA", "Number Of Attributes",
                        "/html/NumberOfAttributes.html",1));

        assertEquals(result, numberOfAttributes.meter(javaClass));
    }

    public void testNumberOfOperations() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOperations numberOfOperations = new NumberOfOperations();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("NOO", "Number Of Operations",
                        "/html/NumberOfOperations.html", 8));

        assertEquals(result, numberOfOperations.meter(javaClass));
    }

    public void testNumberOfOverriddenMethods() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaClass");
        JavaClass javaClass = new JavaClass(psiClass);

        NumberOfOverriddenMethods numberOfOverriddenMethods = new NumberOfOverriddenMethods();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("NOOM", "Number Of Overridden Methods",
                        "/html/NumberOfOverriddenMethods.html",0));

        assertEquals(result, numberOfOverriddenMethods.meter(javaClass));
    }
}