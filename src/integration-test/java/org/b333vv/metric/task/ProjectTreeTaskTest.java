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

package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.tree.DefaultTreeModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for ProjectTreeTask to capture its current behavior before refactoring.
 * This test serves as a contract for the refactoring process.
 */
public class ProjectTreeTaskTest extends BasePlatformTestCase {

    @Mock
    private MetricsEventListener mockMetricsEventListener;

    @Mock
    private ProgressIndicator mockProgressIndicator;

    @Mock
    private MetricTaskManager mockMetricTaskManager;

    @Mock
    private JavaProject mockJavaProject;

    private ProjectTreeTask projectTreeTask;
    private CacheService cacheService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);

        // Set up the cache service
        cacheService = new CacheService(getProject());
        ServiceContainerUtil.replaceService(getProject(), CacheService.class, cacheService, getTestRootDisposable());

        // Set up the mock MetricTaskManager
        ServiceContainerUtil.replaceService(getProject(), MetricTaskManager.class, mockMetricTaskManager, getTestRootDisposable());

        // Subscribe to the message bus
        getProject().getMessageBus().connect(getTestRootDisposable())
                .subscribe(MetricsEventListener.TOPIC, mockMetricsEventListener);

        // Configure mock behavior
        when(mockMetricTaskManager.getProjectModel(any(ProgressIndicator.class))).thenReturn(mockJavaProject);

        // Create the task under test
        projectTreeTask = new ProjectTreeTask(getProject());
    }

    @Test
    public void testProjectTreeTaskCreatesAndCachesTreeModel() {
        // Verify initial cache state - should be empty
        assertNull(cacheService.getUserData(CacheService.PROJECT_TREE));
        assertNull(cacheService.getUserData(CacheService.TREE_BUILDER));

        // Run the task
        projectTreeTask.run(mockProgressIndicator);

        // Verify that the tree model was created and cached
        DefaultTreeModel cachedTreeModel = cacheService.getUserData(CacheService.PROJECT_TREE);
        ProjectMetricTreeBuilder cachedTreeBuilder = cacheService.getUserData(CacheService.TREE_BUILDER);

        assertNotNull(cachedTreeModel);
        assertNotNull(cachedTreeBuilder);

        // Verify that the MetricTaskManager was called to get the project model
        verify(mockMetricTaskManager, times(1)).getProjectModel(mockProgressIndicator);

        // Verify that appropriate messages were published
        verify(mockMetricsEventListener, times(1)).printInfo("Try to getProfiles tree model from cache");
        verify(mockMetricsEventListener, times(1)).printInfo("Building tree model started");
    }

    @Test
    public void testProjectTreeTaskUsesExistingCacheWhenAvailable() {
        // Pre-populate the cache
        DefaultTreeModel existingTreeModel = mock(DefaultTreeModel.class);
        ProjectMetricTreeBuilder existingTreeBuilder = mock(ProjectMetricTreeBuilder.class);
        
        cacheService.putUserData(CacheService.PROJECT_TREE, existingTreeModel);
        cacheService.putUserData(CacheService.TREE_BUILDER, existingTreeBuilder);

        // Run the task
        projectTreeTask.run(mockProgressIndicator);

        // Verify that the existing cached objects are still there (not replaced)
        assertSame(existingTreeModel, cacheService.getUserData(CacheService.PROJECT_TREE));
        assertSame(existingTreeBuilder, cacheService.getUserData(CacheService.TREE_BUILDER));

        // Verify that MetricTaskManager was NOT called since cache was available
        verify(mockMetricTaskManager, never()).getProjectModel(any(ProgressIndicator.class));

        // Verify that only the cache retrieval message was published
        verify(mockMetricsEventListener, times(1)).printInfo("Try to getProfiles tree model from cache");
        verify(mockMetricsEventListener, never()).printInfo("Building tree model started");
    }

    @Test
    public void testProjectTreeTaskHasCorrectTitle() {
        // Verify that the task has the correct title
        assertEquals("Build Project Tree", projectTreeTask.getTitle());
    }

    @Test
    public void testProjectTreeTaskCreatesTreeModelWithCorrectStructure() {
        // Run the task
        projectTreeTask.run(mockProgressIndicator);

        // Get the cached tree model
        DefaultTreeModel cachedTreeModel = cacheService.getUserData(CacheService.PROJECT_TREE);
        assertNotNull(cachedTreeModel);

        // Verify that the tree model has a root node
        assertNotNull(cachedTreeModel.getRoot());

        // Verify that the tree builder was configured with the correct project
        ProjectMetricTreeBuilder cachedTreeBuilder = cacheService.getUserData(CacheService.TREE_BUILDER);
        assertNotNull(cachedTreeBuilder);
    }
}