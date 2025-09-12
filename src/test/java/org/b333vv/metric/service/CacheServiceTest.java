package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {
    @Mock
    private Project mockProject;
    @Mock
    private VirtualFile mockVirtualFile;
    @Mock
    private FileElement mockJavaFile;
    @Mock
    private TaskQueueService mockTaskQueueService;
    @Mock
    private DependenciesBuilder mockDependenciesBuilder;
    @Mock
    private ProjectElement mockJavaProject;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(mockProject);
        when(mockProject.getService(TaskQueueService.class)).thenReturn(mockTaskQueueService);
        // No need to mock getVirtualFile() as we now pass VirtualFile directly
        when(mockVirtualFile.getPath()).thenReturn("/path/to/Test.java");
    }

    @Test
    void testUserDataOperations() {
        // Test putting and getting user data
        cacheService.putUserData(CacheService.DEPENDENCIES, mockDependenciesBuilder);
        assertEquals(mockDependenciesBuilder, cacheService.getUserData(CacheService.DEPENDENCIES));

        // Test putting and getting another type of user data
        cacheService.putUserData(CacheService.CLASS_AND_METHODS_METRICS, mockJavaProject);
        assertEquals(mockJavaProject, cacheService.getUserData(CacheService.CLASS_AND_METHODS_METRICS));

        // Test invalidating user data
        cacheService.invalidateUserData();
        assertNull(cacheService.getUserData(CacheService.DEPENDENCIES));
        assertNull(cacheService.getUserData(CacheService.CLASS_AND_METHODS_METRICS));
    }

    @Test
    void testJavaFileOperations() {
        // Test adding a JavaFile
        cacheService.addJavaFile(mockVirtualFile, mockJavaFile);
        assertEquals(1, cacheService.getJavaFiles().count());
        assertTrue(cacheService.getJavaFiles().anyMatch(file -> file == mockJavaFile));

        // Test removing a JavaFile
        cacheService.removeJavaFile(mockVirtualFile);
        assertEquals(0, cacheService.getJavaFiles().count());
    }
}
