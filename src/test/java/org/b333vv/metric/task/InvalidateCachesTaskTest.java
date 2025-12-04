package org.b333vv.metric.task;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvalidateCachesTaskTest extends BasePlatformTestCase {

    @Mock
    private VirtualFile file;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);
    }

    public void testConstructor() {
        // This should not throw IllegalArgumentException
        new InvalidateCachesTask(getProject(), file);
    }
}
