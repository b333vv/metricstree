package org.b333vv.metric.export;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvClassMetricsBuilderTest {

    private Project mockProject;
    private ProjectElement mockJavaProject;
    private String testFileName = "test_class_report.csv";

    @BeforeEach
    void setUp() {
        mockProject = Mockito.mock(Project.class);
        mockJavaProject = Mockito.mock(ProjectElement.class);
    }

    @Test
    void testBuildAndExport() {
        CsvClassMetricsBuilder builder = new CsvClassMetricsBuilder(mockProject);
        builder.buildAndExport(testFileName, mockJavaProject);

        File outputFile = new File(testFileName);
        assertTrue(outputFile.exists());
        // Further assertions would involve parsing the CSV and verifying its content
        // For a real test, you'd want to compare the generated CSV with an expected CSV string or file.
        // This is a placeholder to ensure the file creation part works.
        outputFile.delete(); // Clean up the test file
    }
}
