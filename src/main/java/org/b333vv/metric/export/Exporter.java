package org.b333vv.metric.export;

import org.b333vv.metric.model.code.ProjectElement;

public interface Exporter {
    void export(String fileName, ProjectElement projectElement);
}
