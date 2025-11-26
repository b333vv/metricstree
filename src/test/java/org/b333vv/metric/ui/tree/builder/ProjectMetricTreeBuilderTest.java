package org.b333vv.metric.ui.tree.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.service.UIStateService;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.PackageNode;
import org.b333vv.metric.ui.tree.node.ProjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectMetricTreeBuilderTest {

    @Mock
    private Project project;
    @Mock
    private UIStateService uiStateService;
    @Mock
    private MetricsTreeFilter metricsTreeFilter;
    @Mock
    private ProjectElement projectElement;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(project.getService(UIStateService.class)).thenReturn(uiStateService);
        when(uiStateService.getProjectMetricsTreeFilter()).thenReturn(metricsTreeFilter);
        when(metricsTreeFilter.isPackageMetricsVisible()).thenReturn(true);
        when(metricsTreeFilter.isClassMetricsVisible()).thenReturn(false);
        when(metricsTreeFilter.isMethodMetricsVisible()).thenReturn(false);
        when(metricsTreeFilter.isMetricsGroupedByMetricSets()).thenReturn(false);
        when(metricsTreeFilter.isMoodMetricsSetVisible()).thenReturn(false);
        when(metricsTreeFilter.isRobertMartinMetricsSetVisible()).thenReturn(false);

        when(projectElement.metrics()).thenReturn(Stream.empty());
    }

    @Test
    public void testCompactingEmptyMiddlePackages() {
        // Structure: com -> example -> app (has files)
        // Expected: com.example.app

        PackageElement com = mock(PackageElement.class);
        PackageElement example = mock(PackageElement.class);
        PackageElement app = mock(PackageElement.class);

        when(com.getName()).thenReturn("com");
        when(example.getName()).thenReturn("example");
        when(app.getName()).thenReturn("app");

        when(com.subPackages()).thenAnswer(i -> Stream.of(example));
        when(com.files()).thenAnswer(i -> Stream.empty());

        when(example.subPackages()).thenAnswer(i -> Stream.of(app));
        when(example.files()).thenAnswer(i -> Stream.empty());

        when(app.subPackages()).thenAnswer(i -> Stream.empty());
        when(app.files()).thenAnswer(i -> Stream.of(mock(FileElement.class)));

        when(projectElement.packages()).thenAnswer(i -> Stream.of(com));

        ProjectMetricTreeBuilder builder = new ProjectMetricTreeBuilder(projectElement, project);
        DefaultTreeModel model = builder.createMetricTreeModel();
        ProjectNode root = (ProjectNode) model.getRoot();

        assertEquals(1, root.getChildCount());
        TreeNode child = root.getChildAt(0);
        assertTrue(child instanceof PackageNode);
        PackageNode packageNode = (PackageNode) child;

        // If compacted, the node should represent 'app' package
        assertEquals(app, packageNode.getJavaPackage());
    }

    @Test
    public void testBranchingPackages() {
        // org -> b333vv (files)
        // org -> other -> sub (files)

        PackageElement org = mock(PackageElement.class);
        PackageElement b333vv = mock(PackageElement.class);
        PackageElement other = mock(PackageElement.class);
        PackageElement sub = mock(PackageElement.class);

        when(org.getName()).thenReturn("org");
        when(b333vv.getName()).thenReturn("b333vv");
        when(other.getName()).thenReturn("other");
        when(sub.getName()).thenReturn("sub");

        when(org.subPackages()).thenAnswer(i -> Stream.of(b333vv, other));
        when(org.files()).thenAnswer(i -> Stream.empty());

        when(b333vv.subPackages()).thenAnswer(i -> Stream.empty());
        when(b333vv.files()).thenAnswer(i -> Stream.of(mock(FileElement.class)));

        when(other.subPackages()).thenAnswer(i -> Stream.of(sub));
        when(other.files()).thenAnswer(i -> Stream.empty());

        when(sub.subPackages()).thenAnswer(i -> Stream.empty());
        when(sub.files()).thenAnswer(i -> Stream.of(mock(FileElement.class)));

        when(projectElement.packages()).thenAnswer(i -> Stream.of(org));

        ProjectMetricTreeBuilder builder = new ProjectMetricTreeBuilder(projectElement, project);
        DefaultTreeModel model = builder.createMetricTreeModel();
        ProjectNode root = (ProjectNode) model.getRoot();

        assertEquals(1, root.getChildCount());
        PackageNode orgNode = (PackageNode) root.getChildAt(0);
        assertEquals(org, orgNode.getJavaPackage());

        assertEquals(2, orgNode.getChildCount());

        boolean foundB333vv = false;
        boolean foundSub = false;

        for (int i = 0; i < orgNode.getChildCount(); i++) {
            TreeNode child = orgNode.getChildAt(i);
            if (child instanceof PackageNode) {
                PackageNode pn = (PackageNode) child;
                if (pn.getJavaPackage() == b333vv)
                    foundB333vv = true;
                if (pn.getJavaPackage() == sub)
                    foundSub = true;
            }
        }

        assertTrue(foundB333vv);
        assertTrue(foundSub);
    }
}
