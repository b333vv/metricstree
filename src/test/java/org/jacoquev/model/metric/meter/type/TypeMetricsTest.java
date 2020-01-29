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
        MetricsService.setMetricsSettings(this.getProject());
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
                .of(Metric.of("NOC", "Number of Children",
                        "/html/NumberOfChildren.html", 4));

        assertEquals(result, numberOfChildren.meter(javaClass));
    }

    public void testWeightedMethodCount() {
        PsiClass psiClass = myFixture.findClass("org.jacoquev.model.code.JavaCode");
        JavaClass javaClass = new JavaClass(psiClass);

        WeightedMethodCount weightedMethodCount = new WeightedMethodCount();
        ImmutableSet<Metric> result = ImmutableSet
                .of(Metric.of("WMC", "Weighted Method Count",
                        "/html/WeightedMethodCount.html", 19));

        assertEquals(result, weightedMethodCount.meter(javaClass));
    }
}