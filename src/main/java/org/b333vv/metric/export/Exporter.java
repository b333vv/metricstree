package org.b333vv.metric.export;

import org.b333vv.metric.model.code.JavaProject;

public interface Exporter {
    void export(String fileName, JavaProject javaProject);
}
